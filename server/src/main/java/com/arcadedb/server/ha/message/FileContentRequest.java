/*
 * Copyright (c) - Arcade Data LTD (https://arcadedata.com)
 */
package com.arcadedb.server.ha.message;

import com.arcadedb.database.Binary;
import com.arcadedb.database.DatabaseInternal;
import com.arcadedb.engine.BasePage;
import com.arcadedb.engine.PageId;
import com.arcadedb.network.binary.NetworkProtocolException;
import com.arcadedb.server.ha.HAServer;

import java.io.IOException;

public class FileContentRequest extends HAAbstractCommand {
  private String databaseName;
  private int    fileId;
  private int    from;

  public FileContentRequest() {
  }

  public FileContentRequest(final String dbName, final int fileId, final int from) {
    this.databaseName = dbName;
    this.fileId = fileId;
    this.from = from;
  }

  @Override
  public HACommand execute(final HAServer server, final String remoteServerName, final long messageNumber) {
    final DatabaseInternal db = (DatabaseInternal) server.getServer().getDatabase(databaseName);
    final int pageSize = db.getFileManager().getFile(fileId).getPageSize();

    try {
      final int totalPages = (int) (db.getFileManager().getFile(fileId).getSize() / pageSize);

      final Binary pagesContent = new Binary();

      int pages = 0;

      for (int i = from; i < totalPages && pages < 10; ++i) {
        final PageId pageId = new PageId(fileId, i);
        final BasePage page = db.getPageManager().getPage(pageId, pageSize, false).createImmutableView();
        pagesContent.putByteArray(page.getContent().array(), pageSize);

        ++pages;
      }

      final boolean last = pages >= totalPages;

      pagesContent.flip();

      return new FileContentResponse(pagesContent, pages, last);

    } catch (IOException e) {
      throw new NetworkProtocolException("Cannot load pages", e);
    }
  }

  @Override
  public void toStream(final Binary stream) {
    stream.putString(databaseName);
    stream.putInt(fileId);
    stream.putInt(from);
  }

  @Override
  public void fromStream(final Binary stream) {
    databaseName = stream.getString();
    fileId = stream.getInt();
    from = stream.getInt();
  }

  @Override
  public String toString() {
    return "file(" + databaseName + "," + fileId + "," + from + ")";
  }
}
