package com.sysunite.coinsweb.connector;

/**
 * @author bastbijl, Sysunite 2017
 */
public interface Connector {

  boolean connect();
  boolean testConnection();
}
