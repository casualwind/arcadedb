/* Generated by: JJTree: Do not edit this line. IntValue.java Version 1.1 */
/* ParserGeneratorCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=true,NODE_PREFIX=,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package com.arcadedb.graphql.parser;

public class IntValue extends SimpleNode {

  protected int val;

  public IntValue(int id) {
    super(id);
  }

  public IntValue(GraphQLParser p, int id) {
    super(p, id);
  }

  /**
   * Accept the visitor.
   **/
  public Object jjtAccept(GraphQLParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }

  @Override
  public String toString() {
    return "IntValue{" + val + '}';
  }
}
/* ParserGeneratorCC - OriginalChecksum=7cc83806b39c0849299461073146a07c (do not edit this line) */
