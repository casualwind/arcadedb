/*
 * Copyright (c) - Arcade Data LTD (https://arcadedata.com)
 */

package com.arcadedb.console;

import com.arcadedb.remote.RemoteException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class RemoteConsoleTest extends BaseGraphServerTest {
  private static final String URL               = "remote:localhost:2480/console root root";
  private static final String URL_SHORT         = "remote:localhost/console root root";
  private static final String URL_NOCREDENTIALS = "remote:localhost/console";
  private static final String URL_WRONGPASSWD   = "remote:localhost/console root wrong";

  private static Console console;

  @Override
  protected boolean isPopulateDatabase() {
    return false;
  }

  @Override
  protected String getDatabaseName() {
    return "console";
  }

  @BeforeEach
  public void beginTest() {
    deleteDatabaseFolders();
    startServers();

    try {
      console = new Console(false);
      Assertions.assertTrue(console.parse("create database " + URL, false));

      console.parse("close", false);
    } catch (IOException e) {
      Assertions.fail(e);
    }
  }

  @AfterEach
  public void endTest() {
    super.endTest();
    if (console != null)
      console.close();
  }

  @Test
  public void testConnect() throws IOException {
    Assertions.assertTrue(console.parse("connect " + URL, false));
  }

  @Test
  public void testConnectShortURL() throws IOException {
    Assertions.assertTrue(console.parse("connect " + URL_SHORT, false));
  }

  @Test
  public void testConnectNoCredentials() throws IOException {
    try {
      Assertions.assertTrue(console.parse("connect " + URL_NOCREDENTIALS + ";create document type VVVV", false));
      Assertions.fail("Security was bypassed!");
    } catch (ConsoleException e) {
    }
  }

  @Test
  public void testConnectWrongPassword() throws IOException {
    try {
      Assertions.assertTrue(console.parse("connect " + URL_WRONGPASSWD + ";create document type VVVV", false));
      Assertions.fail("Security was bypassed!");
    } catch (RemoteException e) {
    }
  }

  @Test
  public void testCreateType() throws IOException {
    Assertions.assertTrue(console.parse("connect " + URL, false));
    Assertions.assertTrue(console.parse("create document type Person2", false));

    final StringBuilder buffer = new StringBuilder();
    console.setOutput(new ConsoleOutput() {
      @Override
      public void onOutput(final String output) {
        buffer.append(output);
      }
    });
    Assertions.assertTrue(console.parse("info types", false));
    Assertions.assertTrue(buffer.toString().contains("Person2"));
    Assertions.assertTrue(console.parse("drop type Person2", false));
  }

  @Test
  public void testInsertAndSelectRecord() throws IOException {
    Assertions.assertTrue(console.parse("connect " + URL, false));
    Assertions.assertTrue(console.parse("create document type Person2", false));
    Assertions.assertTrue(console.parse("insert into Person2 set name = 'Jay', lastname='Miner'", false));

    final StringBuilder buffer = new StringBuilder();
    console.setOutput(new ConsoleOutput() {
      @Override
      public void onOutput(final String output) {
        buffer.append(output);
      }
    });
    Assertions.assertTrue(console.parse("select from Person2", false));
    Assertions.assertTrue(buffer.toString().contains("Jay"));
    Assertions.assertTrue(console.parse("drop type Person2", false));
  }
//
//  @Test
//  public void testInsertAndRollback() throws IOException {
//    Assertions.assertTrue(console.parse("connect " + URL));
//    Assertions.assertTrue(console.parse("begin"));
//    Assertions.assertTrue(console.parse("create document type Person"));
//    Assertions.assertTrue(console.parse("insert into Person set name = 'Jay', lastname='Miner'"));
//    Assertions.assertTrue(console.parse("rollback"));
//
//    final StringBuilder buffer = new StringBuilder();
//    console.setOutput(new ConsoleOutput() {
//      @Override
//      public void onOutput(final String output) {
//        buffer.append(output);
//      }
//    });
//    Assertions.assertTrue(console.parse("select from Person"));
//    Assertions.assertFalse(buffer.toString().contains("Jay"));
//  }

  @Test
  public void testHelp() throws IOException {
    final StringBuilder buffer = new StringBuilder();
    console.setOutput(new ConsoleOutput() {
      @Override
      public void onOutput(final String output) {
        buffer.append(output);
      }
    });
    Assertions.assertTrue(console.parse("?", false));
    Assertions.assertTrue(buffer.toString().contains("quit"));
  }
}