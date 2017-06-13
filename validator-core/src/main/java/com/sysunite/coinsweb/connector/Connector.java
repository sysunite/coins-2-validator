package com.sysunite.coinsweb.connector;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;

/**
 * @author bastbijl, Sysunite 2017
 */
public interface Connector {

  void init();
  boolean testConnection();
  Object query(String queryString);
  void update(String queryString);
  boolean cleanup();
  void uploadFile(File file, String[] contexts);
  void uploadFile(InputStream inputStream, String fileName, String baseUri, String[] contexts);

  HashMap<String, Long> quadCount();

  boolean requiresLoad();
  void setAllLoaded();
}
