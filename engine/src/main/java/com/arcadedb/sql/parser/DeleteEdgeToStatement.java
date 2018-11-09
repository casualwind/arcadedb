/*
 * Copyright (c) 2018 - Arcade Analytics LTD (https://arcadeanalytics.com)
 */

/* Generated By:JJTree: Do not edit this line. ODeleteEdgeToStatement.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=true,NODE_PREFIX=O,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_USERTYPE_VISIBILITY_PUBLIC=true */
package com.arcadedb.sql.parser;

public
class DeleteEdgeToStatement extends DeleteEdgeStatement {
  public DeleteEdgeToStatement(int id) {
    super(id);
  }

  public DeleteEdgeToStatement(SqlParser p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(SqlParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }

  @Override public DeleteEdgeStatement copy() {
    return super.copy();
  }
}
/* JavaCC - OriginalChecksum=8b71c6e3bc7262af9a8e0e0ea3a1964c (do not edit this line) */
