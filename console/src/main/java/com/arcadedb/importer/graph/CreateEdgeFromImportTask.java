/*
 * Copyright (c) - Arcade Data LTD (https://arcadedata.com)
 */

package com.arcadedb.importer.graph;

import com.arcadedb.database.DatabaseInternal;
import com.arcadedb.database.Identifiable;
import com.arcadedb.database.RID;
import com.arcadedb.database.async.DatabaseAsyncAbstractTask;
import com.arcadedb.database.async.DatabaseAsyncExecutor;
import com.arcadedb.graph.*;
import com.arcadedb.importer.ImporterContext;
import com.arcadedb.importer.ImporterSettings;
import com.arcadedb.index.CompressedRID2RIDsIndex;
import com.arcadedb.log.LogManager;
import com.arcadedb.utility.FileUtils;
import com.arcadedb.utility.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * Asynchronous Task that creates the relationship between the sourceVertex and the destinationVertex as outgoing.
 */
public class CreateEdgeFromImportTask extends DatabaseAsyncAbstractTask {
  private final GraphImporter.GraphImporterThreadContext threadContext;
  private final String                                   edgeTypeName;
  private final long                                     sourceVertexKey;
  private final long                                     destinationVertexKey;
  private final Object[]                                 params;
  private final ImporterContext                          context;
  private final ImporterSettings                         settings;

  public CreateEdgeFromImportTask(final GraphImporter.GraphImporterThreadContext threadContext, final String edgeTypeName, final long sourceVertexKey,
      final long destinationVertexKey, final Object[] edgeProperties, final ImporterContext context, final ImporterSettings settings) {
    this.threadContext = threadContext;
    this.edgeTypeName = edgeTypeName;
    this.sourceVertexKey = sourceVertexKey;
    this.destinationVertexKey = destinationVertexKey;
    this.params = edgeProperties;
    this.context = context;
    this.settings = settings;
  }

  public void execute(final DatabaseAsyncExecutor.AsyncThread async, final DatabaseInternal database) {

//    LogManager.instance().log(this, Level.INFO, "Using context %s from theadId=%d", null, threadContext, Thread.currentThread().getId());

    // TODO: LOAD FROM INDEX
    final RID destinationVertexRID = context.graphImporter.getVertex(threadContext.vertexIndexThreadBuffer, destinationVertexKey);
    if (destinationVertexRID == null) {
      // SKIP IT
      context.skippedEdges.incrementAndGet();
      return;
    }

    if (threadContext.lastSourceKey == null || !threadContext.lastSourceKey.equals(sourceVertexKey)) {
      createEdgesInBatch(database, threadContext.incomingConnectionsIndexThread, context, settings, threadContext.connections);
      threadContext.connections = new ArrayList<>();

      // TODO: LOAD FROM INDEX
      final RID sourceVertexRID = context.graphImporter.getVertex(threadContext.vertexIndexThreadBuffer, sourceVertexKey);
      if (sourceVertexRID == null) {
        // SKIP IT
        context.skippedEdges.incrementAndGet();
        return;
      }

      threadContext.lastSourceKey = sourceVertexKey;
      threadContext.lastSourceVertex = (VertexInternal) sourceVertexRID.getVertex(true);
    }

    threadContext.connections.add(new GraphEngine.CreateEdgeOperation(edgeTypeName, destinationVertexRID, params));

    ++threadContext.importedEdges;

    if (threadContext.incomingConnectionsIndexThread.getChunkSize() >= settings.maxRAMIncomingEdges) {
      LogManager.instance()
          .log(this, Level.INFO, "Creation of back connections, reached %s size (max=%s), flushing %d connections (slots=%d thread=%d)...", null,
              FileUtils.getSizeAsString(threadContext.incomingConnectionsIndexThread.getChunkSize()), FileUtils.getSizeAsString(settings.maxRAMIncomingEdges),
              threadContext.incomingConnectionsIndexThread.size(), threadContext.incomingConnectionsIndexThread.getTotalUsedSlots(),
              Thread.currentThread().getId());

      createIncomingEdgesInBatch(database, threadContext.incomingConnectionsIndexThread, new EdgeLinkedCallback() {
        @Override
        public void onLinked(long linked) {
          context.linkedEdges.addAndGet(linked);
        }
      });

      // CREATE A NEW CHUNK BEFORE CONTINUING
      threadContext.incomingConnectionsIndexThread = new CompressedRID2RIDsIndex(database, threadContext.incomingConnectionsIndexThread.getKeys(),
          (int) settings.expectedEdges);

      LogManager.instance().log(this, Level.INFO, "Creation done, reset index buffer and continue", null);
    }

    if (threadContext.importedEdges % settings.commitEvery == 0) {
      LogManager.instance().log(this, Level.FINE, "Committing batch of outgoing edges (chunkSize=%s max=%s entries=%d slots=%d)...", null,
          FileUtils.getSizeAsString(threadContext.incomingConnectionsIndexThread.getChunkSize()), FileUtils.getSizeAsString(settings.maxRAMIncomingEdges),
          threadContext.incomingConnectionsIndexThread.size(), threadContext.incomingConnectionsIndexThread.getTotalUsedSlots());

      createEdgesInBatch(database, threadContext.incomingConnectionsIndexThread, context, settings, threadContext.connections);
      threadContext.connections = new ArrayList<>();
    }
  }

