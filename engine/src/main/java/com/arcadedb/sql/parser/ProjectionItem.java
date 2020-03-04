/*
 * Copyright (c) - Arcade Data LTD (https://arcadedata.com)
 */

/* Generated By:JJTree: Do not edit this line. OProjectionItem.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=true,NODE_PREFIX=O,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_USERTYPE_VISIBILITY_PUBLIC=true */
package com.arcadedb.sql.parser;

import com.arcadedb.database.Record;
import com.arcadedb.exception.CommandExecutionException;
import com.arcadedb.sql.executor.AggregationContext;
import com.arcadedb.sql.executor.CommandContext;
import com.arcadedb.sql.executor.Result;
import com.arcadedb.sql.executor.ResultInternal;

import java.util.Map;

public class ProjectionItem extends SimpleNode {

  protected boolean all = false;

  protected Identifier alias;

  protected Expression expression;

  protected Boolean aggregate;

  protected NestedProjection nestedProjection;

  public ProjectionItem(Expression expression, Identifier alias, NestedProjection nestedProjection) {
    super(-1);
    this.expression = expression;
    this.alias = alias;
    this.nestedProjection = nestedProjection;
  }

  public ProjectionItem(int id) {
    super(id);
  }

  public ProjectionItem(SqlParser p, int id) {
    super(p, id);
  }

  /**
   * Accept the visitor.
   **/
  public Object jjtAccept(SqlParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }

  public boolean isAll() {
    if (all) {
      return true;
    }
    return expression != null && "*".equals(expression.toString());
  }

  public void setAll(boolean all) {
    this.all = all;
  }

  public Identifier getAlias() {
    return alias;
  }

  public void setAlias(Identifier alias) {
    this.alias = alias;
  }

  public Expression getExpression() {
    return expression;
  }

  public void setExpression(Expression expression) {
    this.expression = expression;
  }

  public void toString(Map<Object, Object> params, StringBuilder builder) {
    if (all) {
      builder.append("*");
    } else {
      if (expression != null) {
        expression.toString(params, builder);
      }
      if (nestedProjection != null) {
        builder.append(" ");
        nestedProjection.toString(params, builder);
      }
      if (alias != null) {

        builder.append(" AS ");
        alias.toString(params, builder);
      }
    }
  }

  public Object execute(Record iCurrentRecord, CommandContext ctx) {
    Object result;
    if (all) {
      result = iCurrentRecord;
    } else {
      result = expression.execute(iCurrentRecord, ctx);
    }
    if (nestedProjection != null) {
      result = nestedProjection.apply(expression, result, ctx);
    }
    return convert(result);
  }

  private Object convert(Object value) {
//    if (value instanceof ORidBag) {
//      List result = new ArrayList();
//      ((ORidBag) value).forEach(x -> result.add(x));
//      return result;
//    }
    return value;
  }

  public Object execute(Result iCurrentRecord, CommandContext ctx) {
    Object result;
    if (all) {
      result = iCurrentRecord;
    } else {
      result = expression.execute(iCurrentRecord, ctx);
    }
    if (nestedProjection != null) {
      result = nestedProjection.apply(expression, result, ctx);
    }
    return convert(result);
  }

  /**
   * returns the final alias for this projection item (the explicit alias, if defined, or the default alias)
   *
   * @return the final alias for this projection item
   */
  public String getProjectionAliasAsString() {
    return getProjectionAlias().getStringValue();
  }

  public Identifier getProjectionAlias() {
    if (alias != null) {
      return alias;
    }
    Identifier result;
    if (all) {
      result = new Identifier("*");
    } else {
      result = new Identifier(expression.toString());
    }
    return result;
  }

  public boolean isExpand() {
    return expression.isExpand();
  }

  public ProjectionItem getExpandContent() {
    ProjectionItem result = new ProjectionItem(-1);
    result.setExpression(expression.getExpandContent());
    return result;
  }

