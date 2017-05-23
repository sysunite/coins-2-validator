package com.sysunite.coinsweb.parser.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;

/**
 * @author bastbijl, Sysunite 2017
 */
@JsonInclude(Include.NON_NULL)
@JsonDeserialize(converter=ConfigFileSanitizer.class)
public class ConfigFile {

  private static final Logger log = LoggerFactory.getLogger(ConfigFile.class);

  private Environment environment;
  private Run run;

  public static ConfigFile parse(File file) {
    ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
    String message = "";
    try {
      return mapper.readValue(file, ConfigFile.class);
    } catch (Exception e) {
      message = e.getMessage();
    }
    throw new RuntimeException("Was not able to parse config file: "+message);
  }

  public static ConfigFile parse(InputStream inputStream) {
    ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
    String message = "";
    try {
      return mapper.readValue(inputStream, ConfigFile.class);
    } catch (Exception e) {
      message = e.getMessage();
    }
    throw new RuntimeException("Was not able to parse config file: "+message);
  }

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
