/*
 * Copyright (c) - Arcade Data LTD (https://arcadedata.com)
 */

/* Generated By:JJTree: Do not edit this line. OCreateVertexNoTargetStatement.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=true,NODE_PREFIX=O,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_USERTYPE_VISIBILITY_PUBLIC=true */
package com.arcadedb.sql.parser;

public
class CreateVertexNoTargetStatement extends CreateVertexStatement {
  public CreateVertexNoTargetStatement(int id) {
    super(id);
  }

  public CreateVertexNoTargetStatement(SqlParser p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(SqlParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }

  @Override public CreateVertexStatement copy() {
    return super.copy();
  }
}
/* JavaCC - OriginalChecksum=25d9cdfd149e7b374a84dfd166e11306 (do not edit this line) */
