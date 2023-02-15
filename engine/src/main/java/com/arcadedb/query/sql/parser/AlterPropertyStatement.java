/*
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
 *
 * SPDX-FileCopyrightText: 2021-present Arcade Data Ltd (info@arcadedata.com)
 * SPDX-License-Identifier: Apache-2.0
 */
/* Generated By:JJTree: Do not edit this line. OAlterPropertyStatement.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=true,NODE_PREFIX=O,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_USERTYPE_VISIBILITY_PUBLIC=true */
package com.arcadedb.query.sql.parser;

import com.arcadedb.database.Database;
import com.arcadedb.database.Identifiable;
import com.arcadedb.exception.CommandExecutionException;
import com.arcadedb.query.sql.executor.CommandContext;
import com.arcadedb.query.sql.executor.InternalResultSet;
import com.arcadedb.query.sql.executor.ResultInternal;
import com.arcadedb.query.sql.executor.ResultSet;
import com.arcadedb.schema.DocumentType;
import com.arcadedb.schema.Property;

import java.util.*;

public class AlterPropertyStatement extends DDLStatement {
  public Expression settingValue;
  Identifier typeName;
  Identifier propertyName;
  Identifier customPropertyName;
  Expression customPropertyValue;
  Identifier settingName;

  public AlterPropertyStatement(final int id) {
    super(id);
  }

  @Override
  public ResultSet executeDDL(final CommandContext context) {
    final Database db = context.getDatabase();
    final DocumentType typez = db.getSchema().getType(typeName.getStringValue());

    if (typez == null)
      throw new CommandExecutionException("Invalid type name or type not found: " + typez);

    final Property property = typez.getProperty(propertyName.getStringValue());
    if (property == null)
      throw new CommandExecutionException("Property '" + propertyName + "' not found on type " + typez);

    final ResultInternal result = new ResultInternal();
    result.setProperty("type", typeName.getStringValue());
    result.setProperty("property", propertyName.getStringValue());

    if (customPropertyName != null) {
      final String customName = customPropertyName.getStringValue();
      final Object oldValue = property.getCustomValue(customName);
      final Object finalValue = customPropertyValue.execute((Identifiable) null, context);
      property.setCustomValue(customName, finalValue);

      result.setProperty("operation", "alter property custom");
      result.setProperty("customAttribute", customPropertyName.getStringValue());
      result.setProperty("oldValue", oldValue);
      result.setProperty("newValue", finalValue);
    } else if (settingName != null) {
      final String setting = settingName.getStringValue().toLowerCase();
      final Object finalValue = settingValue.execute((Identifiable) null, context);

      final Object oldValue;

      if (setting.equalsIgnoreCase("readonly")) {
        oldValue = property.isReadonly();
        property.setReadonly((boolean) finalValue);
      } else if (setting.equalsIgnoreCase("mandatory")) {
        oldValue = property.isMandatory();
        property.setMandatory((boolean) finalValue);
      } else if (setting.equalsIgnoreCase("notnull")) {
        oldValue = property.isNotNull();
        property.setNotNull((boolean) finalValue);
      } else if (setting.equalsIgnoreCase("max")) {
        oldValue = property.getMax();
        property.setMax("" + finalValue);
      } else if (setting.equalsIgnoreCase("min")) {
        oldValue = property.getMin();
        property.setMin("" + finalValue);
      } else if (setting.equalsIgnoreCase("default")) {
        oldValue = property.getDefaultValue();
        property.setDefaultValue(settingValue.toString());
      } else if (setting.equalsIgnoreCase("regexp")) {
        oldValue = property.getRegexp();
        property.setRegexp("" + finalValue);
      } else {
        throw new CommandExecutionException("Setting '" + setting + "' not supported");
      }

      result.setProperty("operation", "alter property");
      result.setProperty("attribute", setting);
      result.setProperty("oldValue", oldValue);
      result.setProperty("newValue", finalValue);
    } else
      throw new CommandExecutionException("Property '" + property + "' not found on type '" + typez + "'");

    final InternalResultSet rs = new InternalResultSet();
    rs.add(result);
    return rs;
  }

  @Override
  public void toString(final Map<String, Object> params, final StringBuilder builder) {
    builder.append("ALTER PROPERTY ");
    typeName.toString(params, builder);
    builder.append(".");
    propertyName.toString(params, builder);
    if (customPropertyName != null) {
      builder.append(" CUSTOM ");
      customPropertyName.toString(params, builder);
      builder.append(" = ");
      customPropertyValue.toString(params, builder);
    } else {
      builder.append(" ");
      settingName.toString(params, builder);
      builder.append(" ");
      settingValue.toString(params, builder);
    }
  }

  @Override
  public AlterPropertyStatement copy() {
    final AlterPropertyStatement result = new AlterPropertyStatement(-1);
    result.typeName = typeName == null ? null : typeName.copy();
    result.propertyName = propertyName == null ? null : propertyName.copy();
    result.customPropertyName = customPropertyName == null ? null : customPropertyName.copy();
    result.customPropertyValue = customPropertyValue == null ? null : customPropertyValue.copy();
    result.settingName = settingName == null ? null : settingName.copy();
    result.settingValue = settingValue == null ? null : settingValue.copy();
    return result;
  }

  @Override
  protected Object[] getIdentityElements() {
    return new Object[] { typeName, propertyName, customPropertyName, customPropertyValue, settingName, settingValue };
  }
}
/* JavaCC - OriginalChecksum=2421f6ad3b5f1f8e18149650ff80f1e7 (do not edit this line) */
