package com.sysunite.coinsweb.connector.graphdb;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.sysunite.coinsweb.config.ConfigFile;
import org.apache.log4j.Logger;
import org.junit.Test;

import java.io.File;


/**
 * @author bastbijl, Sysunite 2017
 */
public class GraphDBTest {

  Logger log = Logger.getLogger(GraphDBTest.class);

  @Test
  public void test() {

    ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
    try {
      File file = new File(getClass().getClassLoader().getResource("config.yml").getFile());
      ConfigFile configFile = mapper.readValue(file, ConfigFile.class);

      GraphDB connector = new GraphDB(configFile.getEnvironment().getStore().getEndpoint());
      log.info(connector.testConnection());
    } catch (Exception e) {
      System.out.println(e.getLocalizedMessage());
    }

  }
}