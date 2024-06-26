package com.arcadedb.function.sql;/*
 * Copyright © 2021-present Arcade Data Ltd (info@arcadedata.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.arcadedb.database.Database;
import com.arcadedb.function.FunctionLibraryDefinition;

import java.util.*;
import java.util.concurrent.*;

public class SQLFunctionLibraryDefinition implements FunctionLibraryDefinition<SQLFunctionDefinition> {
  protected final Database                                     database;
  protected final String                                       libraryName;
  protected final ConcurrentMap<String, SQLFunctionDefinition> functions = new ConcurrentHashMap<>();

  public SQLFunctionLibraryDefinition(final Database database, final String libraryName) {
    this.database = database;
    this.libraryName = libraryName;
  }

  public SQLFunctionLibraryDefinition registerFunction(final SQLFunctionDefinition function) {
    if (functions.putIfAbsent(function.getName(), function) != null)
      throw new IllegalArgumentException("Function '" + function.getName() + "' already defined in library '" + libraryName + "'");

    // REGISTER ALL THE FUNCTIONS UNDER THE NEW ENGINE INSTANCE

    return this;
  }

  @Override
  public SQLFunctionLibraryDefinition unregisterFunction(final String functionName) {
    functions.remove(functionName);
    return this;
  }

  @Override
  public String getName() {
    return libraryName;
  }

  @Override
  public Iterable<SQLFunctionDefinition> getFunctions() {
    return Collections.unmodifiableCollection(functions.values());
  }

  @Override
  public boolean hasFunction(final String functionName) {
    return functions.containsKey(functionName);
  }

  @Override
  public SQLFunctionDefinition getFunction(final String functionName) {
    final SQLFunctionDefinition f = functions.get(functionName);
    if (f == null)
      throw new IllegalArgumentException("Function '" + functionName + "' not defined");
    return f;
  }
}
