/*
 * Copyright (c) - Arcade Data LTD (https://arcadedata.com)
 */

package com.arcadedb.sql.executor;

import com.arcadedb.database.Database;
import com.arcadedb.exception.CommandExecutionException;
import com.arcadedb.exception.TimeoutException;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by luigidellaquila on 01/03/17.
 */
public class FilterByClustersStep extends AbstractExecutionStep {
  private Set<String>  clusters;
  private Set<Integer> bucketIds;

  ResultSet prevResult = null;

  public FilterByClustersStep(Set<String> filterClusters, CommandContext ctx, boolean profilingEnabled) {
    super(ctx, profilingEnabled);
    this.clusters = filterClusters;
    Database db = ctx.getDatabase();
    init(db);

  }

  private void init(Database db) {
//    if (this.bucketIds == null) {
//      this.bucketIds = clusters.stream().map(x -> db.getClusterIdByName(x)).filter(x -> x != null).collect(Collectors.toSet());
//    }
    throw new UnsupportedOperationException();
  }

  @Override
  public ResultSet syncPull(CommandContext ctx, int nRecords) throws TimeoutException {
    init(ctx.getDatabase());
    if (!prev.isPresent()) {
      throw new IllegalStateException("filter step requires a previous step");
    }
    ExecutionStepInternal prevStep = prev.get();

    return new ResultSet() {
      public boolean finished = false;

      Result nextItem = null;
      int fetched = 0;

      private void fetchNextItem() {
        nextItem = null;
        if (finished) {
          return;
        }
        if (prevResult == null) {
          prevResult = prevStep.syncPull(ctx, nRecords);
          if (!prevResult.hasNext()) {
            finished = true;
            return;
          }
        }
        while (!finished) {
          while (!prevResult.hasNext()) {
            prevResult = prevStep.syncPull(ctx, nRecords);
            if (!prevResult.hasNext()) {
              finished = true;
              return;
            }
          }
          nextItem = prevResult.next();
          if (nextItem.isElement()) {
            int bucketId = nextItem.getIdentity().get().getBucketId();
            if (bucketId < 0) {
              // this record comes from a TX, it still doesn't have a bucket assigned
              break;
            }
            if (bucketIds.contains(bucketId)) {
              break;
            }
          }
          nextItem = null;
        }
      }

      @Override
      public boolean hasNext() {

        if (fetched >= nRecords || finished) {
          return false;
        }
        if (nextItem == null) {
          fetchNextItem();
        }

        return nextItem != null;

      }

      @Override
      public Result next() {
        if (fetched >= nRecords || finished) {
          throw new IllegalStateException();
        }
        if (nextItem == null) {
          fetchNextItem();
        }
        if (nextItem == null) {
          throw new IllegalStateException();
        }
        Result result = nextItem;
        nextItem = null;
        fetched++;
        return result;
      }

      @Override
      public void close() {
        FilterByClustersStep.this.close();
      }

      @Override
      public Optional<ExecutionPlan> getExecutionPlan() {
        return null;
      }

      @Override
      public Map<String, Long> getQueryStats() {
        return null;
      }
    };

  }

  @Override
  public String prettyPrint(int depth, int indent) {
    return ExecutionStepInternal.getIndent(depth, indent) + "+ FILTER ITEMS BY CLUSTERS \n" + ExecutionStepInternal
        .getIndent(depth, indent) + "  " + clusters.stream().collect(Collectors.joining(", "));
  }

  @Override
  public Result serialize() {
    ResultInternal result = ExecutionStepInternal.basicSerialize(this);
    if (clusters != null) {
      result.setProperty("clusters", clusters);
    }

    return result;
  }

  @Override
  public void deserialize(Result fromResult) {
    try {
      ExecutionStepInternal.basicDeserialize(fromResult, this);
      clusters = fromResult.getProperty("clusters");
    } catch (Exception e) {
      throw new CommandExecutionException(e);
    }
  }

}