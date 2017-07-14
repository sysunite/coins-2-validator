package com.sysunite.coinsweb.connector;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author bastbijl, Sysunite 2017
 */
public interface Connector {

  void init();
  boolean testConnection();

  List<Object> query(String queryString);
  void update(String queryString);

  void cleanup();
  void close();
  void uploadFile(File file, String[] contexts);
  void uploadFile(InputStream inputStream, String fileName, String baseUri, ArrayList<String> contexts);

  HashMap<String, Long> quadCount();
  boolean containsContext(String context);

  String graphExists(String context);
}
