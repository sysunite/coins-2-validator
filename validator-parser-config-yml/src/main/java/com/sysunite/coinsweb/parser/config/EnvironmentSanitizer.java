package com.sysunite.coinsweb.parser.config;

import com.fasterxml.jackson.databind.util.StdConverter;
import org.apache.log4j.Logger;

import static com.sysunite.coinsweb.parser.Parser.isNotNull;

/**
 * @author bastbijl, Sysunite 2017
 */
class EnvironmentSanitizer extends StdConverter<Environment, Environment> {

  private static final Logger log = Logger.getLogger(EnvironmentSanitizer.class);

  @Override
  public Environment convert(Environment obj) {
    isNotNull(obj.getStore());
    return obj;
  }
}
