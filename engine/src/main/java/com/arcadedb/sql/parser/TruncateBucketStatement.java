/*
 * Copyright (c) - Arcade Data LTD (https://arcadedata.com)
 */

/* Generated By:JJTree: Do not edit this line. OTruncateClusterStatement.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=true,NODE_PREFIX=O,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_USERTYPE_VISIBILITY_PUBLIC=true */
package com.arcadedb.sql.parser;

import com.arcadedb.sql.executor.CommandContext;
import com.arcadedb.sql.executor.ResultSet;

import java.util.Map;

public class TruncateBucketStatement extends ODDLStatement {

  public Identifier bucketName;
  public PInteger   bucketNumber;
  public boolean    unsafe = false;

  public TruncateBucketStatement(int id) {
    super(id);
  }

  public TruncateBucketStatement(SqlParser p, int id) {
    super(p, id);
  }

  @Override public ResultSet executeDDL(CommandContext ctx) {
//    ODatabaseDocumentAbstract database = (ODatabaseDocumentAbstract) ctx.getDatabase();
//    OInternalResultSet rs = new OInternalResultSet();
//
//    Integer bucketId = null;
//    if (clusterNumber != null) {
//      bucketId = clusterNumber.getValue().intValue();
//    } else {
//      bucketId = database.getClusterIdByName(bucketName.getStringValue());
//    }
//
//    if (bucketId < 0) {
//      throw new ODatabaseException("Cluster with name " + bucketName + " does not exist");
//    }
//
//    final OSchema schema = database.getMetadata().getSchema();
//    final OClass typez = schema.getClassByClusterId(bucketId);
//    if (typez == null) {
//      final OStorage storage = database.getStorage();
//      final com.orientechnologies.orient.core.storage.OCluster bucket = storage.getClusterById(bucketId);
//
//      if (bucket == null) {
//        throw new ODatabaseException("Cluster with name " + bucketName + " does not exist");
//      }
//
//      try {
//        database.checkForClusterPermissions(bucket.getName());
//        bucket.truncate();
//      } catch (IOException ioe) {
//        throw OException.wrapException(new ODatabaseException("Error during truncation of bucket with name " + bucketName), ioe);
//      }
//    } else {
//      String name = database.getClusterNameById(bucketId);
//      typez.truncateCluster(name);
//    }
//
//    OResultInternal result = new OResultInternal();
//    result.setProperty("operation", "truncate cluster");
//    if (bucketName != null) {
//      result.setProperty("bucketName", bucketName.getStringValue());
//    }
//    result.setProperty("bucketId", bucketId);
//
//    rs.add(result);
//    return rs;
    throw new UnsupportedOperationException();
  }

  /**
   * Accept the visitor.
   **/
  public Object jjtAccept(SqlParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }

  @Override public void toString(Map<Object, Object> params, StringBuilder builder) {
    builder.append("TRUNCATE BUCKET ");
    if (bucketName != null) {
      bucketName.toString(params, builder);
    } else if (bucketNumber != null) {
      bucketNumber.toString(params, builder);
    }
    if (unsafe) {
      builder.append(" UNSAFE");
    }
  }

  @Override public TruncateBucketStatement copy() {
    TruncateBucketStatement result = new TruncateBucketStatement(-1);
    result.bucketName = bucketName == null ? null : bucketName.copy();
    result.bucketNumber = bucketNumber == null ? null : bucketNumber.copy();
    result.unsafe = unsafe;
    return result;
  }

  @Override public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    TruncateBucketStatement that = (TruncateBucketStatement) o;

    if (unsafe != that.unsafe)
      return false;
    if (bucketName != null ? !bucketName.equals(that.bucketName) : that.bucketName != null)
      return false;
    return bucketNumber != null ? bucketNumber.equals(that.bucketNumber) : that.bucketNumber == null;
  }

  @Override public int hashCode() {
    int result = bucketName != null ? bucketName.hashCode() : 0;
    result = 31 * result + (bucketNumber != null ? bucketNumber.hashCode() : 0);
    result = 31 * result + (unsafe ? 1 : 0);
    return result;
  }
}
/* JavaCC - OriginalChecksum=301f993f6ba2893cb30c8f189674b974 (do not edit this line) */
