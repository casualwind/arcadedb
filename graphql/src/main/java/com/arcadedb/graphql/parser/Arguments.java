/* Generated by: JJTree: Do not edit this line. Arguments.java Version 1.1 */
/* ParserGeneratorCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=true,NODE_PREFIX=,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package com.arcadedb.graphql.parser;

import java.util.*;

public class Arguments extends SimpleNode {
  List<Argument> arguments = new ArrayList<>();

  public Arguments(int id) {
    super(id);
  }

  public Arguments(GraphQLParser p, int id) {
    super(p, id);
  }

  /**
   * Accept the visitor.
   **/
  public Object jjtAccept(GraphQLParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}
/* ParserGeneratorCC - OriginalChecksum=262d81780905472eac1cf2b9e5525261 (do not edit this line) */
