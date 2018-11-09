/*
 * Copyright (c) 2018 - Arcade Analytics LTD (https://arcadeanalytics.com)
 */

/* Generated By:JJTree: Do not edit this line. OSkip.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=true,NODE_PREFIX=O,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_USERTYPE_VISIBILITY_PUBLIC=true */
package com.arcadedb.sql.parser;

import com.arcadedb.exception.CommandExecutionException;
import com.arcadedb.sql.executor.CommandContext;
import com.arcadedb.sql.executor.Result;
import com.arcadedb.sql.executor.ResultInternal;

import java.util.Map;

public class Skip extends SimpleNode {

  protected PInteger num;

  protected InputParameter inputParam;

  public Skip(int id) {
    super(id);
  }

  public Skip(SqlParser p, int id) {
    super(p, id);
  }

  /**
   * Accept the visitor.
   **/
  public Object jjtAccept(SqlParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }

  public void toString(Map<Object, Object> params, StringBuilder builder) {
    if (num == null && inputParam == null) {
      return;
    }
    builder.append(" SKIP ");
    if (num != null) {
      num.toString(params, builder);
    } else {
      inputParam.toString(params, builder);
    }
  }

  public int getValue(CommandContext ctx) {
    if (num != null) {
      return num.getValue().intValue();
    }
    if (inputParam != null) {
      Object paramValue = inputParam.getValue(ctx.getInputParameters());
      if (paramValue instanceof Number) {
        return ((Number) paramValue).intValue();
      } else {
        throw new CommandExecutionException("Invalid value for SKIP: " + paramValue);
      }
    }
    throw new CommandExecutionException("No value for SKIP");
  }

  public Skip copy() {
    Skip result = new Skip(-1);
    result.num = num == null ? null : num.copy();
    result.inputParam = inputParam == null ? null : inputParam.copy();
    return result;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    Skip oSkip = (Skip) o;

    if (num != null ? !num.equals(oSkip.num) : oSkip.num != null)
      return false;
    return inputParam != null ? inputParam.equals(oSkip.inputParam) : oSkip.inputParam == null;
  }

  @Override
  public int hashCode() {
    int result = num != null ? num.hashCode() : 0;
    result = 31 * result + (inputParam != null ? inputParam.hashCode() : 0);
    return result;
  }

  public Result serialize() {
    ResultInternal result = new ResultInternal();
    if (num != null) {
      result.setProperty("num", num.serialize());
    }
    if (inputParam != null) {
      result.setProperty("inputParam", inputParam.serialize());
    }
    return result;
  }

  public void deserialize(Result fromResult) {
    if (fromResult.getProperty("num") != null) {
      num = new PInteger(-1);
      num.deserialize(fromResult.getProperty("num"));
    }
    if (fromResult.getProperty("inputParam") != null) {
      inputParam = InputParameter.deserializeFromOResult(fromResult.getProperty("inputParam"));
    }
  }
}
/* JavaCC - OriginalChecksum=8e13ca184705a8fc1b5939ecefe56a60 (do not edit this line) */
