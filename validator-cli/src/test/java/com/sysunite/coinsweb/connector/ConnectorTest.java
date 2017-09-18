package com.sysunite.coinsweb.connector;

import application.SimpleHttpServer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.sysunite.coinsweb.connector.graphdb.GraphDB;
import com.sysunite.coinsweb.parser.config.pojo.ConfigFile;
import com.sysunite.coinsweb.parser.config.pojo.StepDeserializer;
import com.sysunite.coinsweb.parser.config.pojo.Store;
import com.sysunite.coinsweb.report.ReportFactory;
import com.sysunite.coinsweb.steps.StepFactoryImpl;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;


/**
 * @author bastbijl, Sysunite 2017
 */
public class ConnectorTest {

  Logger log = LoggerFactory.getLogger(ConnectorTest.class);

  static {
    File profile = new File("/Users/bastiaanbijl/Documents/Sysunite/GitHub/Sysunite/coins-2-validator/validator-cli/src/test/resources/profiles/profile.lite-9.85-generated.xml");
    SimpleHttpServer.serveFile(profile, "application/xml", 9877);
  }

  @Test
  public void listMapping() {

    Store.factory = new ConnectorFactoryImpl();
    StepDeserializer.factory = new StepFactoryImpl();
    ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
    try {

      File configYml = new File(getClass().getClassLoader().getResource("general-9.85-virtuoso.yml").getFile());
      ConfigFile configFile = mapper.readValue(configYml, ConfigFile.class);

      Connector connector = new GraphDB(configFile.getEnvironment());
      assert(connector.testConnection());



      List list = connector.listMappings();

      log.info(ReportFactory.buildJson(list));



    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }

  }

  @Test
  public void listPhiGraphs() {

    Store.factory = new ConnectorFactoryImpl();
    StepDeserializer.factory = new StepFactoryImpl();
    ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
    try {

      File configYml = new File(getClass().getClassLoader().getResource("general-9.85-graphdb.yml").getFile());
      ConfigFile configFile = mapper.readValue(configYml, ConfigFile.class);

      Connector connector = new GraphDB(configFile.getEnvironment());
      log.info(""+connector.testConnection());



      List list = connector.listPhiGraphs();

      log.info(ReportFactory.buildJson(list));



    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }

  }
}