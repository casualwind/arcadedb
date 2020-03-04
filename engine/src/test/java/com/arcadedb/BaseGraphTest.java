/*
 * Copyright (c) - Arcade Data LTD (https://arcadedata.com)
 */

package com.arcadedb;

import com.arcadedb.database.Database;
import com.arcadedb.database.RID;
import com.arcadedb.graph.ImmutableLightEdge;
import com.arcadedb.graph.MutableEdge;
import com.arcadedb.graph.MutableVertex;
import com.arcadedb.utility.FileUtils;
import org.junit.jupiter.api.Assertions;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public abstract class BaseGraphTest extends BaseTest {
  protected static final String VERTEX1_TYPE_NAME = "V1";
  protected static final String VERTEX2_TYPE_NAME = "V2";
  protected static final String EDGE1_TYPE_NAME   = "E1";
  protected static final String EDGE2_TYPE_NAME   = "E2";
  protected static final String DB_PATH           = "target/database/graph";

  protected static RID root;

  @Override
  public void beginTest() {
    FileUtils.deleteRecursively(new File(DB_PATH));

    database.transaction(new Database.TransactionScope() {
      @Override
      public void execute(Database database) {
        Assertions.assertFalse(database.getSchema().existsType(VERTEX1_TYPE_NAME));
        database.getSchema().createVertexType(VERTEX1_TYPE_NAME, 3);

        Assertions.assertFalse(database.getSchema().existsType(VERTEX2_TYPE_NAME));
        database.getSchema().createVertexType(VERTEX2_TYPE_NAME, 3);

        database.getSchema().createEdgeType(EDGE1_TYPE_NAME);
        database.getSchema().createEdgeType(EDGE2_TYPE_NAME);
      }
    });

    final Database db = database;
    db.begin();

    final MutableVertex v1 = db.newVertex(VERTEX1_TYPE_NAME);
    v1.set("name", VERTEX1_TYPE_NAME);
    v1.save();

    final MutableVertex v2 = db.newVertex(VERTEX2_TYPE_NAME);
    v2.set("name", VERTEX2_TYPE_NAME);
    v2.save();

    // CREATION OF EDGE PASSING PARAMS AS VARARGS
    MutableEdge e1 = v1.newEdge(EDGE1_TYPE_NAME, v2, true, "name", "E1");
    Assertions.assertEquals(e1.getOut(), v1);
    Assertions.assertEquals(e1.getIn(), v2);

    final MutableVertex v3 = db.newVertex(VERTEX2_TYPE_NAME);
    v3.set("name", "V3");
    v3.save();

    Map<String, Object> params = new HashMap<>();
    params.put("name", "E2");

    // CREATION OF EDGE PASSING PARAMS AS MAP
    MutableEdge e2 = v2.newEdge(EDGE2_TYPE_NAME, v3, true, params);
    Assertions.assertEquals(e2.getOut(), v2);
    Assertions.assertEquals(e2.getIn(), v3);

    ImmutableLightEdge e3 = v1.newLightEdge(EDGE2_TYPE_NAME, v3, true);
    Assertions.assertEquals(e3.getOut(), v1);
    Assertions.assertEquals(e3.getIn(), v3);

    db.commit();

    root = v1.getIdentity();
  }
}