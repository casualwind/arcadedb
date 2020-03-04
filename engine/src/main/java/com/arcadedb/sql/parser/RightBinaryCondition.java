/*
 * Copyright (c) - Arcade Data LTD (https://arcadedata.com)
 */

/* Generated By:JJTree: Do not edit this line. ORightBinaryCondition.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=true,NODE_PREFIX=O,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_USERTYPE_VISIBILITY_PUBLIC=true */
package com.arcadedb.sql.parser;

import com.arcadedb.database.Identifiable;
import com.arcadedb.exception.CommandExecutionException;
import com.arcadedb.sql.executor.CommandContext;
import com.arcadedb.sql.executor.Result;
import com.arcadedb.sql.executor.ResultInternal;

import java.util.*;

public class RightBinaryCondition extends SimpleNode {

  BinaryCompareOperator operator;

  boolean    not = false;
  InOperator inOperator;

  Expression right;

  public RightBinaryCondition(int id) {
    super(id);
  }

  public RightBinaryCondition(SqlParser p, int id) {
    super(p, id);
  }

  @Override
  public RightBinaryCondition copy() {
    RightBinaryCondition result = new RightBinaryCondition(-1);
    result.operator = operator == null ? null : operator.copy();
    result.not = not;
    result.inOperator = inOperator == null ? null : inOperator.copy();
    result.right = right == null ? null : right.copy();
    return result;
  }

  @Override
  public void toString(Map<Object, Object> params, StringBuilder builder) {
    if (operator != null) {
      builder.append(operator.toString());
      builder.append(" ");
      right.toString(params, builder);
    } else if (inOperator != null) {
      if (not) {
        builder.append("NOT ");
      }
      builder.append("IN ");
      right.toString(params, builder);
    }
  }

  public Object execute(Result iCurrentRecord, Object elementToFilter, CommandContext ctx) {
    if (elementToFilter == null) {
      return null;
    }
    Iterator iterator;
    if (elementToFilter instanceof Identifiable) {
      iterator = Collections.singleton(elementToFilter).iterator();
    } else if (elementToFilter instanceof Iterable) {
      iterator = ((Iterable) elementToFilter).iterator();
    } else if (elementToFilter instanceof Iterator) {
      iterator = (Iterator) elementToFilter;
    } else {
      iterator = Collections.singleton(elementToFilter).iterator();
    }

    List result = new ArrayList();
    while (iterator.hasNext()) {
      Object element = iterator.next();
      if (matchesFilters(iCurrentRecord, element, ctx)) {
        result.add(element);
      }
    }
    return result;
  }

  public Object execute(Identifiable iCurrentRecord, Object elementToFilter, CommandContext ctx) {
    if (elementToFilter == null) {
      return null;
    }
    Iterator iterator;
    if (elementToFilter instanceof Identifiable) {
      iterator = Collections.singleton(elementToFilter).iterator();
    } else if (elementToFilter instanceof Iterable) {
      iterator = ((Iterable) elementToFilter).iterator();
    } else if (elementToFilter instanceof Iterator) {
      iterator = (Iterator) elementToFilter;
    } else {
      iterator = Collections.singleton(elementToFilter).iterator();
    }

    List result = new ArrayList();
    while (iterator.hasNext()) {
      Object element = iterator.next();
      if (matchesFilters(iCurrentRecord, element, ctx)) {
        result.add(element);
      }
    }
    return result;
  }

  private boolean matchesFilters(final Identifiable iCurrentRecord, final Object element, final CommandContext ctx) {
    if (operator != null) {
      operator.execute(ctx.getDatabase(), element, right.execute(iCurrentRecord, ctx));
    } else if (inOperator != null) {

      final Object rightVal = evaluateRight(iCurrentRecord, ctx);
      if (rightVal == null) {
        return false;
      }
      boolean result = InCondition.evaluateExpression(element, rightVal);
      if (not) {
        result = !result;
      }
      return result;
    }
    return false;
  }

  private boolean matchesFilters(final Result iCurrentRecord, final Object element, final CommandContext ctx) {
    if (operator != null) {
      return operator.execute(ctx.getDatabase(), element, right.execute(iCurrentRecord, ctx));
    } else if (inOperator != null) {

      final Object rightVal = evaluateRight(iCurrentRecord, ctx);
      if (rightVal == null) {
        return false;
      }
      boolean result = InCondition.evaluateExpression(element, rightVal);
      if (not) {
        result = !result;
      }
      return result;
    }
    return false;
  }

  public Object evaluateRight(Identifiable currentRecord, CommandContext ctx) {
    return right.execute(currentRecord, ctx);
  }

  public Object evaluateRight(Result currentRecord, CommandContext ctx) {
    return right.execute(currentRecord, ctx);
  }

  public boolean needsAliases(Set<String> aliases) {
    return right != null && right.needsAliases(aliases);
  }

  public void extractSubQueries(SubQueryCollector collector) {
    if (right != null) {
      right.extractSubQueries(collector);
    }
  }

  public boolean refersToParent() {
    return right != null && right.refersToParent();
  }

  public Result serialize() {

    ResultInternal result = new ResultInternal();
    result.setProperty("operator", operator.getClass().getName());
    result.setProperty("not", not);
    result.setProperty("in", inOperator != null);
    result.setProperty("right", right.serialize());
    return result;
  }

  public void deserialize(Result fromResult) {
    try {
      operator = (BinaryCompareOperator) Class.forName(String.valueOf(fromResult.getProperty("operator"))).newInstance();
    } catch (Exception e) {
      throw new CommandExecutionException(e);
    }
    not = fromResult.getProperty("not");
    if (Boolean.TRUE.equals(fromResult.getProperty("in"))) {
      inOperator = new InOperator(-1);
    }
    right = new Expression(-1);
    right.deserialize(fromResult.getProperty("right"));
  }
}
/* JavaCC - OriginalChecksum=29d59ae04778eb611547292a27863da4 (do not edit this line) */
