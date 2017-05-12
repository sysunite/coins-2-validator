package com.sysunite.coinsweb.connector;

import java.io.File;

/**
 * @author bastbijl, Sysunite 2017
 */
public interface Connector {

  boolean testConnection();
  boolean query(String queryString);
  boolean cleanup();
  boolean uploadFile(File file, String namespace);
}
