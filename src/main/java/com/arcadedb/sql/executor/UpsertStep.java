package com.arcadedb.sql.executor;

import com.arcadedb.exception.PCommandExecutionException;
import com.arcadedb.exception.PTimeoutException;
import com.arcadedb.sql.parser.AndBlock;
import com.arcadedb.sql.parser.BooleanExpression;
import com.arcadedb.sql.parser.FromClause;
import com.arcadedb.sql.parser.WhereClause;

import java.util.List;

/**
 * @author Luigi Dell'Aquila (l.dellaquila-(at)-orientdb.com)
 */
public class UpsertStep extends AbstractExecutionStep {
  private final FromClause  commandTarget;
  private final WhereClause initialFilter;

  boolean applied = false;

  public UpsertStep(FromClause target, WhereClause where, OCommandContext ctx, boolean profilingEnabled) {
    super(ctx, profilingEnabled);
    this.commandTarget = target;
    this.initialFilter = where;
  }

  @Override
  public OResultSet syncPull(OCommandContext ctx, int nRecords) throws PTimeoutException {
    if (applied) {
      return getPrev().get().syncPull(ctx, nRecords);
    }
    applied = true;
    OResultSet upstream = getPrev().get().syncPull(ctx, nRecords);
    if (upstream.hasNext()) {
      return upstream;
    }
    OInternalResultSet result = new OInternalResultSet();
    result.add(createNewRecord(commandTarget, initialFilter));
    return result;
  }

  private OResult createNewRecord(FromClause commandTarget, WhereClause initialFilter) {
    throw new UnsupportedOperationException(); //TODO
//    if (commandTarget.getItem().getIdentifier() == null) {
//      throw new PCommandExecutionException("Cannot execute UPSERT on target '" + commandTarget + "'");
//    }
//
//    ODocument doc = new ODocument(commandTarget.getItem().getIdentifier().getStringValue());
//    OUpdatableResult result = new OUpdatableResult(doc);
//    if (initialFilter != null) {
//      setContent(result, initialFilter);
//    }
//    return result;
  }

  private void setContent(OResultInternal doc, WhereClause initialFilter) {
    List<AndBlock> flattened = initialFilter.flatten();
    if (flattened.size() == 0) {
      return;
    }
    if (flattened.size() > 1) {
      throw new PCommandExecutionException("Cannot UPSERT on OR conditions");
    }
    AndBlock andCond = flattened.get(0);
    for (BooleanExpression condition : andCond.getSubBlocks()) {
      condition.transformToUpdateItem().ifPresent(x -> x.applyUpdate(doc, ctx));
    }
  }

  @Override
  public String prettyPrint(int depth, int indent) {
    String spaces = OExecutionStepInternal.getIndent(depth, indent);
    StringBuilder result = new StringBuilder();
    result.append(spaces);
    result.append("+ INSERT (upsert, if needed)\n");
    result.append(spaces);
    result.append("  target: ");
    result.append(commandTarget);
    result.append("\n");
    result.append(spaces);
    result.append("  content: ");
    result.append(initialFilter);
    return result.toString();
  }
}
