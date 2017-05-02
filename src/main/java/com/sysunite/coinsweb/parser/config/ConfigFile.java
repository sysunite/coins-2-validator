package com.sysunite.coinsweb.parser.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.util.StdConverter;
import org.apache.log4j.Logger;

import static com.sysunite.coinsweb.parser.config.Parser.isNotNull;

/**
 * @author bastbijl, Sysunite 2017
 */
@JsonInclude(Include.NON_NULL)
@JsonDeserialize(converter=ConfigFileSanitizer.class)
public class ConfigFile {

  private static final Logger log = Logger.getLogger(ConfigFile.class);

  private Environment environment;
  private Run run;

  public Run getRun() {
    return run;
  }
  public Environment getEnvironment() {
    return environment;
  }

  public void setRun(Run run) {
    this.run = run;
  }

  public void setEnvironment(Environment environment) {
    this.environment = environment;
  }
}


class ConfigFileSanitizer extends StdConverter<ConfigFile, ConfigFile> {

  private static final Logger log = Logger.getLogger(ConfigFileSanitizer.class);
  
  @Override
  public ConfigFile convert(ConfigFile obj) {

    isNotNull(obj.getEnvironment());
    isNotNull(obj.getRun());
    return obj;
  }
}
