/*
 * Copyright (c) 2018 - Arcade Analytics LTD (https://arcadeanalytics.com)
 */

/* Generated By:JJTree: Do not edit this line. OInOperator.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=true,NODE_PREFIX=O,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_USERTYPE_VISIBILITY_PUBLIC=true */
package com.arcadedb.sql.parser;

import com.arcadedb.database.DatabaseInternal;

import java.util.Collection;
import java.util.Iterator;

public class InOperator extends SimpleNode implements BinaryCompareOperator {
  public InOperator(int id) {
    super(id);
  }

  public InOperator(SqlParser p, int id) {
    super(p, id);
  }

  /** Accept the visitor. **/
  public Object jjtAccept(SqlParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }

  @Override
  public boolean execute(DatabaseInternal database, Object left, Object right) {
    if (left == null) {
      return false;
    }
    if (right instanceof Collection) {
      if (left instanceof Collection) {
        return ((Collection) right).containsAll((Collection) left);
      }
      if (left instanceof Iterable) {
        left = ((Iterable) left).iterator();
      }
      if (left instanceof Iterator) {
        Iterator iterator = (Iterator) left;
        while (iterator.hasNext()) {
          Object next = iterator.next();
          if (!((Collection) right).contains(next)) {
            return false;
          }
        }
      }
      return ((Collection) right).contains(left);
    }
    if (right instanceof Iterable) {
      right = ((Iterable) right).iterator();
    }
    if (right instanceof Iterator) {
      if (left instanceof Iterable) {
        left = ((Iterable) left).iterator();
      }
      Iterator leftIterator = (Iterator) left;
      Iterator rightIterator = (Iterator) right;
      while (leftIterator.hasNext()) {
        Object leftItem = leftIterator.next();
        boolean found = false;
        while (rightIterator.hasNext()) {
          Object rightItem = rightIterator.next();
          if (leftItem != null && leftItem.equals(rightItem)) {
            found = true;
            break;
          }
        }
        if (!found) {
          return false;
        }
      }
      return true;
    }
    return false;
  }

  @Override public boolean supportsBasicCalculation() {
    return true;
  }

  @Override public InOperator copy() {
    return this;
  }

  @Override public boolean equals(Object obj) {
    return obj != null && obj.getClass().equals(this.getClass());
  }

  @Override public int hashCode() {
    return getClass().hashCode();
  }
}
/* JavaCC - OriginalChecksum=6650a720cb942fa3c4d588ff0f381b3a (do not edit this line) */
