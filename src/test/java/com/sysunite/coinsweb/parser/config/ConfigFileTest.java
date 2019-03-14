package com.sysunite.coinsweb.parser.config;

import com.sun.corba.se.impl.orbutil.graph.GraphImpl;
import com.sysunite.coinsweb.connector.ConnectorFactoryImpl;
import com.sysunite.coinsweb.filemanager.DescribeFactoryImpl;
import com.sysunite.coinsweb.parser.config.factory.ConfigFactory;
import com.sysunite.coinsweb.parser.config.pojo.*;
import com.sysunite.coinsweb.steps.StepFactoryImpl;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author bastbijl, Sysunite 2017
 */
public class ConfigFileTest {
  Logger log = LoggerFactory.getLogger(ConfigFileTest.class);

  private boolean visuallyInspect = true;

  @BeforeClass
  public static void before() {
    Store.factory = new ConnectorFactoryImpl();
    StepDeserializer.factory = new StepFactoryImpl();
    ConfigFactory.setDescribeFactory(new DescribeFactoryImpl());
  }

  @Test
  public void voorbeeldContainer() {
    ConfigFile configFile = ConfigFile.parse(new File(getClass().getResource("voorbeeldcontainer-full.yml").getFile()));
    ConfigFactory.overrideContainers(configFile, 1, new Path[]{Paths.get("../../../../../voorbeeldcontainer-6.ccr")});
    inspect(ConfigFactory.toYml(configFile));

    List<Graph> graphs;
    Graph graph;

    // Before expand
    graphs = configFile.getRun().getContainers()[0].getGraphs();
    assertEquals(4, graphs.size());
    graph = graphs.get(0);
    assertEquals("bim/*", graph.getSource().getPath());
    assertEquals("*", graph.getSource().getGraphname());
    assertEquals("*", graph.getSource().getDefaultFileName());
    graph = graphs.get(1);
    assertEquals("bim/repository/*", graph.getSource().getPath());
    assertEquals("*", graph.getSource().getGraphname());
    assertEquals("*", graph.getSource().getDefaultFileName());

    DescribeFactoryImpl.expandGraphConfig(configFile);

    // After expand
    graphs = configFile.getRun().getContainers()[0].getGraphs();
    assertEquals(6, graphs.size());
    graph = graphs.get(0);
    assertEquals(new GraphVarImpl("INSTANCE_UNION_GRAPH"), graph.getSource().getGraph());
    assertEquals("file.2.rdf", graph.getSource().getDefaultFileName());
    graph = graphs.get(1);
    assertEquals(new GraphVarImpl("SCHEMA_UNION_GRAPH"), graph.getSource().getGraph());
    assertEquals("file.3.rdf", graph.getSource().getDefaultFileName());
    graph = graphs.get(2);
    assertEquals("bim/repository/rws-coins-20-referentiekader-21.ttl", graph.getSource().getPath());
    assertEquals("http://otl.rws.nl/coins2/rws-referentiekader.rdf", graph.getSource().getGraphname());
    assertEquals("rws-coins-20-referentiekader-21.ttl", graph.getSource().getDefaultFileName());
    graph = graphs.get(3);
    assertEquals("bim/repository/coins2.0.rdf", graph.getSource().getPath());
    assertEquals("http://www.coinsweb.nl/cbim-2.0.rdf", graph.getSource().getGraphname());
    assertEquals("coins2.0.rdf", graph.getSource().getDefaultFileName());
    graph = graphs.get(4);
    assertEquals("bim/testdata_211.ttl", graph.getSource().getPath());
    assertEquals("http://areaal.rws.nl/S.001032/07032018", graph.getSource().getGraphname());
    assertEquals("testdata_211.ttl", graph.getSource().getDefaultFileName());
    graph = graphs.get(5);
    assertEquals("bim/repository/20180131_otl22_acceptatie_definitief.ttl", graph.getSource().getPath());
    assertEquals("http://otl.rws.nl/", graph.getSource().getGraphname());
    assertEquals("20180131_otl22_acceptatie_definitief.ttl", graph.getSource().getDefaultFileName());

    inspect(ConfigFactory.toYml(configFile));
  }

  @Test
  public void voorbeeldContainerOnlyCbimFile() {
    ConfigFile configFile = ConfigFile.parse(new File(getClass().getResource("voorbeeldcontainer-only-cbim-file.yml").getFile()));
    ConfigFactory.overrideContainers(configFile, 1, new Path[]{Paths.get("../../../../../voorbeeldcontainer-6.ccr")});
    inspect(ConfigFactory.toYml(configFile));

    List<Graph> graphs;
    Graph graph;

    // Before expand
    graphs = configFile.getRun().getContainers()[0].getGraphs();
    assertEquals(4, graphs.size());
    graph = graphs.get(0);
    assertEquals("bim/*", graph.getSource().getPath());
    assertEquals("*", graph.getSource().getGraphname());
    assertEquals("*", graph.getSource().getDefaultFileName());
    graph = graphs.get(1);
    assertEquals("bim/repository/coins*", graph.getSource().getPath());
    assertEquals("*", graph.getSource().getGraphname());
    assertEquals("coins*", graph.getSource().getDefaultFileName());

    DescribeFactoryImpl.expandGraphConfig(configFile);

    // After expand
    graphs = configFile.getRun().getContainers()[0].getGraphs();
    assertEquals(4, graphs.size());
    graph = graphs.get(0);
    assertEquals(new GraphVarImpl("INSTANCE_UNION_GRAPH"), graph.getSource().getGraph());
    assertEquals("file.2.rdf", graph.getSource().getDefaultFileName());
    graph = graphs.get(1);
    assertEquals(new GraphVarImpl("SCHEMA_UNION_GRAPH"), graph.getSource().getGraph());
    assertEquals("file.3.rdf", graph.getSource().getDefaultFileName());
    graph = graphs.get(2);
    assertEquals("bim/repository/coins2.0.rdf", graph.getSource().getPath());
    assertEquals("http://www.coinsweb.nl/cbim-2.0.rdf", graph.getSource().getGraphname());
    assertEquals("coins2.0.rdf", graph.getSource().getDefaultFileName());
    graph = graphs.get(3);
    assertEquals("bim/testdata_211.ttl", graph.getSource().getPath());
    assertEquals("http://areaal.rws.nl/S.001032/07032018", graph.getSource().getGraphname());
    assertEquals("testdata_211.ttl", graph.getSource().getDefaultFileName());

    inspect(ConfigFactory.toYml(configFile));
  }

