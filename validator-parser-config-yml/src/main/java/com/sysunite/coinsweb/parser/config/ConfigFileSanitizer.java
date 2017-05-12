package com.sysunite.coinsweb.parser.config;

import com.fasterxml.jackson.databind.util.StdConverter;
import org.apache.log4j.Logger;

import static com.sysunite.coinsweb.parser.Parser.isNotNull;

/**
 * @author bastbijl, Sysunite 2017
 */
class ConfigFileSanitizer extends StdConverter<ConfigFile, ConfigFile> {

  private static final Logger log = Logger.getLogger(ConfigFileSanitizer.class);
  
  @Override
  public ConfigFile convert(ConfigFile obj) {

    isNotNull(obj.getEnvironment());
    isNotNull(obj.getRun());
    return obj;
  }
}
