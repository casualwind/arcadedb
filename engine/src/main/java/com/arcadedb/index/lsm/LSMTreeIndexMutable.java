/*
 * Copyright (c) - Arcade Data LTD (https://arcadedata.com)
 */

package com.arcadedb.index.lsm;

import com.arcadedb.GlobalConfiguration;
import com.arcadedb.database.*;
import com.arcadedb.engine.BasePage;
import com.arcadedb.engine.MutablePage;
import com.arcadedb.engine.PageId;
import com.arcadedb.engine.PaginatedFile;
import com.arcadedb.exception.DatabaseIsReadOnlyException;
import com.arcadedb.exception.DatabaseOperationException;
import com.arcadedb.index.IndexCursor;
import com.arcadedb.index.IndexCursorEntry;
import com.arcadedb.index.TempIndexCursor;
import com.arcadedb.log.LogManager;
import com.arcadedb.serializer.BinaryTypes;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;

import static com.arcadedb.database.Binary.BYTE_SERIALIZED_SIZE;
import static com.arcadedb.database.Binary.INT_SERIALIZED_SIZE;

public class LSMTreeIndexMutable extends LSMTreeIndexAbstract {
  public static final String UNIQUE_INDEX_EXT    = "umtidx";
  public static final String NOTUNIQUE_INDEX_EXT = "numtidx";

  private int                   subIndexFileId = -1;
  private LSMTreeIndexCompacted subIndex       = null;

  private AtomicLong statsAdjacentSteps  = new AtomicLong();
  private int        minPagesToScheduleACompaction;
  private int        currentMutablePages = 0;

  /**
   * Called at creation time.
   */
  protected LSMTreeIndexMutable(final LSMTreeIndex mainIndex, final DatabaseInternal database, final String name, final boolean unique, final String filePath,
      final PaginatedFile.MODE mode, final byte[] keyTypes, final int pageSize) throws IOException {
    super(mainIndex, database, name, unique, filePath, unique ? UNIQUE_INDEX_EXT : NOTUNIQUE_INDEX_EXT, mode, keyTypes, pageSize);
    database.checkTransactionIsActive();
    createNewPage();
    minPagesToScheduleACompaction = database.getConfiguration().getValueAsInteger(GlobalConfiguration.INDEX_COMPACTION_MIN_PAGES_SCHEDULE);
  }

  /**
   * Called at cloning time.
   */
  protected LSMTreeIndexMutable(final LSMTreeIndex mainIndex, final DatabaseInternal database, final String name, final boolean unique, final String filePath,
      final byte[] keyTypes, final int pageSize, final LSMTreeIndexCompacted subIndex) throws IOException {
    super(mainIndex, database, name, unique, filePath, unique ? UNIQUE_INDEX_EXT : NOTUNIQUE_INDEX_EXT, keyTypes, pageSize);
    this.subIndex = subIndex;
    minPagesToScheduleACompaction = database.getConfiguration().getValueAsInteger(GlobalConfiguration.INDEX_COMPACTION_MIN_PAGES_SCHEDULE);
  }

  /**
   * Called at load time (1st page only).
   */
  protected LSMTreeIndexMutable(final LSMTreeIndex mainIndex, final DatabaseInternal database, final String name, final boolean unique, final String filePath,
      final int id, final PaginatedFile.MODE mode, final int pageSize) throws IOException {
    super(mainIndex, database, name, unique, filePath, id, mode, pageSize);

    final BasePage currentPage = this.database.getTransaction().getPage(new PageId(file.getFileId(), 0), pageSize);

    int pos = INT_SERIALIZED_SIZE + INT_SERIALIZED_SIZE + BYTE_SERIALIZED_SIZE + INT_SERIALIZED_SIZE;

    // TODO: COUNT THE MUTABLE PAGES FROM THE TAIL BACK TO THE HEAD
    currentMutablePages = 1;

    subIndexFileId = currentPage.readInt(pos);

    pos += INT_SERIALIZED_SIZE;

    final int len = currentPage.readByte(pos++);
    this.keyTypes = new byte[len];
    for (int i = 0; i < len; ++i)
      this.keyTypes[i] = currentPage.readByte(pos++);

    minPagesToScheduleACompaction = database.getConfiguration().getValueAsInteger(GlobalConfiguration.INDEX_COMPACTION_MIN_PAGES_SCHEDULE);
  }

