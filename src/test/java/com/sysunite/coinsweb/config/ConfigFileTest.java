package com.sysunite.coinsweb.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
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
      ConfigFile configFile = mapper.readValue(new File(getClass().getClassLoader().getResource("config.yml").getFile()), ConfigFile.class);
      System.out.println(ReflectionToStringBuilder.toString(configFile, ToStringStyle.MULTI_LINE_STYLE));
    } catch (Exception e) {
      System.out.println(e.getLocalizedMessage());
    }
  }
}
