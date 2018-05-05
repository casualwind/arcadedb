package com.arcadedb.remote;

import com.arcadedb.sql.executor.OInternalResultSet;
import com.arcadedb.sql.executor.OResultInternal;
import com.arcadedb.sql.executor.OResultSet;
import com.arcadedb.utility.PFileUtils;
import com.arcadedb.utility.PRWLockContext;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Scanner;

public class PRemoteDatabase extends PRWLockContext {
  public interface Callback {
    Object call(HttpURLConnection iArgument, JSONObject response) throws Exception;
  }

  private final String server;
  private final int    port;
  private final String name;
  private       String protocol = "http";
  private       String charset  = "UTF-8";

  public PRemoteDatabase(final String server, final int port, final String name) {
    this.server = server;
    this.port = port;
    this.name = name;
  }

  public void close() {
  }

  public OResultSet command(final String command) {
    return (OResultSet) connect("command", command, new Callback() {
      @Override
      public Object call(final HttpURLConnection connection, final JSONObject response) throws Exception {
        final OResultSet resultSet = new OInternalResultSet();

        final JSONArray resultArray = response.getJSONArray("result");
        for (int i = 0; i < resultArray.length(); ++i) {
          final JSONObject result = resultArray.getJSONObject(i);
          ((OInternalResultSet) resultSet).add(new OResultInternal(result.toMap()));
        }

        return resultSet;
      }
    });
  }

  @Override
  public String toString() {
    return name;
  }

  private Object connect(final String operation, final String command, final Callback callback) {
    final HttpURLConnection connection;
    try {
      final String url =
          protocol + "://" + server + ":" + port + "/" + operation + "/" + name + "/" + URLEncoder.encode(command, charset);

      connection = (HttpURLConnection) new URL(url).openConnection();
      try {
        connection.setRequestMethod("POST");
//        connection.setRequestProperty("Content-Type", "application/json; charset=" + charset);
//        connection.setDoInput(true);
//        connection.setDoOutput(true);
//        OutputStream os = connection.getOutputStream();
//        os.write(payload.toString().getBytes("UTF-8"));
//        os.close();

        connection.connect();

        if (connection.getResponseCode() != 200) {
          throw new PRemoteException(
              "Error executing remote command (httpError=" + connection.getResponseCode() + " cause=" + connection
                  .getResponseMessage() + ")");
        }

        final JSONObject response = new JSONObject(PFileUtils.readStreamAsString(connection.getInputStream(), charset));

        return callback.call(connection, response);

      } finally {
        connection.disconnect();
      }
    } catch (PRemoteException e) {
      throw e;
    } catch (Exception e) {
      throw new PRemoteException("Error on executing remote operation " + operation, e);
    }
  }

  protected String readResponse(final HttpURLConnection connection) throws IOException {
    InputStream in = connection.getInputStream();
    Scanner scanner = new Scanner(in);

    final StringBuilder buffer = new StringBuilder();

    while (scanner.hasNext()) {
      buffer.append(scanner.next().replace('\n', ' '));
    }

    return buffer.toString();
  }
}