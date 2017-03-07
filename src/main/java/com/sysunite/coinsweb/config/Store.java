package com.sysunite.coinsweb.config;

/**
 * @author bastbijl, Sysunite 2017
 */
public class Store {
  private String type;
  private Endpoint endpoint;

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public Endpoint getEndpoint() {
    return endpoint;
  }

  public void setEndpoint(Endpoint endpoint) {
    this.endpoint = endpoint;
  }
}
