package com.sysunite.coinsweb.config;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.util.StdConverter;
import org.apache.log4j.Logger;

import static com.sysunite.coinsweb.config.Parser.isNotNull;

/**
 * @author bastbijl, Sysunite 2017
 */
@JsonDeserialize(converter=EnvironmentSanitizer.class)
public class Environment {

  private static Logger log = Logger.getLogger(Environment.class);

  private Store store;

  public Store getStore() {
    return store;
  }

  public void setStore(Store store) {
    this.store = store;
  }
}

class EnvironmentSanitizer extends StdConverter<Environment, Environment> {

  private static Logger log = Logger.getLogger(EnvironmentSanitizer.class);

  @Override
  public Environment convert(Environment obj) {
    // todo: should be some form of checking the model definitions
    isNotNull(obj.getStore());
    return obj;
  }
}
