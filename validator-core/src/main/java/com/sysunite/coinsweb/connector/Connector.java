package com.sysunite.coinsweb.connector;

import java.io.File;
import java.io.InputStream;

/**
 * @author bastbijl, Sysunite 2017
 */
public interface Connector {

  boolean testConnection();
  boolean query(String queryString);
  boolean cleanup();
  void uploadFile(File file, String[] contexts);
  void uploadFile(InputStream inputStream, String fileName, String baseUri, String[] contexts);

  boolean requiresLoad();
  void setAllLoaded();
}
