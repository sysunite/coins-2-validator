package com.sysunite.coinsweb.connector.graphdb;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.sysunite.coinsweb.connector.ConnectorException;
import com.sysunite.coinsweb.connector.ConnectorFactoryImpl;
import com.sysunite.coinsweb.filemanager.DescribeFactoryImpl;
import com.sysunite.coinsweb.parser.config.factory.ConfigFactory;
import com.sysunite.coinsweb.parser.config.pojo.ConfigFile;
import com.sysunite.coinsweb.parser.config.pojo.StepDeserializer;
import com.sysunite.coinsweb.parser.config.pojo.Store;
import com.sysunite.coinsweb.steps.StepFactoryImpl;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author bastbijl, Sysunite 2017
 */
public class GraphDBTest {
  Logger log = LoggerFactory.getLogger(GraphDBTest.class);

  @BeforeClass
  public static void before() {
    Store.factory = new ConnectorFactoryImpl();
    StepDeserializer.factory = new StepFactoryImpl();
    ConfigFactory.setDescribeFactory(new DescribeFactoryImpl());
  }

  @Test
  public void testConnection() throws IOException, ConnectorException {

    // Create the connection
    ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
    File configYml = new File(getClass().getResource("minimal-graphdb.yml").getFile());
    ConfigFile configFile = mapper.readValue(configYml, ConfigFile.class);

    GraphDB connector = new GraphDB(configFile.getEnvironment());
    assertTrue(connector.testConnection());

    // Upload a file
    File otl = new File(getClass().getClassLoader().getResource("otl-2.1/otl-2.1.ttl").getFile());
    ArrayList<String> contexts = new ArrayList<>();
    contexts.add("http://otl.rws.nl/");
    String defaultFileName = "otl-2.1.ttl";
    connector.uploadFile(new FileInputStream(otl), defaultFileName, contexts.get(0), contexts);
    assertEquals(contexts.get(0), connector.getContexts().get(0));
    assertEquals(455384, connector.quadCount("http://otl.rws.nl/"));

    // Cleanup
    connector.cleanup(contexts);
    assertEquals(0, connector.getContexts().size());
  }
}