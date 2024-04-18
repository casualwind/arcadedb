/*
 * Copyright 2023 Arcade Data Ltd
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.arcadedb.query.sql.method.conversion;

import com.arcadedb.database.Document;
import com.arcadedb.database.Identifiable;
import com.arcadedb.query.sql.executor.CommandContext;
import com.arcadedb.query.sql.executor.MultiValue;
import com.arcadedb.query.sql.executor.Result;
import com.arcadedb.query.sql.method.AbstractSQLMethod;
import com.arcadedb.serializer.json.JSONArray;
import com.arcadedb.serializer.json.JSONObject;

import java.util.*;

/**
 * Converts a document in JSON string.
 *
 * @author Johann Sorel (Geomatys)
 * @author Luca Garulli (l.garulli--(at)--gmail.com)
 */
public class SQLMethodAsJSON extends AbstractSQLMethod {

  public static final String NAME = "asjson";

  public SQLMethodAsJSON() {
    super(NAME, 0, 0);
  }

  // TEMPORARY TO SUPPORT DEPRECATION OF LEGACY TOJSON METHOD
  protected SQLMethodAsJSON(String name) {
    super(name, 0, 0);
  }

  @Override
  public String getSyntax() {
    return "asJSON()";
  }

  @Override
  public Object execute(final Object me, final Identifiable iCurrentRecord, final CommandContext iContext,
      final Object[] iParams) {
    if (me == null)
      return null;

    if (me instanceof Result) {
      return ((Result) me).toJSON();
    } else if (me instanceof Document) {
      return ((Document) me).toJSON();
    } else if (me instanceof Map) {
      return new JSONObject((Map) me);
    } else if (me instanceof String) {
      if (((String) me).isEmpty())
        return new JSONObject();
      if (((String) me).charAt(0) == '[')
        return new JSONArray((String) me);
      else
        return new JSONObject((String) me);
    } else if (MultiValue.isMultiValue(me)) {
      final JSONArray json = new JSONArray();
      for (final Object o : MultiValue.getMultiValueIterable(me, false))
        json.put(execute(o, iCurrentRecord, iContext, iParams));
      return json;
    }
    return null;
  }
}
