package com.arcadedb.sql.executor;

import com.arcadedb.exception.PTimeoutException;
import com.arcadedb.sql.parser.Identifier;
import com.arcadedb.sql.parser.OLocalResultSet;
import com.arcadedb.sql.parser.Statement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by luigidellaquila on 03/08/16.
 */
public class GlobalLetQueryStep extends AbstractExecutionStep {

  private final Identifier             varName;
  private final OInternalExecutionPlan subExecutionPlan;

  boolean executed = false;


  public GlobalLetQueryStep(Identifier varName, Statement query, OCommandContext ctx, boolean profilingEnabled) {
    super(ctx, profilingEnabled);
    this.varName = varName;

    OBasicCommandContext subCtx = new OBasicCommandContext();
    subCtx.setDatabase(ctx.getDatabase());
    subCtx.setParent(ctx);
    subExecutionPlan = query.createExecutionPlan(subCtx, profilingEnabled);
  }

  @Override public OResultSet syncPull(OCommandContext ctx, int nRecords) throws PTimeoutException {
    getPrev().ifPresent(x -> x.syncPull(ctx, nRecords));
    calculate(ctx);
    return new OInternalResultSet();
  }

  private void calculate(OCommandContext ctx) {
    if (executed) {
      return;
    }
    ctx.setVariable(varName.getStringValue(), toList(new OLocalResultSet(subExecutionPlan)));
    executed = true;
  }



  private List<OResult> toList(OLocalResultSet oLocalResultSet) {
    List<OResult> result = new ArrayList<>();
    while (oLocalResultSet.hasNext()) {
      result.add(oLocalResultSet.next());
    }
    oLocalResultSet.close();
    return result;
  }

  @Override public String prettyPrint(int depth, int indent) {
    String spaces = OExecutionStepInternal.getIndent(depth, indent);
    return spaces + "+ LET (once)\n" +
        spaces + "  " + varName + " = \n" + box(spaces+"    ", this.subExecutionPlan.prettyPrint(0, indent));
  }

  @Override
  public List<OExecutionPlan> getSubExecutionPlans() {
    return Collections.singletonList(this.subExecutionPlan);
  }

  private String box(String spaces, String s) {
    String[] rows = s.split("\n");
    StringBuilder result = new StringBuilder();
    result.append(spaces);
    result.append("+-------------------------\n");
    for(String row:rows){
      result.append(spaces);
      result.append("| ");
      result.append(row);
      result.append("\n");
    }
    result.append(spaces);
    result.append("+-------------------------");
    return result.toString();
  }
}
