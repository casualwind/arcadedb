/* Generated By:JJTree: Do not edit this line. OCreateFunctionStatement.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=true,NODE_PREFIX=O,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package com.arcadedb.query.sql.parser;

import com.arcadedb.database.Database;
import com.arcadedb.exception.CommandSQLParsingException;
import com.arcadedb.function.FunctionDefinition;
import com.arcadedb.function.FunctionLibraryDefinition;
import com.arcadedb.function.polyglot.JavascriptFunctionDefinition;
import com.arcadedb.function.polyglot.JavascriptFunctionLibraryDefinition;
import com.arcadedb.function.sql.SQLFunctionDefinition;
import com.arcadedb.function.sql.SQLFunctionLibraryDefinition;
import com.arcadedb.query.sql.executor.CommandContext;
import com.arcadedb.query.sql.executor.InternalResultSet;
import com.arcadedb.query.sql.executor.ResultInternal;
import com.arcadedb.query.sql.executor.ResultSet;

import java.util.*;
import java.util.stream.*;

public class DefineFunctionStatement extends SimpleExecStatement {
  protected Identifier       libraryName;
  protected Identifier       functionName;
  protected String           codeQuoted;
  protected String           code;
  protected List<Identifier> parameters;
  protected Identifier       language;

  public DefineFunctionStatement(final int id) {
    super(id);
  }

  @Override
  public ResultSet executeSimple(final CommandContext context) {
    final Database database = context.getDatabase();

    final FunctionLibraryDefinition fLib;
    if (!database.getSchema().hasFunctionLibrary(libraryName.getStringValue())) {
      switch (language.getStringValue()) {
      case "js":
        fLib = new JavascriptFunctionLibraryDefinition(database, libraryName.getStringValue());
        break;

      case "sql":
        fLib = new SQLFunctionLibraryDefinition(database, libraryName.getStringValue());
        break;

      default:
        throw new CommandSQLParsingException(
            "Error on function creation: language '" + language.getStringValue() + "' not supported");
      }

      database.getSchema().registerFunctionLibrary(fLib);
    } else
      fLib = database.getSchema().getFunctionLibrary(libraryName.getStringValue());

    final String[] parameterArray;
    if (parameters != null) {
      // CONVERT PARAMETERS
      parameterArray = new String[parameters.size()];
      for (int i = 0; i < parameters.size(); i++)
        parameterArray[i] = parameters.get(i).getStringValue();
    } else
      parameterArray = new String[] {};

    final FunctionDefinition f;
    switch (language.getStringValue()) {
    case "js":
      f = new JavascriptFunctionDefinition(functionName.getStringValue(), code, parameterArray);
      break;

    case "sql":
      f = new SQLFunctionDefinition(database, functionName.getStringValue(), code);
      break;

    default:
      throw new CommandSQLParsingException(
          "Error on function creation: language '" + language.getStringValue() + "' not supported");
    }

    fLib.registerFunction(f);

    return new InternalResultSet().add(
        new ResultInternal(context.getDatabase()).setProperty("operation", "create function").setProperty("libraryName", libraryName.getStringValue())
            .setProperty("functionName", functionName.getStringValue()));
  }

  @Override
  public void toString(final Map<String, Object> params, final StringBuilder builder) {
    builder.append("DEFINE FUNCTION ");
    libraryName.toString(params, builder);
    builder.append(".");
    functionName.toString(params, builder);
    builder.append(" ");
    builder.append(codeQuoted);
    if (parameters != null) {
      boolean first = true;
      builder.append(" PARAMETERS [");
      for (final Identifier param : parameters) {
        if (!first) {
          builder.append(", ");
        }
        param.toString(params, builder);
        first = false;
      }
      builder.append("]");
    }

    if (language != null) {
      builder.append(" LANGUAGE ");
      language.toString(params, builder);
    }
  }

  @Override
  public DefineFunctionStatement copy() {
    final DefineFunctionStatement result = new DefineFunctionStatement(-1);
    result.libraryName = libraryName == null ? null : libraryName.copy();
    result.functionName = functionName == null ? null : functionName.copy();
    result.codeQuoted = codeQuoted;
    result.code = code;
    result.parameters = parameters == null ? null : parameters.stream().map(x -> x.copy()).collect(Collectors.toList());
    result.language = language == null ? null : language.copy();
    return result;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    final DefineFunctionStatement that = (DefineFunctionStatement) o;

    if (!Objects.equals(libraryName, that.libraryName))
      return false;
    if (!Objects.equals(functionName, that.functionName))
      return false;
    if (!Objects.equals(codeQuoted, that.codeQuoted))
      return false;
    if (!Objects.equals(code, that.code))
      return false;
    if (!Objects.equals(parameters, that.parameters))
      return false;
    return Objects.equals(language, that.language);
  }

  @Override
  public int hashCode() {
    int result = libraryName != null ? libraryName.hashCode() : 0;
    result = 31 * result + (functionName != null ? functionName.hashCode() : 0);
    result = 31 * result + (codeQuoted != null ? codeQuoted.hashCode() : 0);
    result = 31 * result + (code != null ? code.hashCode() : 0);
    result = 31 * result + (parameters != null ? parameters.hashCode() : 0);
    result = 31 * result + (language != null ? language.hashCode() : 0);
    return result;
  }
}
/* JavaCC - OriginalChecksum=bbc914f66e96822dedc7e89e14240872 (do not edit this line) */
