/*
 * Copyright (c) 2018 - Arcade Analytics LTD (https://arcadeanalytics.com)
 */

/* Generated By:JJTree: Do not edit this line. OMultExpression.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=true,NODE_PREFIX=O,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_USERTYPE_VISIBILITY_PUBLIC=true */
package com.arcadedb.sql.parser;

import java.util.Set;

public
class MultExpression extends MathExpression {

  public MultExpression(int id) {
    super(id);
  }

  public MultExpression(SqlParser p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(SqlParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }

  public boolean needsAliases(Set<String> aliases) {
    return super.needsAliases(aliases);
  }
}
/* JavaCC - OriginalChecksum=f75b8be48dca1e0cafae0cacadc608c8 (do not edit this line) */
