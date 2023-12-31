package water.api;

import water.H2O;
import water.init.NodePersistentStorage;
import water.server.ServletUtils;
import water.util.FileUtils;
import water.util.Log;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 */
public class NpsBinServlet extends HttpServlet {
  
  private static final Pattern URL_PATTERN = Pattern.compile(".*/NodePersistentStorage.bin/([^/]+)/([^/]+)");

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) {
    String uri = ServletUtils.getDecodedUri(request);
    try {
      String[] params = ServletUtils.parseUriParams(uri, response, URL_PATTERN, 2);
      if (params == null) {
        return;
      }
      String categoryName = params[0];
      String keyName = params[1];

      NodePersistentStorage nps = H2O.getNPS();
      AtomicLong length = new AtomicLong();
      InputStream is = nps.get(categoryName, keyName, length);
      if (length.get() > (long) Integer.MAX_VALUE) {
        throw new Exception("NPS value size exceeds Integer.MAX_VALUE");
      }
      response.setContentType("application/octet-stream");
      response.setContentLength((int) length.get());
      response.addHeader("Content-Disposition", "attachment; filename=" + keyName + ".flow");
      ServletUtils.setResponseStatus(response, HttpServletResponse.SC_OK);
      OutputStream os = null;
      try {
        os = response.getOutputStream();
        FileUtils.copyStream(is, os, 2048);
      } finally {
        if (os != null) {
          try {
            os.close();
          }
          catch (Exception e) {
            Log.err(e);
          }
        }
      }
    } catch (Exception e) {
      ServletUtils.sendErrorResponse(response, e, uri);
    } finally {
      ServletUtils.logRequest("GET", request, response);
    }
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response) {
    String uri = ServletUtils.getDecodedUri(request);
    try {
      String[] params = ServletUtils.parseUriParams(uri, response, URL_PATTERN, 2);
      if (params == null) {
        return;
      }
      String categoryName = params[0];
      String keyName = params[1];

      try (InputStream is = ServletUtils.extractInputStream(request, response)) {
        if (is == null) {
          return;
        }

        H2O.getNPS().put(categoryName, keyName, is);
        long length = H2O.getNPS().get_length(categoryName, keyName);
        String responsePayload = "{ " +
                "\"category\" : " + "\"" + categoryName + "\", " +
                "\"name\" : " + "\"" + keyName + "\", " +
                "\"total_bytes\" : " + length + " " +
                "}\n";
        response.setContentType("application/json");
        response.getWriter().write(responsePayload);
      }
    } catch (Exception e) {
      ServletUtils.sendErrorResponse(response, e, uri);
    } finally {
      ServletUtils.logRequest("POST", request, response);
    }
  }
}