  @Override
  public void close() {
    if (subIndex != null)
      subIndex.close();
    super.close();
  }

  @Override
  public void onAfterLoad() {
    if (subIndexFileId > -1) {
      try {
        subIndex = (LSMTreeIndexCompacted) database.getSchema().getFileById(subIndexFileId);
        subIndex.mainIndex = mainIndex;
        subIndex.keyTypes = keyTypes;

      } catch (Exception e) {
        LogManager.instance().log(this, Level.SEVERE,
            "Invalid sub-index for index '%s', ignoring it. WARNING: This could lead on using partial indexes. Please recreate the index from scratch (error=%s)",
            null, name, e.getMessage());

        try {
          drop();
        } catch (IOException ex) {
          ex.printStackTrace();
        }
      }
    }
  }

  @Override
  public void onAfterCommit() {
    if (minPagesToScheduleACompaction > 0 && currentMutablePages >= minPagesToScheduleACompaction) {
      LogManager.instance()
          .log(this, Level.FINE, "Scheduled compaction of index '%s' (currentMutablePages=%d totalPages=%d)", null, name, currentMutablePages, getTotalPages());
      database.async().compact(mainIndex);
    }
  }

  public void put(final Object[] keys, final RID[] rids) {
    if (rids == null)
      throw new IllegalArgumentException("RIDs is null");

    internalPut(keys, rids);
  }

  public void remove(final Object[] keys) {
    internalRemove(keys, null);
  }

  public void remove(final Object[] keys, final Identifiable rid) {
    internalRemove(keys, rid);
  }

  public LSMTreeIndexCompacted createNewForCompaction() throws IOException {
    int last_ = name.lastIndexOf('_');
    final String newName = name.substring(0, last_) + "_" + System.nanoTime();

    return new LSMTreeIndexCompacted(mainIndex, database, newName, unique, database.getDatabasePath() + "/" + newName, keyTypes, pageSize);
  }

  public IndexCursor iterator(final boolean ascendingOrder, final Object[] fromKeys, final boolean inclusive) throws IOException {
    if (ascendingOrder)
      return range(fromKeys, inclusive, null, true);

    return range(null, true, fromKeys, inclusive);
  }

  public IndexCursor range(final Object[] fromKeys, final boolean beginKeysInclusive, final Object[] toKeys, final boolean endKeysInclusive)
      throws IOException {
    boolean ascending = true;

    if (fromKeys != null && toKeys != null)
      ascending = LSMTreeIndexMutable.compareKeys(comparator, keyTypes, fromKeys, toKeys) <= 0;

    return new LSMTreeIndexCursor(this, ascending, fromKeys, beginKeysInclusive, toKeys, endKeysInclusive);
  }

  public LSMTreeIndexUnderlyingPageCursor newPageIterator(final int pageId, final int currentEntryInPage, final boolean ascendingOrder) throws IOException {
    final BasePage page = database.getTransaction().getPage(new PageId(file.getFileId(), pageId), pageSize);
    return new LSMTreeIndexUnderlyingPageCursor(this, page, currentEntryInPage, getHeaderSize(pageId), keyTypes, getCount(page), ascendingOrder);
  }

  public LSMTreeIndexCompacted getSubIndex() {
    return subIndex;
  }

  public IndexCursor get(final Object[] keys, final int limit) throws IOException {
    checkForNulls(keys);

    final Object[] convertedKeys = convertKeys(keys, keyTypes);

    final Set<IndexCursorEntry> set = new HashSet<>();

    final Set<RID> removedRIDs = new HashSet<>();

    // NON COMPACTED INDEX, SEARCH IN ALL THE PAGES
    searchInNonCompactedIndex(keys, convertedKeys, limit, set, removedRIDs);

    return new TempIndexCursor(set);
  }

