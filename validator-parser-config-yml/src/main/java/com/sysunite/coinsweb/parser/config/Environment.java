package com.sysunite.coinsweb.parser.config;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author bastbijl, Sysunite 2017
 */
@JsonDeserialize(converter=EnvironmentSanitizer.class)
public class Environment {

  private static final Logger log = LoggerFactory.getLogger(Environment.class);

  private Store store;
  private Mapping[] graphs;

  public Store getStore() {
    return store;
  }
  public Mapping[] getGraphs() {
    return graphs;
  }

  public void setStore(Store store) {
    this.store = store;
  }
  public void setGraphs(Mapping[] graphs) {
    this.graphs = graphs;
  }
}
