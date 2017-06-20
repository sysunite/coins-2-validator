package com.sysunite.coinsweb.graphset;

import com.sysunite.coinsweb.connector.Connector;
import com.sysunite.coinsweb.connector.ConnectorFactory;
import com.sysunite.coinsweb.connector.ConnectorFactoryImpl;
import com.sysunite.coinsweb.filemanager.ConfigGenerator;
import com.sysunite.coinsweb.filemanager.ContainerFile;
import com.sysunite.coinsweb.filemanager.ContainerFileImpl;
import com.sysunite.coinsweb.filemanager.FileFactory;
import com.sysunite.coinsweb.parser.config.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

import static java.util.Collections.sort;

/**
 * @author bastbijl, Sysunite 2017
 */
public class GraphSetFactory {

  private static final Logger log = LoggerFactory.getLogger(GraphSetFactory.class);

  public static ContainerGraphSet lazyLoad(ContainerFileImpl container, Container containerConfig, ConfigFile configFile) {
    Environment environment = configFile.getEnvironment();
    if("none".equals(environment.getStore().getType())) {
      return new ContainerGraphSetImpl();
    }


    HashMap<String, String> graphs = configFile.getEnvironment().getMapping();

    log.info("Construct graphset and lazy load connector");
    ConnectorFactory factory = new ConnectorFactoryImpl();
    Connector connector = factory.build(environment);
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

    ArrayList<Graph> loadList = new ArrayList();


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
          loadList.add(graph);
        }
      }
    }

    if(allLibraryFile != null) {
      for(Graph graph : ConfigGenerator.libraryGraphsInContainer(container, allLibraryFile.getContent())) {
        if(!explicitGraphs.contains(graph.getGraphname())) {
          log.info("Will load library file from wildcard definition");
          loadList.add(graph);
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
        loadList.add(graph);
      }
    }

    // Keep a blacklist of keys that should not be loaded
    ArrayList<String> blacklist = new ArrayList();

    // Map source graphname to target graphname
    HashMap<String, String> mapping = configFile.getEnvironment().getMapping();
    if(Environment.HASH_IN_GRAPHNAME.equals(configFile.getEnvironment().getLoadingStrategy())) {

      HashMap<String, ArrayList<String>> keyToHashArray = new HashMap();
      for (Graph graph : loadList) {
        for(String key : graph.getContent()) {
          if(!keyToHashArray.containsKey(key)) {
            keyToHashArray.put(key, new ArrayList());
          }
          ArrayList<String> hashList = keyToHashArray.get(key);
          String hash = FileFactory.getFileHash(graph, container);
          if(!hashList.contains(hash)) {
            hashList.add(hash);
          }
        }
      }
      HashMap<String, String> sortedHashMapping = new HashMap();
      for(String key : keyToHashArray.keySet()) {
        sort(keyToHashArray.get(key));
        String fullNamespace = mapping.get(key)+"-"+String.join("-", keyToHashArray.get(key));
        log.info("Use for "+ key + " graphname "+ fullNamespace);
        sortedHashMapping.put(key, fullNamespace);

        // Fill the blacklist
        if(connector.containsContext(fullNamespace)) {
          log.info("Adding key "+ key+" to the blacklist, this graph is already available: "+ fullNamespace);
          blacklist.add(key);
        }
      }




      for (Graph graph : loadList) {
        load(graph, connector, container, configFile, sortedHashMapping, blacklist);
      }

    } else {


      for (Graph graph : loadList) {
        load(graph, connector, container, configFile, mapping, blacklist);
      }
    }
  }

  public static void load(Graph graph, Connector connector, ContainerFile container, ConfigFile configFile, HashMap<String, String> mapping, ArrayList<String> blacklist) {

    ArrayList<String> graphNames = new ArrayList();
    for(int i = 0; i < graph.getContent().size(); i++) {
      String key = graph.getContent().get(i);
      if(!blacklist.contains(key)) {
        graphNames.add(mapping.get(key));
      }
    }
    String fileName = graph.getPath();
    if(fileName == null) {
      fileName = graph.getUri();
    }

    if(!graphNames.isEmpty()) {
      log.info("Upload rdf file to connector: " + fileName);
      connector.uploadFile(FileFactory.toInputStream(graph, container, configFile), fileName, graph.getGraphname(), graphNames);
    }

  }

}
