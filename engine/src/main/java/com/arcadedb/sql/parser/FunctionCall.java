/*
 * Copyright (c) 2018 - Arcade Analytics LTD (https://arcadeanalytics.com)
 */

/* Generated By:JJTree: Do not edit this line. OFunctionCall.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=true,NODE_PREFIX=O,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_USERTYPE_VISIBILITY_PUBLIC=true */
package com.arcadedb.sql.parser;

import com.arcadedb.database.Database;
import com.arcadedb.database.Identifiable;
import com.arcadedb.database.Record;
import com.arcadedb.exception.CommandExecutionException;
import com.arcadedb.sql.executor.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class FunctionCall extends SimpleNode {

  protected Identifier name;

  protected List<Expression> params = new ArrayList<Expression>();

  public FunctionCall(int id) {
    super(id);
  }

  public FunctionCall(SqlParser p, int id) {
    super(p, id);
  }

  public static Database getDatabase() {
    throw new UnsupportedOperationException();
  }

  /**
   * Accept the visitor. *
   */
  public Object jjtAccept(SqlParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }

  public boolean isStar() {

    if (this.params.size() != 1) {
      return false;
    }
    Expression param = params.get(0);
    if (param.mathExpression == null || !(param.mathExpression instanceof BaseExpression)) {

      return false;
    }
    BaseExpression base = (BaseExpression) param.mathExpression;
    if (base.identifier == null || base.identifier.suffix == null) {
      return false;
    }
    return base.identifier.suffix.star;
  }

  public List<Expression> getParams() {
    return params;
  }

  public void setParams(List<Expression> params) {
    this.params = params;
  }

  public void toString(Map<Object, Object> params, StringBuilder builder) {
    name.toString(params, builder);
    builder.append("(");
    boolean first = true;
    for (Expression expr : this.params) {
      if (!first) {
        builder.append(", ");
      }
      expr.toString(params, builder);
      first = false;
    }
    builder.append(")");
  }

  public Object execute(Object targetObjects, CommandContext ctx) {
    return execute(targetObjects, ctx, name.getStringValue());
  }

  private Object execute(Object targetObjects, CommandContext ctx, String name) {
    List<Object> paramValues = new ArrayList<Object>();

    Object record = null;

    if (record == null) {
      if (targetObjects instanceof Identifiable) {
        record = targetObjects;
      } else if (targetObjects instanceof Result) {
        record = ((Result) targetObjects).toElement();
      } else {
        record = targetObjects;
      }
    }
    if (record == null) {
      Object current = ctx == null ? null : ctx.getVariable("$current");
      if (current != null) {
        if (current instanceof Identifiable) {
          record = current;
        } else if (current instanceof Result) {
          record = ((Result) current).toElement();
        } else {
          record = current;
        }
      }
    }
    for (Expression expr : this.params) {
      if (record instanceof Identifiable) {
        paramValues.add(expr.execute((Identifiable) record, ctx));
      } else if (record instanceof Result) {
        paramValues.add(expr.execute((Result) record, ctx));
      } else if (record == null) {
        paramValues.add(expr.execute((Result) record, ctx));
      } else {
        throw new CommandExecutionException("Invalid value for $current: " + record);
      }
    }
    SQLFunction function = SQLEngine.getInstance().getFunction(name);
    if (function != null) {
      if (record instanceof Identifiable) {
        return function.execute(targetObjects, (Identifiable) record, null, paramValues.toArray(), ctx);
      } else if (record instanceof Result) {
        return function.execute(targetObjects, ((Result) record).getElement().orElse(null), null, paramValues.toArray(), ctx);
      } else if (record == null) {
        return function.execute(targetObjects, null, null, paramValues.toArray(), ctx);
      } else {
        throw new CommandExecutionException("Invalid value for $current: " + record);
      }
    } else {
      throw new CommandExecutionException("Funciton not found: " + name);
    }
  }

  public boolean isIndexedFunctionCall() {
    SQLFunction function = SQLEngine.getInstance().getFunction(name.getStringValue());
    return (function instanceof OIndexableSQLFunction);
  }

  /**
   * see OIndexableSQLFunction.searchFromTarget()
   *
   * @param target
   * @param ctx
   * @param operator
   * @param rightValue
   *
   * @return
   */
  public Iterable<Record> executeIndexedFunction(FromClause target, CommandContext ctx, BinaryCompareOperator operator,
      Object rightValue) {
    SQLFunction function = SQLEngine.getInstance().getFunction(name.getStringValue());
    if (function instanceof OIndexableSQLFunction) {
      return ((OIndexableSQLFunction) function)
          .searchFromTarget(target, operator, rightValue, ctx, this.getParams().toArray(new Expression[] {}));
    }
    return null;
  }

  /**
   * @param target     query target
   * @param ctx        execution context
   * @param operator   operator at the right of the function
   * @param rightValue value to compare to funciton result
   *
   * @return the approximate number of items returned by the condition execution, -1 if the extimation cannot be executed
   */
  public long estimateIndexedFunction(FromClause target, CommandContext ctx, BinaryCompareOperator operator, Object rightValue) {
    SQLFunction function = SQLEngine.getInstance().getFunction(name.getStringValue());
    if (function instanceof OIndexableSQLFunction) {
      return ((OIndexableSQLFunction) function)
          .estimate(target, operator, rightValue, ctx, this.getParams().toArray(new Expression[] {}));
    }
    return -1;
  }

  /**
   * tests if current function is an indexed function AND that function can also be executed without using the index
   *
   * @param target   the query target
   * @param context  the execution context
   * @param operator
   * @param right
   *
   * @return true if current function is an indexed funciton AND that function can also be executed without using the index, false
   * otherwise
   */
  public boolean canExecuteIndexedFunctionWithoutIndex(FromClause target, CommandContext context, BinaryCompareOperator operator,
      Object right) {
    SQLFunction function = SQLEngine.getInstance().getFunction(name.getStringValue());
    if (function instanceof OIndexableSQLFunction) {
      return ((OIndexableSQLFunction) function)
          .canExecuteInline(target, operator, right, context, this.getParams().toArray(new Expression[] {}));
    }
    return false;
  }

  /**
   * tests if current function is an indexed function AND that function can be used on this target
   *
   * @param target   the query target
   * @param context  the execution context
   * @param operator
   * @param right
   *
   * @return true if current function is an indexed function AND that function can be used on this target, false otherwise
   */
  public boolean allowsIndexedFunctionExecutionOnTarget(FromClause target, CommandContext context,
      BinaryCompareOperator operator, Object right) {
    SQLFunction function = SQLEngine.getInstance().getFunction(name.getStringValue());
    if (function instanceof OIndexableSQLFunction) {
      return ((OIndexableSQLFunction) function)
          .allowsIndexedExecution(target, operator, right, context, this.getParams().toArray(new Expression[] {}));
    }
    return false;
  }

  /**
   * tests if current expression is an indexed function AND the function has also to be executed after the index search. In some
   * cases, the index search is accurate, so this condition can be excluded from further evaluation. In other cases the result from
   * the index is a superset of the expected result, so the function has to be executed anyway for further filtering
   *
   * @param target  the query target
   * @param context the execution context
   *
   * @return true if current expression is an indexed function AND the function has also to be executed after the index search.
   */
  public boolean executeIndexedFunctionAfterIndexSearch(FromClause target, CommandContext context,
      BinaryCompareOperator operator, Object right) {
    SQLFunction function = SQLEngine.getInstance().getFunction(name.getStringValue());
    if (function instanceof OIndexableSQLFunction) {
      return ((OIndexableSQLFunction) function)
          .shouldExecuteAfterSearch(target, operator, right, context, this.getParams().toArray(new Expression[] {}));
    }
    return false;
  }

  public boolean isExpand() {
    return name.getStringValue().equals("expand");
  }

  public boolean needsAliases(Set<String> aliases) {
    for (Expression param : params) {
      if (param.needsAliases(aliases)) {
        return true;
      }
    }
    return false;
  }

  public boolean isAggregate() {
    if (isAggregateFunction()) {
      return true;
    }

    for (Expression exp : params) {
      if (exp.isAggregate()) {
        return true;
      }
    }

    return false;
  }

  public SimpleNode splitForAggregation(AggregateProjectionSplit aggregateProj) {
    if (isAggregate()) {
      FunctionCall newFunct = new FunctionCall(-1);
      newFunct.name = this.name;
      Identifier functionResultAlias = aggregateProj.getNextAlias();

      if (isAggregateFunction()) {

        if (isStar()) {
          for (Expression param : params) {
            newFunct.getParams().add(param);
          }
        } else {
          for (Expression param : params) {
            if (param.isAggregate()) {
              throw new CommandExecutionException(
                  "Cannot calculate an aggregate function of another aggregate function " + toString());
            }
            Identifier nextAlias = aggregateProj.getNextAlias();
            ProjectionItem paramItem = new ProjectionItem(-1);
            paramItem.alias = nextAlias;
            paramItem.expression = param;
            aggregateProj.getPreAggregate().add(paramItem);

            newFunct.params.add(new Expression(nextAlias));
          }
        }
        aggregateProj.getAggregate().add(createProjection(newFunct, functionResultAlias));
        return new Expression(functionResultAlias);
      } else {
        if (isStar()) {
          for (Expression param : params) {
            newFunct.getParams().add(param);
          }
        } else {
          for (Expression param : params) {
            newFunct.getParams().add(param.splitForAggregation(aggregateProj));
          }
        }
      }
      return newFunct;
    }
    return this;
  }

  private boolean isAggregateFunction() {
    SQLFunction function = SQLEngine.getInstance().getFunction(name.getStringValue());
    function.config(this.params.toArray());
    return function.aggregateResults();
  }

  private ProjectionItem createProjection(FunctionCall newFunct, Identifier alias) {
    LevelZeroIdentifier l0 = new LevelZeroIdentifier(-1);
    l0.functionCall = newFunct;
    BaseIdentifier l1 = new BaseIdentifier(-1);
    l1.levelZero = l0;
    BaseExpression l2 = new BaseExpression(-1);
    l2.identifier = l1;
    Expression l3 = new Expression(-1);
    l3.mathExpression = l2;
    ProjectionItem item = new ProjectionItem(-1);
    item.alias = alias;
    item.expression = l3;
    return item;
  }

  public boolean isEarlyCalculated() {
    for (Expression param : params) {
      if (!param.isEarlyCalculated()) {
        return false;
      }
    }
    return true;
  }

  public AggregationContext getAggregationContext(CommandContext ctx) {
    SQLFunction function = SQLEngine.getInstance().getFunction(name.getStringValue());
    function.config(this.params.toArray());

    FunctionAggregationContext result = new FunctionAggregationContext(function, this.params);
    return result;
  }

  public FunctionCall copy() {
    FunctionCall result = new FunctionCall(-1);
    result.name = name;
    result.params = params.stream().map(x -> x.copy()).collect(Collectors.toList());
    return result;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    FunctionCall that = (FunctionCall) o;

    if (name != null ? !name.equals(that.name) : that.name != null)
      return false;
    return params != null ? params.equals(that.params) : that.params == null;
  }

  @Override
  public int hashCode() {
    int result = name != null ? name.hashCode() : 0;
    result = 31 * result + (params != null ? params.hashCode() : 0);
    return result;
  }

  public boolean refersToParent() {
    if (params != null) {
      for (Expression param : params) {
        if (param != null && param.refersToParent()) {
          return true;
        }
      }
    }
    return false;
  }

  public Identifier getName() {
    return name;
  }

  public MethodCall toMethod() {
    MethodCall result = new MethodCall(-1);
    result.methodName = name.copy();
    result.params = params.stream().map(x -> x.copy()).collect(Collectors.toList());
    return result;
  }

  public Result serialize() {
    ResultInternal result = new ResultInternal();
    if (name != null) {
      result.setProperty("name", name.serialize());
    }
    if (params != null) {
      result.setProperty("collection", params.stream().map(x -> x.serialize()).collect(Collectors.toList()));
    }
    return result;
  }

  public void deserialize(Result fromResult) {
    if (fromResult.getProperty("name") != null) {
      name = new Identifier(-1);
      Identifier.deserialize(fromResult.getProperty("name"));
    }
    if (fromResult.getProperty("params") != null) {
      params = new ArrayList<>();
      List<Result> ser = fromResult.getProperty("params");
      for (Result item : ser) {
        Expression exp = new Expression(-1);
        exp.deserialize(item);
        params.add(exp);
      }
    }
  }

  public void extractSubQueries(Identifier letAlias, SubQueryCollector collector) {
    for (Expression param : this.params) {
      param.extractSubQueries(letAlias, collector);
    }
  }

  public void extractSubQueries(SubQueryCollector collector) {
    for (Expression param : this.params) {
      param.extractSubQueries(collector);
    }
  }

  public boolean isCacheable() {
    return isGraphFunction();
  }

  private boolean isGraphFunction() {
    String string = name.getStringValue();
    if (string.equalsIgnoreCase("out")) {
      return true;
    }
    if (string.equalsIgnoreCase("outE")) {
      return true;
    }
    if (string.equalsIgnoreCase("outV")) {
      return true;
    }
    if (string.equalsIgnoreCase("in")) {
      return true;
    }
    if (string.equalsIgnoreCase("inE")) {
      return true;
    }
    if (string.equalsIgnoreCase("inV")) {
      return true;
    }
    if (string.equalsIgnoreCase("both")) {
      return true;
    }
    if (string.equalsIgnoreCase("bothE")) {
      return true;
    }
    return string.equalsIgnoreCase("bothV");
  }
}
/* JavaCC - OriginalChecksum=290d4e1a3f663299452e05f8db718419 (do not edit this line) */
