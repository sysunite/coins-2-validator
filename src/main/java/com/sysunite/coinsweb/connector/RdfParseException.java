package com.sysunite.coinsweb.connector;

/**
 * @author bastbijl, Sysunite 2017
 */
public class RdfParseException extends Exception {

  public RdfParseException(String message) {
    super(message);
  }
  public RdfParseException(Exception original) {
    super(original);
  }
  public RdfParseException(String message, Exception original) {
    super(message, original);
  }
}
