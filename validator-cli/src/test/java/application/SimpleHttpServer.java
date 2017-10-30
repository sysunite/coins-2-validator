package application;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.InetSocketAddress;
import java.util.ArrayList;

public class SimpleHttpServer {

  private static Logger log = LoggerFactory.getLogger(SimpleHttpServer.class);

  public static void serveFiles(ArrayList<File> files, String contentType, int port) {
    try {
      HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
      for(File file : files) {
        server.createContext("/" + file.getName(), new GetHandler(file, contentType));
      }
      server.setExecutor(null); // creates a default executor
      server.start();
    } catch(Exception e) {
      log.error(e.getMessage(), e);
    }
  }


  static class GetHandler implements HttpHandler {
    private File file;
    private String contentType;
    public GetHandler(File file, String contentType) {
      this.file = file;
      this.contentType = contentType;
    }
    public void handle(HttpExchange t) throws IOException {

      // add the required response header for a PDF file
      Headers h = t.getResponseHeaders();
      h.add("Content-Type", contentType);

      // the file
      byte [] bytearray  = new byte [(int)file.length()];
      FileInputStream fis = new FileInputStream(file);
      BufferedInputStream bis = new BufferedInputStream(fis);
      bis.read(bytearray, 0, bytearray.length);

      // ok, we are ready to send the response
      t.sendResponseHeaders(200, file.length());
      OutputStream os = t.getResponseBody();
      os.write(bytearray,0, bytearray.length);
      os.close();
    }
  }
}