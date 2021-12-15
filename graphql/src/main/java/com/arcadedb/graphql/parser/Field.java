/* Generated by: JJTree: Do not edit this line. Field.java Version 1.1 */
/* ParserGeneratorCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=true,NODE_PREFIX=,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package com.arcadedb.graphql.parser;

public class Field extends SimpleNode {

  protected Name         name;
  protected Arguments    arguments;
  protected Directives   directives;
  protected SelectionSet selectionSet;

  public Field(Name name, int line, int column, int tokenId) {
    this(-1);
    this.name = name;
  }

  public Field(int id) {
    super(id);
  }

  public Field(GraphQLParser p, int id) {
    super(p, id);
  }

  /**
   * Accept the visitor.
   **/
  public Object jjtAccept(GraphQLParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }

  public Arguments getArguments() {
    return arguments;
  }

  public String getName() {
    return name != null ? name.value : null;
  }

  public SelectionSet getSelectionSet() {
    return selectionSet;
  }

  public Directives getDirectives() {
    return directives;
  }
}
/* ParserGeneratorCC - OriginalChecksum=2b182b10a025776d444c1f179f3e7ff4 (do not edit this line) */
