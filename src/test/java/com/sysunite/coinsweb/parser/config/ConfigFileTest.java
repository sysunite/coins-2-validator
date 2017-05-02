package com.sysunite.coinsweb.parser.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.log4j.Logger;
import org.junit.Test;

import java.io.File;


/**
 * @author bastbijl, Sysunite 2017
 */
public class ConfigFileTest {

  Logger log = Logger.getLogger(ConfigFileTest.class);

  @Test
  public void test() {

    ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
    try {
      File file = new File(getClass().getClassLoader().getResource("config.yml").getFile());
      ConfigFile configFile = mapper.readValue(file, ConfigFile.class);
      for(Step step : configFile.getRun().getSteps()) {
        log.warn(step.getValidationStep().getClass().toString());
      }
      log.warn(configFile);
    } catch (Exception e) {
      System.out.println(e.getLocalizedMessage());
    }
  }
}