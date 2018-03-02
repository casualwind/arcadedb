package com.arcadedb.database;

import com.arcadedb.PGlobalConfiguration;
import com.arcadedb.engine.PBasePage;
import com.arcadedb.engine.PModifiablePage;
import com.arcadedb.engine.PPageId;
import com.arcadedb.engine.PPageManager;
import com.arcadedb.exception.PConcurrentModificationException;
import com.arcadedb.exception.PTransactionException;

import java.io.IOException;
import java.util.*;

/**
 * Manage the transaction context. When the transaction begins, the modifiedPages map is initialized. This allows to always delegate
 * to the transaction context, even if there is not active transaction by ignoring tx data. This keeps code smaller.
 */
public class PTransactionContext {
  private final PDatabase                     database;
  private       Map<PPageId, PModifiablePage> modifiedPages;
  private       Map<PPageId, PModifiablePage> newPages;
  private final Map<Integer, Integer> newPageCounters = new HashMap<Integer, Integer>();
  private final Set<PPageId>          pagesToDispose  = new HashSet<PPageId>();

  public PTransactionContext(final PDatabase database) {
    this.database = database;
  }

  public void begin() {
    if (modifiedPages != null)
      throw new PTransactionException("Transaction already begun");

    modifiedPages = new HashMap<PPageId, PModifiablePage>();
  }

  public void commit() {
    if (modifiedPages == null)
      throw new PTransactionException("Transaction not begun");

    // LOCK FILES IN ORDER (TO AVOID DEADLOCK)
    final List<Integer> lockedFiles = lockFilesInOrder();
    try {

      // CHECK THE VERSION FIRST
      final PPageManager pageManager = database.getPageManager();
      for (PModifiablePage p : modifiedPages.values())
        pageManager.checkPageVersion(p);

      // AT THIS POINT, LOCK + VERSION CHECK, THERE IS NO NEED TO MANAGE ROLLBACK
      for (PModifiablePage p : modifiedPages.values())
        pageManager.updatePage(p);
      modifiedPages = null;

      if (newPages != null) {
        for (PModifiablePage p : newPages.values())
          pageManager.updatePage(p);

        for (Map.Entry<Integer, Integer> entry : newPageCounters.entrySet())
          database.getSchema().getFileById(entry.getKey()).onAfterCommit(entry.getValue());

        newPages = null;
      }

      pageManager.addPagesToDispose(pagesToDispose);
      pagesToDispose.clear();

    } catch (PConcurrentModificationException e) {
      throw e;
    } catch (Exception e) {
      rollback();
      throw new PTransactionException("Transaction error on commit", e);
    } finally {
      unlockFilesInOrder(lockedFiles);
    }
  }

  public void rollback() {
    modifiedPages = null;
    newPages = null;
  }

  public void addPageToDispose(final PPageId pageId) {
    pagesToDispose.add(pageId);
  }

  /**
   * Looks for the page in the TX context first, then delegates to the database.
   */
  public PBasePage getPage(final PPageId pageId, final int size) throws IOException {
    PBasePage page = null;

    if (modifiedPages != null)
      page = modifiedPages.get(pageId);

    if (page == null && newPages != null)
      page = newPages.get(pageId);

    if (page == null)
      // NOT FOUND, DELEGATES TO THE DATABASE
      page = database.getPageManager().getPage(pageId, size);

    return page;
  }

  /**
   * If the page is not already in transaction tx, loads from the database and clone it locally.
   */
  public PModifiablePage getPageToModify(final PPageId pageId, final int size) throws IOException {
    if (!isActive())
      throw new PTransactionException("Transaction not active");

    PModifiablePage page = modifiedPages.get(pageId);
    if (page == null) {
      if (newPages != null)
        page = newPages.get(pageId);

      if (page == null) {
        // NOT FOUND, DELEGATES TO THE DATABASE
        final PBasePage loadedPage = database.getPageManager().getPage(pageId, size);
        if (loadedPage != null) {
          PModifiablePage modifiablePage = loadedPage.createModifiableCopy();
          modifiedPages.put(pageId, modifiablePage);
          page = modifiablePage;
        }
      }
    }

    return page;
  }

  public PModifiablePage addPage(final PPageId pageId, final int pageSize) throws IOException {
    if (newPages == null)
      newPages = new HashMap<PPageId, PModifiablePage>();

    // CREATE A PAGE ID BASED ON NEW PAGES IN TX. IN CASE OF ROLLBACK THEY ARE SIMPLY REMOVED AND THE GLOBAL PAGE COUNT IS UNCHANGED
    final PModifiablePage page = new PModifiablePage(database.getPageManager(), pageId, pageSize);
    newPages.put(pageId, page);

    final Integer indexCounter = newPageCounters.get(pageId.getFileId());
    if (indexCounter == null || indexCounter < pageId.getPageNumber() + 1)
      newPageCounters.put(pageId.getFileId(), pageId.getPageNumber() + 1);

    return page;
  }

  public Integer getPageCounter(final int indexFileId) {
    return newPageCounters.get(indexFileId);
  }

  public boolean isActive() {
    return modifiedPages != null;
  }

  public int getModifiedPages() {
    int result = 0;
    if (modifiedPages != null)
      result += modifiedPages.size();
    if (newPages != null)
      result += newPages.size();
    return result;
  }

  private List<Integer> lockFilesInOrder() {
    final Set<Integer> modifiedFiles = new HashSet<>();
    for (PPageId p : modifiedPages.keySet())
      modifiedFiles.add(p.getFileId());
    if (newPages != null)
      for (PPageId p : newPages.keySet())
        modifiedFiles.add(p.getFileId());

    final List<Integer> orderedModifiedFiles = new ArrayList<>(modifiedFiles);
    Collections.sort(orderedModifiedFiles);

    final long timeout = PGlobalConfiguration.COMMIT_LOCK_TIMEOUT.getValueAsLong();

    final List<Integer> lockedFiles = new ArrayList<>(orderedModifiedFiles.size());
    try {
      for (Integer fileId : orderedModifiedFiles) {
        if (database.getFileManager().getFile(fileId).tryLock(timeout))
          lockedFiles.add(fileId);
        else
          break;
      }

      if (lockedFiles.size() == orderedModifiedFiles.size())
        // OK: ALL LOCKED
        return lockedFiles;

    } catch (InterruptedException e) {
      // MANAGE THIS BELOW AS TIMEOUT EXCEPTION
    }

    // ERROR: UNLOCK LOCKED FILES
    unlockFilesInOrder(lockedFiles);
    throw new PTransactionException("Timeout on locking resource during commit");
  }

  private void unlockFilesInOrder(final List<Integer> lockedFiles) {
    for (Integer fileId : lockedFiles)
      database.getFileManager().getFile(fileId).unlock();
  }
}
