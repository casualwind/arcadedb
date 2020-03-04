/*
 * Copyright (c) - Arcade Data LTD (https://arcadedata.com)
 */

/* Generated By:JJTree: Do not edit this line. ONeOperator.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=true,NODE_PREFIX=O,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_USERTYPE_VISIBILITY_PUBLIC=true */
package com.arcadedb.sql.parser;

import com.arcadedb.database.DatabaseInternal;
import com.arcadedb.sql.executor.OQueryOperatorEquals;

public
class NeOperator extends SimpleNode implements BinaryCompareOperator {
  public NeOperator(int id) {
    super(id);
  }

  public NeOperator(SqlParser p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(SqlParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }

  @Override public boolean execute(DatabaseInternal database, Object left, Object right) {
    return !OQueryOperatorEquals.equals(left, right);
  }

  @Override public String toString() {
    return "!=";
  }

  @Override public boolean supportsBasicCalculation() {
    return true;
  }

  @Override public NeOperator copy() {
    return this;
  }

  @Override public boolean equals(Object obj) {
    return obj != null && obj.getClass().equals(this.getClass());
  }

  @Override public int hashCode() {
    return getClass().hashCode();
  }
}
/* JavaCC - OriginalChecksum=ac0ae426fb86c930dea83013ddc202ba (do not edit this line) */
