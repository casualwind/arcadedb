/*
 * Copyright (c) - Arcade Data LTD (https://arcadedata.com)
 */
package com.arcadedb.server.ha;

public class ReplicationProtocol extends Thread {
  public static final long  MAGIC_NUMBER     = 20986405762943483l;
  public static final short PROTOCOL_VERSION = 0;

  // MESSAGES
  public static final short COMMAND_CONNECT            = 0;
  public static final short COMMAND_VOTE_FOR_ME        = 1;
  public static final short COMMAND_ELECTION_COMPLETED = 2;

  // CONNECT ERROR
  public static final byte ERROR_CONNECT_NOLEADER            = 0;
  public static final byte ERROR_CONNECT_ELECTION_PENDING    = 1;
  public static final byte ERROR_CONNECT_UNSUPPORTEDPROTOCOL = 2;
  public static final byte ERROR_CONNECT_WRONGCLUSTERNAME    = 3;
  public static final byte ERROR_CONNECT_SAME_SERVERNAME     = 4;
}