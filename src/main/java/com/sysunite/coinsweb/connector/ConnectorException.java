package com.sysunite.coinsweb.connector;

/**
 * @author bastbijl, Sysunite 2017
 */
public class ConnectorException extends Exception {

  public ConnectorException(String message) {
    super(message);
  }
  public ConnectorException(Exception original) {
    super(original);
  }
  public ConnectorException(String message, Exception original) {
    super(message, original);
  }
}