  @Override
  public Map<String, Long> getStats() {
    final Map<String, Long> stats = new HashMap<>();
    stats.put("pages", (long) getTotalPages());
    stats.put("adjacentSteps", statsAdjacentSteps.get());
    return stats;
  }

  public void removeTempSuffix() {
    super.removeTempSuffix();
    if (subIndex != null)
      subIndex.removeTempSuffix();
  }

  protected LookupResult compareKey(final Binary currentPageBuffer, final int startIndexArray, final Object[] convertedKeys, int mid, final int count,
      final int purpose) {

    int result = compareKey(currentPageBuffer, startIndexArray, convertedKeys, mid, count);

    if (result > 0)
      return HIGHER;
    else if (result < 0)
      return LOWER;

    if (purpose == 0) {
      // EXISTS
      currentPageBuffer.position(currentPageBuffer.getInt(startIndexArray + (mid * INT_SERIALIZED_SIZE)));
      final int keySerializedSize = getSerializedKeySize(currentPageBuffer, convertedKeys.length);

      return new LookupResult(true, false, mid, new int[] { currentPageBuffer.getInt(startIndexArray + (mid * INT_SERIALIZED_SIZE)) + keySerializedSize });
    } else if (purpose == 1) {
      // RETRIEVE
      currentPageBuffer.position(currentPageBuffer.getInt(startIndexArray + (mid * INT_SERIALIZED_SIZE)));
      final int keySerializedSize = getSerializedKeySize(currentPageBuffer, convertedKeys.length);

      // RETRIEVE ALL THE RESULTS
      final int firstKeyPos = findFirstEntryOfSameKey(currentPageBuffer, convertedKeys, startIndexArray, mid);
      final int lastKeyPos = findLastEntryOfSameKey(count, currentPageBuffer, convertedKeys, startIndexArray, mid);

      final int[] positionsArray = new int[lastKeyPos - firstKeyPos + 1];
      for (int i = firstKeyPos; i <= lastKeyPos; ++i)
        positionsArray[i - firstKeyPos] = currentPageBuffer.getInt(startIndexArray + (i * INT_SERIALIZED_SIZE)) + keySerializedSize;

      return new LookupResult(true, false, lastKeyPos, positionsArray);
    }

    if (convertedKeys.length < keyTypes.length) {
      // PARTIAL MATCHING
      if (purpose == 2) {
        // ASCENDING ITERATOR: FIND THE MOST LEFT ITEM
        mid = findFirstEntryOfSameKey(currentPageBuffer, convertedKeys, startIndexArray, mid);
      } else if (purpose == 3) {
        // DESCENDING ITERATOR
        mid = findLastEntryOfSameKey(count, currentPageBuffer, convertedKeys, startIndexArray, mid);
      }
    }

    // TODO: SET CORRECT VALUE POSITION FOR PARTIAL KEYS
    return new LookupResult(true, false, mid, new int[] { currentPageBuffer.position() });
  }

  private int findLastEntryOfSameKey(final int count, final Binary currentPageBuffer, final Object[] keys, final int startIndexArray, int mid) {
    int result;// FIND THE MOST RIGHT ITEM
    for (int i = mid + 1; i < count; ++i) {
      currentPageBuffer.position(currentPageBuffer.getInt(startIndexArray + (i * INT_SERIALIZED_SIZE)));

      result = 1;
      for (int keyIndex = 0; keyIndex < keys.length; ++keyIndex) {
        final byte keyType = keyTypes[keyIndex];
        if (keyType == BinaryTypes.TYPE_STRING) {
          // OPTIMIZATION: SPECIAL CASE, LAZY EVALUATE BYTE PER BYTE THE STRING
          result = comparator.compareStrings((byte[]) keys[keyIndex], currentPageBuffer);
        } else {
          final Object key = serializer.deserializeValue(database, currentPageBuffer, keyType);
          result = comparator.compare(keys[keyIndex], keyType, key, keyType);
        }

        if (result != 0)
          break;
      }

      if (result == 0) {
        mid = i;
        statsAdjacentSteps.incrementAndGet();
      } else
        break;
    }
    return mid;
  }

