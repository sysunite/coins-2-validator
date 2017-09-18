package com.sysunite.coinsweb.connector.stardog;

import application.SimpleHttpServer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.sysunite.coinsweb.connector.ConnectorFactoryImpl;
import com.sysunite.coinsweb.parser.config.pojo.ConfigFile;
import com.sysunite.coinsweb.parser.config.pojo.StepDeserializer;
import com.sysunite.coinsweb.parser.config.pojo.Store;
import com.sysunite.coinsweb.steps.StepFactoryImpl;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;


/**
 * @author bastbijl, Sysunite 2017
 */
public class StardogTest {

  Logger log = LoggerFactory.getLogger(StardogTest.class);

  static {
    File profile = new File("/Users/bastiaanbijl/Documents/Sysunite/GitHub/Sysunite/coins-2-validator/validator-cli/src/test/resources/profiles/profile.lite-9.85-virtuoso.xml");
    SimpleHttpServer.serveFile(profile, "application/xml", 9877);
  }

  @Test
  public void test() {

    Store.factory = new ConnectorFactoryImpl();
    StepDeserializer.factory = new StepFactoryImpl();
    ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
    try {

      File configYml = new File(getClass().getClassLoader().getResource("general-9.85-stardog.yml").getFile());
      ConfigFile configFile = mapper.readValue(configYml, ConfigFile.class);

      Stardog connector = new Stardog(configFile.getEnvironment());
      log.info(""+connector.testConnection());

//      File file = new File(getClass().getClassLoader().getResource("dataroom-1.43/bim/repository/rws-coins-20-referentiekader-2.0.ttl").getFile());
      File file = new File(getClass().getClassLoader().getResource("dataroom-1.43/bim/Dataroom-1.3-coins2-otl-2.1.ttl").getFile());
      ArrayList<String> contexts = new ArrayList<>();
      contexts.add("http://ns");
      connector.uploadFile(file, contexts);



    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }

  }
}