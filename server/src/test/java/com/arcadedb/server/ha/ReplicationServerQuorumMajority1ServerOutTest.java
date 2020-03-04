/*
 * Copyright (c) - Arcade Data LTD (https://arcadedata.com)
 */

package com.arcadedb.server.ha;

import com.arcadedb.GlobalConfiguration;
import com.arcadedb.log.LogManager;
import com.arcadedb.server.ArcadeDBServer;
import com.arcadedb.server.TestCallback;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

public class ReplicationServerQuorumMajority1ServerOutTest extends ReplicationServerTest {
  private final AtomicInteger messages = new AtomicInteger();

  public ReplicationServerQuorumMajority1ServerOutTest() {
    GlobalConfiguration.HA_QUORUM.setValue("Majority");
  }

  @Override
  protected void onBeforeStarting(final ArcadeDBServer server) {
    if (server.getServerName().equals("ArcadeDB_2"))
      server.registerTestEventListener(new TestCallback() {
        @Override
        public void onEvent(final TYPE type, final Object object, final ArcadeDBServer server) {
          if (type == TYPE.REPLICA_MSG_RECEIVED) {
            if (messages.incrementAndGet() > 100) {
              LogManager.instance().log(this, Level.INFO, "TEST: Stopping Replica 2...");
              getServer(2).stop();
            }
          }
        }
      });
  }

  protected int[] getServerToCheck() {
    final int[] result = new int[getServerCount() - 1];
    for (int i = 0; i < result.length; ++i)
      result[i] = i;
    return result;
  }

  @Override
  protected int getTxs() {
    return 300;
  }

}