/*
 * Copyright (c) - Arcade Data LTD (https://arcadedata.com)
 */

/* Generated By:JJTree: Do not edit this line. OOutPathItem.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=true,NODE_PREFIX=O,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_USERTYPE_VISIBILITY_PUBLIC=true */
package com.arcadedb.sql.parser;

import com.arcadedb.sql.executor.Result;

import java.util.Map;

public
class OutPathItem extends MatchPathItem {
  public OutPathItem(int id) {
    super(id);
  }

  public OutPathItem(SqlParser p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(SqlParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }

  @Override
  public void toString(Map<Object, Object> params, StringBuilder builder) {
    builder.append("-");
    boolean first = true;
    if (this.method.params != null) {
      for (Expression exp : this.method.params) {
        if (!first) {
          builder.append(", ");
        }
        builder.append(exp.execute((Result) null, null));
        first = false;
      }
    }
    builder.append("->");
    if (filter != null) {
      filter.toString(params, builder);
    }
  }
}
/* JavaCC - OriginalChecksum=b9cd4c40325a129d9166b281866b7a34 (do not edit this line) */
