package com.sysunite.coinsweb.parser.config;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.apache.log4j.Logger;

/**
 * @author bastbijl, Sysunite 2017
 */
@JsonDeserialize(converter=EnvironmentSanitizer.class)
public class Environment {

  private static final Logger log = Logger.getLogger(Environment.class);

  private Store store;

  public Store getStore() {
    return store;
  }

  public void setStore(Store store) {
    this.store = store;
  }
}