  public int getCurrentMutablePages() {
    return currentMutablePages;
  }

  public void setCurrentMutablePages(final int currentMutablePages) {
    this.currentMutablePages = currentMutablePages;
  }

  private int findFirstEntryOfSameKey(final Binary currentPageBuffer, final Object[] keys, final int startIndexArray, int mid) {
    int result;
    for (int i = mid - 1; i >= 0; --i) {
      currentPageBuffer.position(currentPageBuffer.getInt(startIndexArray + (i * INT_SERIALIZED_SIZE)));

      result = 1;
      for (int keyIndex = 0; keyIndex < keys.length; ++keyIndex) {
        if (keyTypes[keyIndex] == BinaryTypes.TYPE_STRING) {
          // OPTIMIZATION: SPECIAL CASE, LAZY EVALUATE BYTE PER BYTE THE STRING
          result = comparator.compareStrings((byte[]) keys[keyIndex], currentPageBuffer);
        } else {
          final Object key = serializer.deserializeValue(database, currentPageBuffer, keyTypes[keyIndex]);
          result = comparator.compare(keys[keyIndex], keyTypes[keyIndex], key, keyTypes[keyIndex]);
        }

        if (result != 0)
          break;
      }

      if (result == 0) {
        mid = i;
        statsAdjacentSteps.incrementAndGet();
      } else
        break;
    }
    return mid;
  }

  protected MutablePage createNewPage() throws IOException {
    // NEW FILE, CREATE HEADER PAGE
    final int txPageCounter = getTotalPages();

    final PageId pageId = new PageId(file.getFileId(), txPageCounter);
    final MutablePage currentPage = database.isTransactionActive() ?
        database.getTransaction().addPage(pageId, pageSize) :
        new MutablePage(database.getPageManager(), new PageId(getFileId(), txPageCounter), pageSize);

    int pos = 0;
    currentPage.writeInt(pos, currentPage.getMaxContentSize());
    pos += INT_SERIALIZED_SIZE;

    currentPage.writeInt(pos, 0); // ENTRIES COUNT
    pos += INT_SERIALIZED_SIZE;

    currentPage.writeByte(pos, (byte) 1); // MUTABLE PAGE
    pos += BYTE_SERIALIZED_SIZE;

    currentPage.writeInt(pos, 0); // COMPACTED PAGES
    pos += INT_SERIALIZED_SIZE;

    if (txPageCounter == 0) {
      currentPage.writeInt(pos, subIndex != null ? subIndex.getId() : -1); // SUB-INDEX FILE ID
      pos += INT_SERIALIZED_SIZE;

      currentPage.writeByte(pos++, (byte) keyTypes.length);
      for (int i = 0; i < keyTypes.length; ++i)
        currentPage.writeByte(pos++, keyTypes[i]);
    }

    ++currentMutablePages;

    return currentPage;
  }

  private void searchInNonCompactedIndex(final Object[] originalKeys, final Object[] convertedKeys, final int limit, final Set<IndexCursorEntry> set,
      final Set<RID> removedRIDs) throws IOException {
    // SEARCH FROM THE LAST PAGE BACK
    final int totalPages = getTotalPages();

    for (int p = totalPages - 1; p > -1; --p) {
      final BasePage currentPage = database.getTransaction().getPage(new PageId(file.getFileId(), p), pageSize);
      final Binary currentPageBuffer = new Binary(currentPage.slice());
      final int count = getCount(currentPage);

      if (count < 1)
        continue;

      if (!lookupInPageAndAddInResultset(currentPage, currentPageBuffer, count, originalKeys, convertedKeys, limit, set, removedRIDs))
        return;
    }

    if (subIndex != null)
      // CONTINUE ON THE SUB-INDEX
      subIndex.searchInCompactedIndex(originalKeys, convertedKeys, limit, set, removedRIDs);
  }

