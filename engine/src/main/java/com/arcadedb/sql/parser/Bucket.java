/*
 * Copyright (c) 2018 - Arcade Analytics LTD (https://arcadeanalytics.com)
 */

/* Generated By:JJTree: Do not edit this line. OCluster.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=true,NODE_PREFIX=O,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_USERTYPE_VISIBILITY_PUBLIC=true */
package com.arcadedb.sql.parser;

import com.arcadedb.sql.executor.Result;
import com.arcadedb.sql.executor.ResultInternal;

import java.util.Map;

public class Bucket extends SimpleNode {
  protected String  bucketName;
  protected Integer bucketNumber;

  public Bucket(String bucketName) {
    super(-1);
    this.bucketName = bucketName;
  }
  public Bucket(int id) {
    super(id);
  }

  public Bucket(SqlParser p, int id) {
    super(p, id);
  }

  /**
   * Accept the visitor.
   **/
  public Object jjtAccept(SqlParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }

  @Override
  public String toString(String prefix) {
    return super.toString(prefix);
  }

  public void toString(Map<Object, Object> params, StringBuilder builder) {
    if (bucketName != null) {
      builder.append("bucket:" + bucketName);
    } else {
      builder.append("bucket:" + bucketNumber);
    }
  }

  public String getBucketName() {
    return bucketName;
  }

  public Integer getBucketNumber() {
    return bucketNumber;
  }

  public Bucket copy() {
    Bucket result = new Bucket(-1);
    result.bucketName = bucketName;
    result.bucketNumber = bucketNumber;
    return result;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    Bucket oCluster = (Bucket) o;

    if (bucketName != null ? !bucketName.equals(oCluster.bucketName) : oCluster.bucketName != null)
      return false;
    return bucketNumber != null ? bucketNumber.equals(oCluster.bucketNumber) : oCluster.bucketNumber == null;
  }

  @Override
  public int hashCode() {
    int result = bucketName != null ? bucketName.hashCode() : 0;
    result = 31 * result + (bucketNumber != null ? bucketNumber.hashCode() : 0);
    return result;
  }

  public Result serialize() {
    ResultInternal result = new ResultInternal();
    result.setProperty("bucketName", bucketName);
    result.setProperty("bucketNumber", bucketNumber);
    return result;
  }

  public void deserialize(Result fromResult) {
    bucketName = fromResult.getProperty("bucketName");
    bucketNumber = fromResult.getProperty("bucketNumber");
  }
}
/* JavaCC - OriginalChecksum=d27abf009fe7db482fbcaac9d52ba192 (do not edit this line) */