  public boolean isAggregate() {
    if (aggregate != null) {
      return aggregate;
    }
    if (all) {
      aggregate = false;
      return false;
    }
    if (expression.isAggregate()) {
      aggregate = true;
      return true;
    }
    aggregate = false;
    return false;
  }

  /**
   * INTERNAL USE ONLY this has to be invoked ONLY if the item is aggregate!!!
   *
   * @param aggregateSplit
   */
  public ProjectionItem splitForAggregation(AggregateProjectionSplit aggregateSplit) {
    if (isAggregate()) {
      ProjectionItem result = new ProjectionItem(-1);
      result.alias = getProjectionAlias();
      result.expression = expression.splitForAggregation(aggregateSplit);
      result.nestedProjection = nestedProjection;
      return result;
    } else {
      return this;
    }
  }

  public AggregationContext getAggregationContext(CommandContext ctx) {
    if (expression == null) {
      throw new CommandExecutionException("Cannot aggregate on this projection: " + toString());
    }
    return expression.getAggregationContext(ctx);
  }

  public ProjectionItem copy() {
    ProjectionItem result = new ProjectionItem(-1);
    result.all = all;
    result.alias = alias == null ? null : alias.copy();
    result.expression = expression == null ? null : expression.copy();
    result.nestedProjection = nestedProjection == null ? null : nestedProjection.copy();
    result.aggregate = aggregate;
    return result;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    ProjectionItem that = (ProjectionItem) o;

    if (all != that.all)
      return false;
    if (alias != null ? !alias.equals(that.alias) : that.alias != null)
      return false;
    if (expression != null ? !expression.equals(that.expression) : that.expression != null)
      return false;
    if (nestedProjection != null ? !nestedProjection.equals(that.nestedProjection) : that.nestedProjection != null)
      return false;
    return aggregate != null ? aggregate.equals(that.aggregate) : that.aggregate == null;
  }

  @Override
  public int hashCode() {
    int result = (all ? 1 : 0);
    result = 31 * result + (alias != null ? alias.hashCode() : 0);
    result = 31 * result + (expression != null ? expression.hashCode() : 0);
    result = 31 * result + (nestedProjection != null ? nestedProjection.hashCode() : 0);
    result = 31 * result + (aggregate != null ? aggregate.hashCode() : 0);
    return result;
  }

  public void extractSubQueries(SubQueryCollector collector) {
    if (expression != null) {
      expression.extractSubQueries(collector);
    }
  }

  public boolean refersToParent() {
    if (expression != null) {
      return expression.refersToParent();
    }
    return false;
  }

  public Result serialize() {
    ResultInternal result = new ResultInternal();
    result.setProperty("all", all);
    if (alias != null) {
      result.setProperty("alias", alias.serialize());
    }
    if (expression != null) {
      result.setProperty("expression", expression.serialize());
    }
    result.setProperty("aggregate", aggregate);
    if (nestedProjection != null) {
      result.setProperty("nestedProjection", nestedProjection.serialize());
    }
    return result;
  }

  public void deserialize(Result fromResult) {
    all = fromResult.getProperty("all");
    if (fromResult.getProperty("alias") != null) {
      alias = Identifier.deserialize(fromResult.getProperty("alias"));
    }
    if (fromResult.getProperty("expression") != null) {
      expression = new Expression(-1);
      expression.deserialize(fromResult.getProperty("expression"));
    }
    aggregate = fromResult.getProperty("aggregate");
    if (fromResult.getProperty("nestedProjection") != null) {
      nestedProjection = new NestedProjection(-1);
      nestedProjection.deserialize(fromResult.getProperty("nestedProjection"));
    }
  }

  public void setNestedProjection(NestedProjection nestedProjection) {
    this.nestedProjection = nestedProjection;
  }

  public boolean isCacheable() {
    if (expression != null) {
      return expression.isCacheable();
    }
    return true;
  }
}
/* JavaCC - OriginalChecksum=6d6010734c7434a6f516e2eac308e9ce (do not edit this line) */
