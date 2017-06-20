package com.sysunite.coinsweb.graphset;

import com.sysunite.coinsweb.connector.Connector;
import com.sysunite.coinsweb.connector.ConnectorFactory;
import com.sysunite.coinsweb.connector.ConnectorFactoryImpl;
import com.sysunite.coinsweb.filemanager.ConfigGenerator;
import com.sysunite.coinsweb.filemanager.ContainerFile;
import com.sysunite.coinsweb.filemanager.ContainerFileImpl;
import com.sysunite.coinsweb.filemanager.FileFactory;
import com.sysunite.coinsweb.parser.config.ConfigFile;
import com.sysunite.coinsweb.parser.config.Container;
import com.sysunite.coinsweb.parser.config.Graph;
import com.sysunite.coinsweb.parser.config.Store;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author bastbijl, Sysunite 2017
 */
public class GraphSetFactory {

  private static final Logger log = LoggerFactory.getLogger(GraphSetFactory.class);

  public static ContainerGraphSet lazyLoad(ContainerFileImpl container, Container containerConfig, ConfigFile configFile) {
    Store storeConfig = configFile.getEnvironment().getStore();
    if("none".equals(storeConfig.getType())) {
      return new ContainerGraphSetImpl();
    }


    HashMap<String, String> graphs = configFile.getEnvironment().getMapping();

    log.info("Construct graphset and lazy load connector");
    ConnectorFactory factory = new ConnectorFactoryImpl();
    Connector connector = factory.build(storeConfig);
    ContainerGraphSet graphSet = new ContainerGraphSetImpl(connector, graphs);
    graphSet.setContainerFile(container);
    graphSet.setContainerConfig(containerConfig);
    graphSet.setConfigFile(configFile);
    return graphSet;
  }

  /**
   * Build load strategy and execute the loading
   *
   * @param selectedGraphs
   * @param connector
   */
  public static void load(Graph[] selectedGraphs, Connector connector, ContainerFile container, ConfigFile configFile) {

    Graph allContentFile = null;
    Graph allLibraryFile = null;

    // Explicit graphs
    ArrayList<String> explicitGraphs = new ArrayList();
    for(Graph graph : selectedGraphs) {
      if(!graph.anyGraph()) {
        explicitGraphs.add(graph.getGraphname());
      }

      // Keep track of fallback graph definitions
      if(graph.anyContentFile()) {
        if(allContentFile != null) {
          throw new RuntimeException("Only one graph with content file asterisk allowed");
        }
        allContentFile = graph;
      }
      if(graph.anyLibraryFile()) {
        if(allLibraryFile != null) {
          throw new RuntimeException("Only one graph with content file asterisk allowed");
        }
        allLibraryFile = graph;
      }
    }

    if(allContentFile != null) {
      for(Graph graph : ConfigGenerator.contentGraphsInContainer(container, allContentFile.getContent())) {
        if(!explicitGraphs.contains(graph.getGraphname())) {
          log.info("Will load content file from wildcard definition");
          load(graph, connector, container, configFile);
        }
      }
    }

    if(allLibraryFile != null) {
      for(Graph graph : ConfigGenerator.libraryGraphsInContainer(container, allLibraryFile.getContent())) {
        if(!explicitGraphs.contains(graph.getGraphname())) {
          log.info("Will load library file from wildcard definition");
          load(graph, connector, container, configFile);
        }
      }
    }

    for(Graph graph : selectedGraphs) {
      if(!graph.anyGraph()) {

        // Check if the file in the container is available
        if(graph.CONTAINER.equals(graph.getType())) {
          try {
            container.getFile(Paths.get(graph.getPath()));
          } catch(RuntimeException e) {
            throw e;
          }
        }

        log.info("Will load explicitly defined file");
        load(graph, connector, container, configFile);
      }
    }
  }

  public static void load(Graph graph, Connector connector, ContainerFile container, ConfigFile configFile) {

    String[] graphNames = new String[graph.getContent().size()];
    for(int i = 0; i < graph.getContent().size(); i++) {
      graphNames[i] = configFile.getEnvironment().getMapping().get(graph.getContent().get(i));
    }
    String fileName = graph.getPath();
    if(fileName == null) {
      fileName = graph.getUri();
    }

    log.info("Upload rdf file to connector: "+fileName);
    connector.uploadFile(FileFactory.toInputStream(graph, container, configFile), fileName, graph.getGraphname(), graphNames);
  }

}