  protected void internalPut(final Object[] keys, final RID[] rids) {
    if (keys == null)
      throw new IllegalArgumentException("Keys parameter is null");

    if (database.getMode() == PaginatedFile.MODE.READ_ONLY)
      throw new DatabaseIsReadOnlyException("Cannot update the index '" + name + "'");

    if (keys.length != keyTypes.length)
      throw new IllegalArgumentException("Cannot put an entry in the index with a partial key");

    checkForNulls(keys);

    database.checkTransactionIsActive();

    final int txPageCounter = getTotalPages();

    if (txPageCounter < 1)
      throw new IllegalArgumentException("Cannot update the index '" + name + "' because the file is invalid");

    int pageNum = txPageCounter - 1;

    try {
      MutablePage currentPage = database.getTransaction().getPageToModify(new PageId(file.getFileId(), pageNum), pageSize, false);

      TrackableBinary currentPageBuffer = currentPage.getTrackable();

      int count = getCount(currentPage);

      final Object[] convertedKeys = convertKeys(keys, keyTypes);

      final LookupResult result = lookupInPage(pageNum, count, currentPageBuffer, convertedKeys, unique ? 0 : 1);

      // WRITE KEY/VALUE PAIRS FIRST
      final Binary keyValueContent = database.getContext().getTemporaryBuffer1();
      writeEntry(keyValueContent, convertedKeys, rids);

      int keyValueFreePosition = getValuesFreePosition(currentPage);

      int keyIndex = result.found ? result.keyIndex + 1 : result.keyIndex;
      boolean newPage = false;

      final boolean mutablePage = isMutable(currentPage);

      if (!mutablePage || keyValueFreePosition - (getHeaderSize(pageNum) + (count * INT_SERIALIZED_SIZE) + INT_SERIALIZED_SIZE) < keyValueContent.size()) {

        if (mutablePage)
          setMutable(currentPage, false);

        // NO SPACE LEFT, CREATE A NEW PAGE
        newPage = true;

        currentPage = createNewPage();

        assert isMutable(currentPage);

        currentPageBuffer = currentPage.getTrackable();
        pageNum = currentPage.getPageId().getPageNumber();
        count = 0;
        keyIndex = 0;
        keyValueFreePosition = currentPage.getMaxContentSize();
      }

      keyValueFreePosition -= keyValueContent.size();

      // WRITE KEY/VALUE PAIR CONTENT
      currentPageBuffer.putByteArray(keyValueFreePosition, keyValueContent.toByteArray());

      final int startPos = getHeaderSize(pageNum) + (keyIndex * INT_SERIALIZED_SIZE);
      if (keyIndex < count)
        // NOT LAST KEY, SHIFT POINTERS TO THE RIGHT
        currentPageBuffer.move(startPos, startPos + INT_SERIALIZED_SIZE, (count - keyIndex) * INT_SERIALIZED_SIZE);

      currentPageBuffer.putInt(startPos, keyValueFreePosition);

      setCount(currentPage, count + 1);
      setValuesFreePosition(currentPage, keyValueFreePosition);

      if (LogManager.instance().isDebugEnabled())
        LogManager.instance()
            .log(this, Level.FINE, "Put entry %s=%s in index '%s' (page=%s countInPage=%d newPage=%s)", null, Arrays.toString(keys), Arrays.toString(rids),
                name, currentPage.getPageId(), count + 1, newPage);

    } catch (IOException e) {
      throw new DatabaseOperationException(
          "Cannot index key '" + Arrays.toString(keys) + "' with value '" + Arrays.toString(rids) + "' in index '" + name + "'", e);
    }
  }

