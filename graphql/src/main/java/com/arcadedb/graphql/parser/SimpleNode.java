/* Generated by: JJTree: Do not edit this line. SimpleNode.java Version 1.1 */
/* ParserGeneratorCCOptions:MULTI=false,NODE_USES_PARSER=false,VISITOR=false,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package com.arcadedb.graphql.parser;

import com.arcadedb.query.sql.parser.SqlParserVisitor;

public class SimpleNode implements Node {
  protected Node          parent;
  protected Node[]        children;
  protected int           id;
  protected Object        value;
  protected GraphQLParser parser;
  protected Token         firstToken;
  protected Token         lastToken;

  public SimpleNode(final int i) {
    id = i;
  }

  public SimpleNode(final GraphQLParser p, final int i) {
    this(i);
    parser = p;
  }

  public void jjtOpen() {
  }

  public void jjtClose() {
  }

  public void jjtSetParent(final Node n) {
    parent = n;
  }

  public Node jjtGetParent() {
    return parent;
  }

  public void jjtAddChild(final Node n, final int i) {
    if (children == null) {
      children = new Node[i + 1];
    } else if (i >= children.length) {
      Node c[] = new Node[i + 1];
      System.arraycopy(children, 0, c, 0, children.length);
      children = c;
    }
    children[i] = n;
  }

  public Node jjtGetChild(final int i) {
    return children[i];
  }

  public int jjtGetNumChildren() {
    return children == null ? 0 : children.length;
  }

  public void jjtSetValue(final Object aValue) {
    value = aValue;
  }

  public Object jjtGetValue() {
    return value;
  }

  /* You can override these two methods in subclasses of SimpleNode to
     customize the way the node appears when the tree is dumped.  If
     your output uses more than one line you should override
     toString(String), otherwise overriding toString() is probably all
     you need to do. */
  @Override
  public String toString() {
    return GraphQLParserTreeConstants.jjtNodeName[id];
  }

  public String toString(String prefix) {
    return prefix + toString();
  }

  protected void dumpString(String s) {
    // TODO get rid of this
    System.out.println(s);
  }

  /* Override this method if you want to customize how the node dumps
     out its children. */
  public void dump(String prefix) {
    dumpString(toString(prefix));
    if (children != null) {
      for (int i = 0; i < children.length; ++i) {
        SimpleNode n = (SimpleNode) children[i];
        if (n != null)
          n.dump(prefix + " ");
      }
    }
  }

  public int getId() {
    return id;
  }

  public void jjtSetFirstToken(Token token) {
    this.firstToken = token;
  }

  public void jjtSetLastToken(Token token) {
    this.lastToken = token;
  }

  public Object childrenAccept(SqlParserVisitor visitor, Object data) {
    if (children != null) {
      for (int i = 0; i < children.length; ++i) {
//        children[i].jjtAccept(visitor, data);//TODO fix
      }
    }
    return data;
  }
}

/* ParserGeneratorCC - OriginalChecksum=a932dd6e6d33187f6cbda5ebe13d5edb (do not edit this line) */
