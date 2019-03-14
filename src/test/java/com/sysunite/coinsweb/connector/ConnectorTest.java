package com.sysunite.coinsweb.connector;

import application.run.HostFiles;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.sysunite.coinsweb.connector.graphdb.GraphDB;
import com.sysunite.coinsweb.filemanager.DescribeFactoryImpl;
import com.sysunite.coinsweb.parser.config.factory.ConfigFactory;
import com.sysunite.coinsweb.parser.config.pojo.ConfigFile;
import com.sysunite.coinsweb.parser.config.pojo.StepDeserializer;
import com.sysunite.coinsweb.parser.config.pojo.Store;
import com.sysunite.coinsweb.report.ReportFactory;
import com.sysunite.coinsweb.steps.StepFactoryImpl;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * @author bastbijl, Sysunite 2017
 */
public class ConnectorTest extends HostFiles {
  Logger log = LoggerFactory.getLogger(ConnectorTest.class);

  private boolean visuallyInspect = false;

  @BeforeClass
  public static void before() {
    Store.factory = new ConnectorFactoryImpl();
    StepDeserializer.factory = new StepFactoryImpl();
    ConfigFactory.setDescribeFactory(new DescribeFactoryImpl());
  }

  @Test
  public void listMapping() throws IOException, ConnectorException {
    ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
    File configYml = new File(getClass().getClassLoader().getResource("general-9.85.yml").getFile());
    ConfigFile configFile = mapper.readValue(configYml, ConfigFile.class);

    Connector connector = new GraphDB(configFile.getEnvironment());
    assertTrue(connector.testConnection());
    List list = connector.listMappings();
    String json = ReportFactory.buildJson(list);
    inspect(json);
  }

  @Test
  public void listPhiGraphs() throws IOException, ConnectorException {
    ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
    File configYml = new File(getClass().getClassLoader().getResource("general-9.85.yml").getFile());
    ConfigFile configFile = mapper.readValue(configYml, ConfigFile.class);

    Connector connector = new GraphDB(configFile.getEnvironment());
    assertTrue(connector.testConnection());
    List list = connector.listPhiGraphs();
    String json = ReportFactory.buildJson(list);
    inspect(json);
  }

  @Test
  public void exportRdf() throws IOException {
    ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

    File configYml = new File(getClass().getClassLoader().getResource("general-9.85.yml").getFile());
    ConfigFile configFile = mapper.readValue(configYml, ConfigFile.class);

    Connector connector = new GraphDB(configFile.getEnvironment());
    assertTrue(connector.testConnection());

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    HashMap<String, String> namespaceMap = new HashMap<>();
    namespaceMap.put("otl", "http://otl.rws.nl/otl#");
    namespaceMap.put("owl", "http://www.w3.org/2002/07/owl#");
    namespaceMap.put("cbim", "http://www.coinsweb.nl/cbim-2.0.rdf#");

    ArrayList<String> contexts = new ArrayList<>();
    contexts.add("http://coins-commander.com/container-main-graph#");
    connector.writeContextsToFile(contexts, baos, namespaceMap, "http://coins-commander.com/container-main-graph#");

    String xml = baos.toString("UTF-8");
    inspect(xml);
  }

  private void inspect(String message) {
    if(visuallyInspect) {
      System.out.println(message);
    }
  }
}