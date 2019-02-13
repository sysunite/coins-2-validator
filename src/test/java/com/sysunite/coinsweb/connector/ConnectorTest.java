package com.sysunite.coinsweb.connector;

import application.run.HostFiles;
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author bastbijl, Sysunite 2017
 */
public class ConnectorTest extends HostFiles {

  Logger log = LoggerFactory.getLogger(ConnectorTest.class);

  @Test
  public void listMapping() {

    Store.factory = new ConnectorFactoryImpl();
    StepDeserializer.factory = new StepFactoryImpl();
    ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
    try {

      File configYml = new File(getClass().getClassLoader().getResource("general-9.85.yml").getFile());
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

      File configYml = new File(getClass().getClassLoader().getResource("general-9.85.yml").getFile());
      ConfigFile configFile = mapper.readValue(configYml, ConfigFile.class);

      Connector connector = new GraphDB(configFile.getEnvironment());
      log.info(""+connector.testConnection());

      List list = connector.listPhiGraphs();

      log.info(ReportFactory.buildJson(list));
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }
  }

  @Test
  public void exportRdf() {

    Store.factory = new ConnectorFactoryImpl();
    StepDeserializer.factory = new StepFactoryImpl();
    ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
    try {

      File configYml = new File(getClass().getClassLoader().getResource("general-9.85.yml").getFile());
      ConfigFile configFile = mapper.readValue(configYml, ConfigFile.class);

      Connector connector = new GraphDB(configFile.getEnvironment());
      log.info(""+connector.testConnection());

      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      HashMap<String, String> namespaceMap = new HashMap<>();
      namespaceMap.put("otl", "http://otl.rws.nl/otl#");
      namespaceMap.put("owl", "http://www.w3.org/2002/07/owl#");
      namespaceMap.put("cbim", "http://www.coinsweb.nl/cbim-2.0.rdf#");
      ArrayList<String> contexts = new ArrayList<>();
      contexts.add("http://coins-commander.com/container-main-graph#");
      connector.writeContextsToFile(contexts, baos, namespaceMap, "http://coins-commander.com/container-main-graph#");

      log.info(baos.toString("UTF-8"));
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }
  }
}