package com.sysunite.coinsweb.connector.graphdb;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.sysunite.coinsweb.parser.config.ConfigFile;
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

      File configYml = new File(getClass().getClassLoader().getResource("config.yml").getFile());
      ConfigFile configFile = mapper.readValue(configYml, ConfigFile.class);

      GraphDB connector = new GraphDB(configFile.getEnvironment().getStore());
      log.info(connector.testConnection());

      File otl = new File(getClass().getClassLoader().getResource("otl-2.1/otl-2.1.ttl").getFile());
      log.info(connector.uploadFile(otl, "http://ns"));

//      log.info(connector.cleanup());

    } catch (Exception e) {
      System.out.println(e.getLocalizedMessage());
    }

  }
}