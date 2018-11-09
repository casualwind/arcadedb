/*
 * Copyright (c) 2018 - Arcade Analytics LTD (https://arcadeanalytics.com)
 */

/* Generated By:JJTree: Do not edit this line. OProjection.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=true,NODE_PREFIX=O,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_USERTYPE_VISIBILITY_PUBLIC=true */
package com.arcadedb.sql.parser;

import com.arcadedb.database.Record;
import com.arcadedb.exception.CommandSQLParsingException;
import com.arcadedb.sql.executor.CommandContext;
import com.arcadedb.sql.executor.Result;
import com.arcadedb.sql.executor.ResultInternal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class Projection extends SimpleNode {

  protected boolean distinct = false;

  List<ProjectionItem> items;

  public Projection(List<ProjectionItem> items, boolean distinct) {
    super(-1);
    this.items = items;
    this.distinct = distinct;
    //TODO make the whole class immutable!
  }

  public Projection(int id) {
    super(id);
  }

  public Projection(SqlParser p, int id) {
    super(p, id);
  }

  /**
   * Accept the visitor.
   **/
  public Object jjtAccept(SqlParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }

  public List<ProjectionItem> getItems() {
    return items;
  }

  public void setItems(List<ProjectionItem> items) {
    this.items = items;
  }

  @Override
  public void toString(Map<Object, Object> params, StringBuilder builder) {
    if (items == null) {
      return;
    }
    boolean first = true;

    if (distinct) {
      builder.append("DISTINCT ");
    }
    // print * before
    for (ProjectionItem item : items) {
      if (item.isAll()) {
        if (!first) {
          builder.append(", ");
        }

        item.toString(params, builder);
        first = false;
      }
    }

    // and then the rest of the projections
    for (ProjectionItem item : items) {
      if (!item.isAll()) {
        if (!first) {
          builder.append(", ");
        }

        item.toString(params, builder);
        first = false;
      }
    }
  }

  public Result calculateSingle(CommandContext iContext, Result iRecord) {
    if (isExpand()) {
      throw new IllegalStateException("This is an expand projection, it cannot be calculated as a single result" + toString());
    }

    if (items.size() == 0 || (items.size() == 1 && items.get(0).isAll()) && items.get(0).nestedProjection == null) {
      return iRecord;
    }

    ResultInternal result = new ResultInternal();
    for (ProjectionItem item : items) {
      if (item.isAll()) {
        for (String alias : iRecord.getPropertyNames()) {
          result.setProperty(alias, iRecord.getProperty(alias));
        }
        if (iRecord.getElement().isPresent()) {
          Record x = iRecord.getElement().get();
          result.setProperty("@rid", x.getIdentity());
//          result.setProperty("@version", x.getVersion());
//          result.setProperty("@class", x.getSchemaType().map(typez -> typez.getName()).orElse(null));
        }
        if (item.nestedProjection != null) {
          result = (ResultInternal) item.nestedProjection.apply(item.expression, result, iContext);
        }
      } else {
        result.setProperty(item.getProjectionAliasAsString(), item.execute(iRecord, iContext));
      }

    }
    return result;
  }

  public boolean isExpand() {
    return items != null && items.size() == 1 && items.get(0).isExpand();
  }

  public void validate() {
    if (items != null && items.size() > 1) {
      for (ProjectionItem item : items) {
        if (item.isExpand()) {
          throw new CommandSQLParsingException("Cannot execute a query with expand() together with other projections");
        }
      }
    }
  }

  public Projection getExpandContent() {
    Projection result = new Projection(-1);
    result.setItems(new ArrayList<>());
    result.getItems().add(this.getItems().get(0).getExpandContent());
    return result;
  }

  public Set<String> getAllAliases() {
    return items.stream().map(i -> i.getProjectionAliasAsString()).collect(Collectors.toSet());
  }

  public Projection copy() {
    Projection result = new Projection(-1);
    if (items != null) {
      result.items = items.stream().map(x -> x.copy()).collect(Collectors.toList());
    }
    result.distinct = distinct;
    return result;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    Projection that = (Projection) o;

    return items != null ? items.equals(that.items) : that.items == null;
  }

  @Override
  public int hashCode() {
    return items != null ? items.hashCode() : 0;
  }

  public boolean isDistinct() {
    return distinct;
  }

  public void setDistinct(boolean distinct) {
    this.distinct = distinct;
  }

  public void extractSubQueries(SubQueryCollector collector) {
    if (items != null) {
      for (ProjectionItem item : items) {
        item.extractSubQueries(collector);
      }
    }
  }

  public boolean refersToParent() {
    for (ProjectionItem item : items) {
      if (item.refersToParent()) {
        return true;
      }
    }
    return false;
  }

  public Result serialize() {
    ResultInternal result = new ResultInternal();
    result.setProperty("distinct", distinct);
    if (items != null) {
      result.setProperty("items", items.stream().map(x -> x.serialize()).collect(Collectors.toList()));
    }
    return result;
  }

  public void deserialize(Result fromResult) {
    distinct = fromResult.getProperty("distinct");
    if (fromResult.getProperty("items") != null) {
      items = new ArrayList<>();

      List<Result> ser = fromResult.getProperty("items");
      for (Result x : ser) {
        ProjectionItem item = new ProjectionItem(-1);
        item.deserialize(x);
        items.add(item);
      }
    }
  }

  public boolean isCacheable() {
    if (items != null) {
      for (ProjectionItem item : items) {
        if (!item.isCacheable()) {
          return false;
        }
      }
    }
    return true;
  }
}
/* JavaCC - OriginalChecksum=3a650307b53bae626dc063c4b35e62c3 (do not edit this line) */
