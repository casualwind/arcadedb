/*
 * Copyright (c) 2018 - Arcade Analytics LTD (https://arcadeanalytics.com)
 */

package com.arcadedb.server.ha;

import com.arcadedb.GlobalConfiguration;

public class ReplicationServerQuorumMajorityTest extends ReplicationServerTest {
  public ReplicationServerQuorumMajorityTest() {
    GlobalConfiguration.HA_QUORUM.setValue("Majority");
  }
}