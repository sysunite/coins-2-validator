package com.sysunite.coinsweb.config;

/**
 * @author bastbijl, Sysunite 2017
 */
public class Environment {
  private Model[] models;
  private Store store;

  public Model[] getModels() {
    return models;
  }

  public void setModels(Model[] models) {
    this.models = models;
  }

  public Store getStore() {
    return store;
  }

  public void setStore(Store store) {
    this.store = store;
  }
}
