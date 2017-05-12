package com.sysunite.coinsweb.connector;

/**
 * @author bastbijl, Sysunite 2017
 */
public interface ConnectorFactory {
  boolean exists(String key);
  Class<? extends Connector> get(String key);
  Connector build(Object config);
}
