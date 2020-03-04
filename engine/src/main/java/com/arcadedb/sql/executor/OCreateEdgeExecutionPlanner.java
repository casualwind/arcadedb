/*
 * Copyright (c) - Arcade Data LTD (https://arcadedata.com)
 */

package com.arcadedb.sql.executor;

import com.arcadedb.database.Database;
import com.arcadedb.schema.DocumentType;
import com.arcadedb.sql.parser.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by luigidellaquila on 08/08/16.
 */
public class OCreateEdgeExecutionPlanner {

  protected Identifier targetClass;
  protected Identifier targetClusterName;
  protected Expression leftExpression;
  protected Expression rightExpression;

  protected InsertBody body;
  protected Number     retry;
  protected Number     wait;
  protected Batch      batch;


  public OCreateEdgeExecutionPlanner(CreateEdgeStatement statement) {
    this.targetClass = statement.getTargetType() == null ? null : statement.getTargetType().copy();
    this.targetClusterName = statement.getTargetBucketName() == null ? null : statement.getTargetBucketName().copy();
    this.leftExpression = statement.getLeftExpression() == null ? null : statement.getLeftExpression().copy();
    this.rightExpression = statement.getRightExpression() == null ? null : statement.getRightExpression().copy();
    this.body = statement.getBody() == null ? null : statement.getBody().copy();
    this.retry = statement.getRetry();
    this.wait = statement.getWait();
    this.batch = statement.getBatch() == null ? null : statement.getBatch().copy();

  }

  public InsertExecutionPlan createExecutionPlan(CommandContext ctx, boolean enableProfiling) {

    if (targetClass == null) {
      if (targetClusterName == null) {
        targetClass = new Identifier("E");
      } else {
        Database db = ctx.getDatabase();
        DocumentType typez = db.getSchema()
            .getTypeByBucketId((db.getSchema().getBucketByName(targetClusterName.getStringValue()).getId()));
        if (typez != null) {
          targetClass = new Identifier(typez.getName());
        } else {
          targetClass = new Identifier("E");
        }
      }
    }

    InsertExecutionPlan result = new InsertExecutionPlan(ctx);

    handleCheckType(result, ctx, enableProfiling);

    handleGlobalLet(result, new Identifier("$__ORIENT_CREATE_EDGE_fromV"), leftExpression, ctx, enableProfiling);
    handleGlobalLet(result, new Identifier("$__ORIENT_CREATE_EDGE_toV"), rightExpression, ctx, enableProfiling);

    result.chain(new CreateEdgesStep(targetClass, targetClusterName, new Identifier("$__ORIENT_CREATE_EDGE_fromV"),
        new Identifier("$__ORIENT_CREATE_EDGE_toV"), wait, retry, batch, ctx, enableProfiling));

    handleSetFields(result, body, ctx, enableProfiling);
    handleSave(result, targetClusterName, ctx, enableProfiling);
    //TODO implement batch, wait and retry
    return result;
  }

  private void handleGlobalLet(InsertExecutionPlan result, Identifier name, Expression expression, CommandContext ctx, boolean profilingEnabled) {
    result.chain(new GlobalLetExpressionStep(name, expression, ctx, profilingEnabled));
  }

  private void handleCheckType(InsertExecutionPlan result, CommandContext ctx, boolean profilingEnabled) {
    if (targetClass != null) {
      result.chain(new CheckClassTypeStep(targetClass.getStringValue(), "E", ctx, profilingEnabled));
    }
  }

  private void handleSave(InsertExecutionPlan result, Identifier targetClusterName, CommandContext ctx, boolean profilingEnabled) {
    result.chain(new SaveElementStep(ctx, targetClusterName, profilingEnabled));
  }

  private void handleSetFields(InsertExecutionPlan result, InsertBody insertBody, CommandContext ctx, boolean profilingEnabled) {
    if (insertBody == null) {
      return;
    }
    if (insertBody.getIdentifierList() != null) {
      result.chain(new InsertValuesStep(insertBody.getIdentifierList(), insertBody.getValueExpressions(), ctx, profilingEnabled));
    } else if (insertBody.getContent() != null) {
      result.chain(new UpdateContentStep(insertBody.getContent(), ctx, profilingEnabled));
    } else if (insertBody.getSetExpressions() != null) {
      List<UpdateItem> items = new ArrayList<>();
      for (InsertSetExpression exp : insertBody.getSetExpressions()) {
        UpdateItem item = new UpdateItem(-1);
        item.setOperator(UpdateItem.OPERATOR_EQ);
        item.setLeft(exp.getLeft().copy());
        item.setRight(exp.getRight().copy());
        items.add(item);
      }
      result.chain(new UpdateSetStep(items, ctx, profilingEnabled));
    }
  }

}