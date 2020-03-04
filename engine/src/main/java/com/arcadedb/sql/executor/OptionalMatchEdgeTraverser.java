/*
 * Copyright (c) - Arcade Data LTD (https://arcadedata.com)
 */

package com.arcadedb.sql.executor;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by luigidellaquila on 17/10/16.
 */
public class OptionalMatchEdgeTraverser extends MatchEdgeTraverser {
  public static final Result EMPTY_OPTIONAL = new ResultInternal();

  public OptionalMatchEdgeTraverser(Result lastUpstreamRecord, EdgeTraversal edge) {
    super(lastUpstreamRecord, edge);
  }

  protected void init(CommandContext ctx) {
    if (downstream == null) {
      super.init(ctx);
      if (!downstream.hasNext()) {
        List x = new ArrayList();
        x.add(EMPTY_OPTIONAL);
        downstream = x.iterator();
      }
    }
  }

  public Result next(CommandContext ctx) {
    init(ctx);
    if (!downstream.hasNext()) {
      throw new IllegalStateException();
    }

    String endPointAlias = getEndpointAlias();
    Object prevValue = sourceRecord.getProperty(endPointAlias);
    ResultInternal next = downstream.next();

    if (isEmptyOptional(prevValue)) {
      return sourceRecord;
    }
    if (!isEmptyOptional(next)) {
      if (prevValue != null && !equals(prevValue, next.getElement().get())) {
        return null;
      }
    }

    ResultInternal result = new ResultInternal();
    for (String prop : sourceRecord.getPropertyNames()) {
      result.setProperty(prop, sourceRecord.getProperty(prop));
    }
    result.setProperty(endPointAlias, next.getElement().map(x -> toResult(x)).orElse(null));
    return result;
  }

  public static boolean isEmptyOptional(Object elem) {
    if (elem == EMPTY_OPTIONAL) {
      return true;
    }
    return elem instanceof Result && EMPTY_OPTIONAL == ((Result) elem).getElement().orElse(null);

  }
}