  protected void internalRemove(final Object[] keys, final Identifiable rid) {
    if (keys == null)
      throw new IllegalArgumentException("Keys parameter is null");

    if (database.getMode() == PaginatedFile.MODE.READ_ONLY)
      throw new DatabaseIsReadOnlyException("Cannot update the index '" + name + "'");

    if (keys.length != keyTypes.length)
      throw new IllegalArgumentException("Cannot remove an entry in the index with a partial key");

    checkForNulls(keys);

    database.checkTransactionIsActive();

    final int txPageCounter = getTotalPages();

    if (txPageCounter < 1)
      throw new IllegalArgumentException("Cannot update the index '" + name + "' because the file is invalid");

    int pageNum = txPageCounter - 1;

    try {
      MutablePage currentPage = database.getTransaction().getPageToModify(new PageId(file.getFileId(), pageNum), pageSize, false);

      assert isMutable(currentPage);

      TrackableBinary currentPageBuffer = currentPage.getTrackable();

      int count = getCount(currentPage);

      final RID removedRID = rid != null ? getRemovedRID(rid) : REMOVED_ENTRY_RID;

      final Object[] convertedKeys = convertKeys(keys, keyTypes);

      final LookupResult result = lookupInPage(pageNum, count, currentPageBuffer, convertedKeys, 1);
      if (result.found) {
        boolean exit = false;

        for (int i = result.valueBeginPositions.length - 1; !exit && i > -1; --i) {
          currentPageBuffer.position(result.valueBeginPositions[i]);

          final Object[] values = readEntryValues(currentPageBuffer);

          for (int v = values.length - 1; v > -1; --v) {
            final RID currentRID = (RID) values[v];

            if (rid != null) {
              if (rid.equals(currentRID)) {
                // FOUND
                exit = true;
                break;
              } else if (removedRID.equals(currentRID))
                // ALREADY DELETED
                return;
            }

            if (currentRID.getBucketId() == REMOVED_ENTRY_RID.getBucketId() && currentRID.getPosition() == REMOVED_ENTRY_RID.getPosition()) {
              // ALREADY DELETED
              return;
            }
          }
        }
      }

      // WRITE KEY/VALUE PAIRS FIRST
      final Binary keyValueContent = database.getContext().getTemporaryBuffer1();
      writeEntry(keyValueContent, keys, removedRID);

      int keyValueFreePosition = getValuesFreePosition(currentPage);

      int keyIndex = result.found ? result.keyIndex + 1 : result.keyIndex;
      boolean newPage = false;

      final boolean mutablePage = isMutable(currentPage);

      if (!mutablePage || keyValueFreePosition - (getHeaderSize(pageNum) + (count * INT_SERIALIZED_SIZE) + INT_SERIALIZED_SIZE) < keyValueContent.size()) {
        // NO SPACE LEFT, CREATE A NEW PAGE
        if (mutablePage)
          setMutable(currentPage, false);

        newPage = true;

        currentPage = createNewPage();

        assert isMutable(currentPage);

        currentPageBuffer = currentPage.getTrackable();
        pageNum = currentPage.getPageId().getPageNumber();
        count = 0;
        keyIndex = 0;
        keyValueFreePosition = currentPage.getMaxContentSize();
      }

      keyValueFreePosition -= keyValueContent.size();

      // WRITE KEY/VALUE PAIR CONTENT
      currentPageBuffer.putByteArray(keyValueFreePosition, keyValueContent.toByteArray());

      final int startPos = getHeaderSize(pageNum) + (keyIndex * INT_SERIALIZED_SIZE);
      if (keyIndex < count)
        // NOT LAST KEY, SHIFT POINTERS TO THE RIGHT
        currentPageBuffer.move(startPos, startPos + INT_SERIALIZED_SIZE, (count - keyIndex) * INT_SERIALIZED_SIZE);

      currentPageBuffer.putInt(startPos, keyValueFreePosition);

      setCount(currentPage, count + 1);
      setValuesFreePosition(currentPage, keyValueFreePosition);

      if (LogManager.instance().isDebugEnabled())
        LogManager.instance()
            .log(this, Level.FINE, "Put removed entry %s=%s (original=%s) in index '%s' (page=%s countInPage=%d newPage=%s)", null, Arrays.toString(keys),
                removedRID, rid, name, currentPage.getPageId(), count + 1, newPage);

    } catch (IOException e) {
      throw new DatabaseOperationException("Cannot index key '" + Arrays.toString(keys) + "' with value '" + rid + "' in index '" + name + "'", e);
    }
  }

  protected RID getRemovedRID(final Identifiable record) {
    final RID rid = record.getIdentity();
    return new RID(database, (rid.getBucketId() + 2) * -1, rid.getPosition());
  }
}
