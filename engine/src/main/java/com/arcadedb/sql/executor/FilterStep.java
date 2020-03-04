/*
 * Copyright (c) - Arcade Data LTD (https://arcadedata.com)
 */

package com.arcadedb.sql.executor;

import com.arcadedb.exception.CommandExecutionException;
import com.arcadedb.exception.TimeoutException;
import com.arcadedb.sql.parser.WhereClause;

import java.util.Map;
import java.util.Optional;

/**
 * Created by luigidellaquila on 12/07/16.
 */
public class FilterStep extends AbstractExecutionStep {
  private WhereClause whereClause;

  ResultSet prevResult = null;

  private long cost;

  public FilterStep(WhereClause whereClause, CommandContext ctx, boolean profilingEnabled) {
    super(ctx, profilingEnabled);
    this.whereClause = whereClause;
  }

  @Override
  public ResultSet syncPull(CommandContext ctx, int nRecords) throws TimeoutException {
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
          long begin = profilingEnabled ? System.nanoTime() : 0;
          try {
            if (whereClause.matchesFilters(nextItem, ctx)) {
              break;
            }

            nextItem = null;
          } finally {
            if (profilingEnabled) {
              cost += (System.nanoTime() - begin);
            }
          }
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
        FilterStep.this.close();
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
    StringBuilder result = new StringBuilder();
    result.append(ExecutionStepInternal.getIndent(depth, indent) + "+ FILTER ITEMS WHERE ");
    if (profilingEnabled) {
      result.append(" (" + getCostFormatted() + ")");
    }
    result.append("\n");
    result.append(ExecutionStepInternal.getIndent(depth, indent));
    result.append("  ");
    result.append(whereClause.toString());
    return result.toString();
  }

  @Override
  public Result serialize() {
    ResultInternal result = ExecutionStepInternal.basicSerialize(this);
    if (whereClause != null) {
      result.setProperty("whereClause", whereClause.serialize());
    }

    return result;
  }

  @Override
  public void deserialize(Result fromResult) {
    try {
      ExecutionStepInternal.basicDeserialize(fromResult, this);
      whereClause = new WhereClause(-1);
      whereClause.deserialize(fromResult.getProperty("whereClause"));
    } catch (Exception e) {
      throw new CommandExecutionException(e);
    }
  }

  @Override
  public long getCost() {
    return cost;
  }

  @Override
  public boolean canBeCached() {
    return true;
  }

  @Override
  public ExecutionStep copy(CommandContext ctx) {
    return new FilterStep(this.whereClause.copy(), ctx, profilingEnabled);
  }
}
