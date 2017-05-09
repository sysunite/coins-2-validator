package com.sysunite.coinsweb.parser.config;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.util.StdConverter;
import org.apache.log4j.Logger;

import static com.sysunite.coinsweb.parser.config.Parser.isNotNull;

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

class EnvironmentSanitizer extends StdConverter<Environment, Environment> {

  private static final Logger log = Logger.getLogger(EnvironmentSanitizer.class);

  @Override
  public Environment convert(Environment obj) {
    isNotNull(obj.getStore());
    return obj;
  }
}
