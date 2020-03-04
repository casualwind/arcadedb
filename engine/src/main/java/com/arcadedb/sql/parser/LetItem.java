/*
 * Copyright (c) - Arcade Data LTD (https://arcadedata.com)
 */

/* Generated By:JJTree: Do not edit this line. OLetItem.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=true,NODE_PREFIX=O,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_USERTYPE_VISIBILITY_PUBLIC=true */
package com.arcadedb.sql.parser;

import com.arcadedb.sql.executor.Result;
import com.arcadedb.sql.executor.ResultInternal;

import java.util.Map;

public class LetItem extends SimpleNode {

  Identifier varName;
  Expression expression;
  Statement  query;

  public LetItem(int id) {
    super(id);
  }

  public LetItem(SqlParser p, int id) {
    super(p, id);
  }

  /**
   * Accept the visitor.
   **/
  public Object jjtAccept(SqlParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }

  public void toString(Map<Object, Object> params, StringBuilder builder) {
    varName.toString(params, builder);
    builder.append(" = ");
    if (expression != null) {
      expression.toString(params, builder);
    } else if (query != null) {
      builder.append("(");
      query.toString(params, builder);
      builder.append(")");
    }
  }

  public LetItem copy() {
    LetItem result = new LetItem(-1);
    result.varName = varName.copy();
    result.expression = expression == null ? null : expression.copy();
    result.query = query == null ? null : query.copy();
    return result;
  }

  public void setVarName(Identifier varName) {
    this.varName = varName;
  }

  public void setExpression(Expression expression) {
    this.expression = expression;
  }

  public void setQuery(Statement query) {
    this.query = query;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    LetItem oLetItem = (LetItem) o;

    if (varName != null ? !varName.equals(oLetItem.varName) : oLetItem.varName != null)
      return false;
    if (expression != null ? !expression.equals(oLetItem.expression) : oLetItem.expression != null)
      return false;
    return query != null ? query.equals(oLetItem.query) : oLetItem.query == null;
  }

  @Override
  public int hashCode() {
    int result = varName != null ? varName.hashCode() : 0;
    result = 31 * result + (expression != null ? expression.hashCode() : 0);
    result = 31 * result + (query != null ? query.hashCode() : 0);
    return result;
  }

  public boolean refersToParent() {
    if (expression != null && expression.refersToParent()) {
      return true;
    }
    return query != null && query.refersToParent();
  }

  public Identifier getVarName() {
    return varName;
  }

  public Expression getExpression() {
    return expression;
  }

  public Statement getQuery() {
    return query;
  }

  public void extractSubQueries(SubQueryCollector collector) {
    //this is to transform LET expressions with subqueries in simple LET, plus LET with query only, so the direct query is ignored
    if (expression != null) {
      expression.extractSubQueries(varName, collector);
    }
  }

  public Result serialize() {
    ResultInternal result = new ResultInternal();
    if (varName != null) {
      result.setProperty("varName", varName.serialize());
    }
    if (expression != null) {
      result.setProperty("expression", expression.serialize());
    }
    if (query != null) {
      result.setProperty("query", query.serialize());
    }

    return result;
  }

  public void deserialize(Result fromResult) {
    if (fromResult.getProperty("varName") != null) {
      varName = new Identifier(-1);
      Identifier.deserialize(fromResult.getProperty("varName"));
    }
    if (fromResult.getProperty("expression") != null) {
      expression = new Expression(-1);
      expression.deserialize(fromResult.getProperty("expression"));
    }
    if (fromResult.getProperty("query") != null) {
      query = Statement.deserializeFromOResult(fromResult.getProperty("expression"));
    }
  }

  public boolean isCacheable() {
    if (expression != null) {
      return expression.isCacheable();
    }
    if (query != null) {
      return expression.isCacheable();
    }

    return true;
  }
}
/* JavaCC - OriginalChecksum=bb3cd298d79f50d72f6842e6d6ea4fb2 (do not edit this line) */
