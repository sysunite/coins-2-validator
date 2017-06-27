package com.sysunite.coinsweb.parser.config.pojo;

import com.fasterxml.jackson.databind.util.StdConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.sysunite.coinsweb.parser.Parser.isNotNull;

/**
 * @author bastbijl, Sysunite 2017
 */
class EnvironmentSanitizer extends StdConverter<Environment, Environment> {

  private static final Logger log = LoggerFactory.getLogger(EnvironmentSanitizer.class);

  @Override
  public Environment convert(Environment obj) {
    isNotNull(obj.getStore());
    isNotNull(obj.getLoadingStrategy());

    return obj;
  }
}
