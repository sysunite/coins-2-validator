package com.sysunite.coinsweb.connector;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author bastbijl, Sysunite 2017
 */
public interface Connector {

  void init();
  boolean testConnection();
  Object query(String queryString);
  void update(String queryString);
  void cleanup();
  void uploadFile(File file, String[] contexts);
  void uploadFile(InputStream inputStream, String fileName, String baseUri, ArrayList<String> contexts);

  HashMap<String, Long> quadCount();
  boolean containsContext(String context);

  boolean requiresLoad();
  void setAllLoaded();

  String graphExists(String context);
}
