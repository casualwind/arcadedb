/* Generated by: JJTree: Do not edit this line. ValueWithVariable.java Version 1.1 */
/* ParserGeneratorCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=true,NODE_PREFIX=,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package com.arcadedb.graphql.parser;

public class ValueWithVariable extends SimpleNode {

  protected VariableLiteral         variableLiteral;
  protected IntValue                intValue;
  protected FloatValue              floatValue;
  protected StringValue             stringValue;
  protected BooleanValue            booleanValue;
  protected EnumValue               enumValue;
  protected ListValueWithVariable   listValueWithVariable;
  protected ObjectValueWithVariable objectValueWithVariable;

  public ValueWithVariable(int id) {
    super(id);
  }

  public ValueWithVariable(GraphQLParser p, int id) {
    super(p, id);
  }

  /**
   * Accept the visitor.
   **/
  public Object jjtAccept(GraphQLParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}
/* ParserGeneratorCC - OriginalChecksum=7d715420456ed7cdb3c0875b52f6a13a (do not edit this line) */