  @Test
  public void voorbeeldContainerOnlyCbimNamespace() {
    ConfigFile configFile = ConfigFile.parse(new File(getClass().getResource("voorbeeldcontainer-only-cbim-namespace.yml").getFile()));
    ConfigFactory.overrideContainers(configFile, 1, new Path[]{Paths.get("../../../../../voorbeeldcontainer-6.ccr")});
    inspect(ConfigFactory.toYml(configFile));

    List<Graph> graphs;
    Graph graph;

    // Before expand
    graphs = configFile.getRun().getContainers()[0].getGraphs();
    assertEquals(4, graphs.size());
    graph = graphs.get(0);
    assertEquals("bim/*", graph.getSource().getPath());
    assertEquals("*", graph.getSource().getGraphname());
    assertEquals("*", graph.getSource().getDefaultFileName());
    graph = graphs.get(1);
    assertEquals("bim/repository/*", graph.getSource().getPath());
    assertEquals("http://www.coinsweb.nl/cbim-2.0.rdf", graph.getSource().getGraphname());
    assertEquals("*", graph.getSource().getDefaultFileName());

    DescribeFactoryImpl.expandGraphConfig(configFile);

    // After expand
    graphs = configFile.getRun().getContainers()[0].getGraphs();
    assertEquals(4, graphs.size());
    graph = graphs.get(0);
    assertEquals(new GraphVarImpl("INSTANCE_UNION_GRAPH"), graph.getSource().getGraph());
    assertEquals("file.2.rdf", graph.getSource().getDefaultFileName());
    graph = graphs.get(1);
    assertEquals(new GraphVarImpl("SCHEMA_UNION_GRAPH"), graph.getSource().getGraph());
    assertEquals("file.3.rdf", graph.getSource().getDefaultFileName());
    graph = graphs.get(2);
    assertEquals("bim/repository/coins2.0.rdf", graph.getSource().getPath());
    assertEquals("http://www.coinsweb.nl/cbim-2.0.rdf", graph.getSource().getGraphname());
    assertEquals("coins2.0.rdf", graph.getSource().getDefaultFileName());
    graph = graphs.get(3);
    assertEquals("bim/testdata_211.ttl", graph.getSource().getPath());
    assertEquals("http://areaal.rws.nl/S.001032/07032018", graph.getSource().getGraphname());
    assertEquals("testdata_211.ttl", graph.getSource().getDefaultFileName());

    inspect(ConfigFactory.toYml(configFile));
  }

  @Test
  public void testVirtual() {
    ConfigFile configFile = ConfigFile.parse(new File(getClass().getResource("virtual.yml").getFile()));
    inspect(ConfigFactory.toYml(configFile));

    List<Graph> graphs;
    Graph graph;

    // Before expand
    graphs = configFile.getRun().getContainers()[0].getGraphs();
    assertEquals(2, graphs.size());
    graph = graphs.get(0);
    assertEquals("nquad.nq", graph.getSource().getPath());
    assertEquals("*", graph.getSource().getGraphname());
    assertEquals("nquad.nq", graph.getSource().getDefaultFileName());
    graph = graphs.get(1);
    assertEquals("empty.rdf", graph.getSource().getPath());
    assertEquals("*", graph.getSource().getGraphname());
    assertEquals("empty.rdf", graph.getSource().getDefaultFileName());

    DescribeFactoryImpl.expandGraphConfig(configFile);

    // After expand
    graphs = configFile.getRun().getContainers()[0].getGraphs();
    assertEquals(3, graphs.size());
    graph = graphs.get(0);
    assertEquals("nquad.nq", graph.getSource().getPath());
    assertEquals("http://example.org/graphs/superman", graph.getSource().getGraphname());
    assertEquals("nquad.nq", graph.getSource().getDefaultFileName());
    graph = graphs.get(1);
    assertEquals("empty.rdf", graph.getSource().getPath());
    assertEquals("http://www.coinsweb.nl/empty.rdf", graph.getSource().getGraphname());
    assertEquals("empty.rdf", graph.getSource().getDefaultFileName());
    graph = graphs.get(2);
    assertEquals("nquad.nq", graph.getSource().getPath());
    assertEquals("http://example.org/graphs/spiderman", graph.getSource().getGraphname());
    assertEquals("nquad.nq", graph.getSource().getDefaultFileName());

    inspect(ConfigFactory.toYml(configFile));
  }

  private void inspect(String message) {
    if(visuallyInspect) {
      System.out.println(message);
    }
  }
}
