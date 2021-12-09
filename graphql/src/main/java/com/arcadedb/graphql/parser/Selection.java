/* Generated by: JJTree: Do not edit this line. Selection.java Version 1.1 */
/* ParserGeneratorCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=true,NODE_PREFIX=,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package com.arcadedb.graphql.parser;

public class Selection extends SimpleNode {

  protected Name           name;
  protected FieldWithAlias fieldWithAlias;
  protected Field          field;
  protected boolean        ellipsis = false;

  protected FragmentSpread fragmentSpread;

  protected InlineFragment inlineFragment;

  public Selection(int id) {
    super(id);
  }

  public Selection(GraphQLParser p, int id) {
    super(p, id);
  }

  /**
   * Accept the visitor.
   **/
  public Object jjtAccept(GraphQLParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}
/* ParserGeneratorCC - OriginalChecksum=aac9a2d576730b830f5ef7c02bdf7951 (do not edit this line) */
