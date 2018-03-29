package com.arcadedb.sql.parser;

import com.arcadedb.database.PIdentifiable;
import com.arcadedb.sql.executor.OCommandContext;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Created by luigidellaquila on 02/10/15.
 */
public class OQueryCursor implements Iterator<PIdentifiable> {
  private int                     limit;
  private int                     skip;
  private WhereClause             filter;
  private Iterator<PIdentifiable> iterator;
  private OrderBy                 orderBy;
  private OCommandContext         ctx;

  private PIdentifiable           next         = null;
  private long                    countFetched = 0;

  public OQueryCursor() {

  }

  public OQueryCursor(Iterator<PIdentifiable> PIdentifiableIterator, WhereClause filter, OrderBy orderBy, int skip, int limit,
      OCommandContext ctx) {
    this.iterator = PIdentifiableIterator;
    this.filter = filter;
    this.skip = skip;
    this.limit = limit;
    this.orderBy = orderBy;
    this.ctx = ctx;
    loadNext();
  }

  private void loadNext() {
    if (iterator == null) {
      next = null;
      return;
    }
    if (limit > 0 && countFetched >= limit) {
      next = null;
      return;
    }
    if (countFetched == 0 && skip > 0) {
      for (int i = 0; i < skip; i++) {
        next = getNextFromIterator();
        if (next == null) {
          return;
        }
      }
    }
    next = getNextFromIterator();
    countFetched++;
  }

  private PIdentifiable getNextFromIterator() {
    while (true) {
      if (iterator == null || !iterator.hasNext()) {
        return null;
      }

      PIdentifiable result = iterator.next();
      if (filter==null || filter.matchesFilters(result, ctx)) {
        return result;
      }
    }
  }

  public boolean hasNext() {
    return next != null;
  }

  public void remove() {
    throw new UnsupportedOperationException("remove");
  }

  public PIdentifiable next() {
    PIdentifiable result = next;
    if (result == null) {
      throw new NoSuchElementException();
    }
    loadNext();
    return result;
  }
}