  private void createEdgesInBatch(final DatabaseInternal database, final CompressedRID2RIDsIndex edgeIndex, final ImporterContext context,
      final ImporterSettings settings, final List<GraphEngine.CreateEdgeOperation> connections) {
    if (!connections.isEmpty()) {
      // CREATE EDGES ALL TOGETHER FOR THE PREVIOUS BATCH
      if (threadContext.lastSourceVertex.getOutEdgesHeadChunk() == null)
        // RELOAD IT
        threadContext.lastSourceVertex = (VertexInternal) threadContext.lastSourceVertex.getIdentity().getVertex();

      final List<Edge> newEdges = database.getGraphEngine().newEdges(database, threadContext.lastSourceVertex, connections, false);

      context.createdEdges.addAndGet(newEdges.size());

      for (Edge e : newEdges)
        edgeIndex.put(e.getIn(), e.getIdentity(), threadContext.lastSourceVertex.getIdentity());

      connections.clear();
    }
  }

  protected static void createIncomingEdgesInBatch(final DatabaseInternal database, final CompressedRID2RIDsIndex index, final EdgeLinkedCallback callback) {
    Vertex lastVertex = null;

    LogManager.instance()
        .log(CreateEdgeFromImportTask.class, Level.INFO, "Linking %d incoming connections (chunk=%s allocated=%s totalSlotUsed=%d keys=%d)...", null,
            index.size(), FileUtils.getSizeAsString(index.getChunkSize()), FileUtils.getSizeAsString(index.getChunkAllocated()), index.getTotalUsedSlots(),
            index.getKeys());

    List<Pair<Identifiable, Identifiable>> connections = new ArrayList<>();

    long totalVertices = 0;
    long totalEdges = 0;
    int minEdges = Integer.MAX_VALUE;
    int maxEdges = -1;

    for (final CompressedRID2RIDsIndex.EntryIterator it = index.entryIterator(); it.hasNext(); it.moveNext()) {
      try {
        final Vertex destinationVertex = it.getKeyRID().getVertex(true);

        if (!connections.isEmpty() && !destinationVertex.equals(lastVertex)) {
          ++totalVertices;

          if (connections.size() < minEdges)
            minEdges = connections.size();
          if (connections.size() > maxEdges)
            maxEdges = connections.size();

          connectIncomingEdges(database, lastVertex, connections, callback);

          connections = new ArrayList<>();
        }

        lastVertex = destinationVertex;

        connections.add(new Pair<>(it.getEdgeRID(), it.getVertexRID()));

        ++totalEdges;
      } catch (Exception e) {
        LogManager.instance()
            .log(CreateEdgeFromImportTask.class, Level.SEVERE, "Error on creating incoming edge from %s -[%s]-> %s", e, it.getVertexRID(), it.getEdgeRID(),
                it.getKeyRID(), it.getVertexRID());
      }
    }

    if (lastVertex != null)
      connectIncomingEdges(database, lastVertex, connections, callback);

    LogManager.instance()
        .log(CreateEdgeFromImportTask.class, Level.INFO, "Created %d back connections from %d vertices (min=%d max=%d avg=%d)", null, totalEdges, totalVertices,
            minEdges, maxEdges, totalVertices > 0 ? totalEdges / totalVertices : 0);

  }

  public static void connectIncomingEdges(final DatabaseInternal database, final Identifiable toVertex,
      final List<Pair<Identifiable, Identifiable>> connections, final EdgeLinkedCallback callback) {

    final MutableVertex toVertexRecord = ((Vertex) toVertex.getRecord()).modify();

    final EdgeSegment inChunk = database.getGraphEngine().createInEdgeChunk(database, toVertexRecord);

    final EdgeLinkedList inLinkedList = new EdgeLinkedList(toVertexRecord, Vertex.DIRECTION.IN, inChunk);
    inLinkedList.addAll(connections);

    if (callback != null)
      callback.onLinked(connections.size());
  }

  @Override
  public String toString() {
    return "CreateEdgeFromImportTask(" + sourceVertexKey + "->" + destinationVertexKey + ")";
  }
}
