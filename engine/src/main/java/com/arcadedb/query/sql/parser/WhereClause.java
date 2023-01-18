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
/* Generated By:JJTree: Do not edit this line. OWhereClause.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=true,NODE_PREFIX=O,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_USERTYPE_VISIBILITY_PUBLIC=true */
package com.arcadedb.query.sql.parser;

import com.arcadedb.database.Identifiable;
import com.arcadedb.index.Index;
import com.arcadedb.index.IndexInternal;
import com.arcadedb.index.TypeIndex;
import com.arcadedb.query.sql.executor.CommandContext;
import com.arcadedb.query.sql.executor.Result;
import com.arcadedb.query.sql.executor.ResultInternal;
import com.arcadedb.schema.DocumentType;
import com.arcadedb.schema.Schema;
import com.arcadedb.schema.Type;
import com.arcadedb.utility.CollectionUtils;

import java.util.*;
import java.util.stream.*;

public class WhereClause extends SimpleNode {
  protected BooleanExpression baseExpression;

  protected List<AndBlock> flattened;

  public WhereClause(final int id) {
    super(id);
  }

  public WhereClause(final SqlParser p, final int id) {
    super(p, id);
  }

  public boolean matchesFilters(final Identifiable currentRecord, final CommandContext ctx) {
    if (baseExpression == null)
      return true;

    return baseExpression.evaluate(currentRecord, ctx);
  }

  public boolean matchesFilters(final Result currentRecord, final CommandContext ctx) {
    if (baseExpression == null)
      return true;

    return baseExpression.evaluate(currentRecord, ctx);
  }

  public void toString(final Map<String, Object> params, final StringBuilder builder) {
    if (baseExpression == null) {
      return;
    }
    baseExpression.toString(params, builder);
  }

  /**
   * estimates how many items of this class will be returned applying this filter
   *
   * @param oClass
   *
   * @return an estimation of the number of records of this class returned applying this filter, 0 if and only if sure that no
   * records are returned
   */
  public long estimate(final DocumentType oClass, final long threshold, final CommandContext ctx) {
    long count = ctx.getDatabase().countType(oClass.getName(), true);
    if (count > 1) {
      count = count / 2;
    }
    if (count < threshold) {
      return count;
    }

    long indexesCount = 0L;
    final List<AndBlock> flattenedConditions = flatten();
    final Collection<TypeIndex> indexes = oClass.getAllIndexes(true);
    for (final AndBlock condition : flattenedConditions) {

      final List<BinaryCondition> indexedFunctConditions = condition.getIndexedFunctionConditions(oClass, ctx.getDatabase());

      long conditionEstimation = Long.MAX_VALUE;

      if (indexedFunctConditions != null) {
        for (final BinaryCondition cond : indexedFunctConditions) {
          final FromClause from = new FromClause(-1);
          from.item = new FromItem(-1);
          from.item.setIdentifier(new Identifier(oClass.getName()));
          final long newCount = cond.estimateIndexed(from, ctx);
          if (newCount < conditionEstimation) {
            conditionEstimation = newCount;
          }
        }
      } else {
        final Map<String, Object> conditions = getEqualityOperations(condition, ctx);

        for (final Index index : indexes) {
          if (index.getType().equals(Schema.INDEX_TYPE.FULL_TEXT))
            continue;

          final List<String> indexedFields = index.getPropertyNames();
          int nMatchingKeys = 0;
          for (final String indexedField : indexedFields) {
            if (conditions.containsKey(indexedField)) {
              nMatchingKeys++;
            } else {
              break;
            }
          }
          if (nMatchingKeys > 0) {
            final long newCount = estimateFromIndex(index, conditions, nMatchingKeys);
            if (newCount < conditionEstimation) {
              conditionEstimation = newCount;
            }
          }
        }
      }
      if (conditionEstimation > count) {
        return count;
      }
      indexesCount += conditionEstimation;
    }
    return Math.min(indexesCount, count);
  }

  private long estimateFromIndex(final Index index, final Map<String, Object> conditions, final int nMatchingKeys) {
    if (nMatchingKeys < 1) {
      throw new IllegalArgumentException("Cannot estimate from an index with zero keys");
    }
    List<String> definitionFields = index.getPropertyNames();
    Object[] key = new Object[nMatchingKeys];
    for (int i = 0; i < nMatchingKeys; i++) {
      Object keyValue = convert(conditions.get(definitionFields.get(i)), ((IndexInternal) index).getKeyTypes()[i]);
      key[i] = keyValue;
    }
    if (key != null) {
      if (conditions.size() == definitionFields.size()) {
        CollectionUtils.countEntries(index.get(key));
      } else if (index.supportsOrderedIterations()) {
        return ((TypeIndex) index).range(true, key, true, key, true).estimateSize();
      }
    }
    return Long.MAX_VALUE;
  }

  private Map<String, Object> getEqualityOperations(final AndBlock condition, final CommandContext ctx) {
    final Map<String, Object> result = new HashMap<String, Object>();
    for (final BooleanExpression expression : condition.subBlocks) {
      if (expression instanceof BinaryCondition) {
        final BinaryCondition b = (BinaryCondition) expression;
        if (b.operator instanceof EqualsCompareOperator) {
          if (b.left.isBaseIdentifier() && b.right.isEarlyCalculated(ctx)) {
            result.put(b.left.toString(), b.right.execute((Result) null, ctx));
          }
        }
      }
    }
    return result;
  }

  public List<AndBlock> flatten() {
    if (this.baseExpression == null)
      return Collections.emptyList();

    if (flattened == null)
      flattened = this.baseExpression.flatten();

    // TODO remove false conditions (contradictions)
    return flattened;
  }

  public void setBaseExpression(final BooleanExpression baseExpression) {
    this.baseExpression = baseExpression;
  }

  public WhereClause copy() {
    final WhereClause result = new WhereClause(-1);
    result.baseExpression = baseExpression.copy();
    result.flattened = flattened == null ? null : flattened.stream().map(x -> x.copy()).collect(Collectors.toList());
    return result;
  }

  @Override
  protected Object[] getIdentityElements() {
    return new Object[] { baseExpression, flattened };
  }

  public void extractSubQueries(final SubQueryCollector collector) {
    if (baseExpression != null)
      baseExpression.extractSubQueries(collector);

    flattened = null;
  }

  public BooleanExpression getBaseExpression() {
    return baseExpression;
  }

  public Result serialize() {
    final ResultInternal result = new ResultInternal();
    if (baseExpression != null) {
      result.setProperty("baseExpression", baseExpression.serialize());
    }
    if (flattened != null) {
      result.setProperty("flattened", flattened.stream().map(x -> x.serialize()).collect(Collectors.toList()));
    }
    return result;
  }

  public void deserialize(final Result fromResult) {
    if (fromResult.getProperty("baseExpression") != null) {
      baseExpression = BooleanExpression.deserializeFromOResult(fromResult.getProperty("baseExpression"));
    }
    if (fromResult.getProperty("flattened") != null) {
      final List<Result> ser = fromResult.getProperty("flattened");
      flattened = new ArrayList<>();
      for (final Result r : ser) {
        final AndBlock block = new AndBlock(-1);
        block.deserialize(r);
        flattened.add(block);
      }
    }
  }

  @Override
  protected SimpleNode[] getCacheableElements() {
    return new SimpleNode[] { baseExpression };
  }

  public static Object convert(final Object o, final Type oType) {
    return Type.convert(null, o, oType.getDefaultJavaType());
  }
}
/* JavaCC - OriginalChecksum=e8015d01ce1ab2bc337062e9e3f2603e (do not edit this line) */
