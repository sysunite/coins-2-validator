package com.sysunite.coinsweb.graphset;

import com.sysunite.coinsweb.connector.Connector;
import com.sysunite.coinsweb.filemanager.ContainerFile;
import com.sysunite.coinsweb.filemanager.DescribeFactoryImpl;
import com.sysunite.coinsweb.parser.config.factory.FileFactory;
import com.sysunite.coinsweb.parser.config.pojo.*;
import com.sysunite.coinsweb.rdfutil.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static java.util.Collections.sort;

/**
 * @author bastbijl, Sysunite 2017
 */
public class GraphSetFactory {

  private static final Logger log = LoggerFactory.getLogger(GraphSetFactory.class);

  public static ContainerGraphSet lazyLoad(ContainerFile container, Container containerConfig, Connector connector) {
    Environment environment = containerConfig.getParent().getEnvironment();

    if("none".equals(environment.getStore().getType())) {
      return new ContainerGraphSetImpl();
    }

    log.info("Construct and lazy load graphSet");
    ContainerGraphSet graphSet = new ContainerGraphSetImpl(connector);
    graphSet.setContainerFile(container);
    graphSet.setContainerConfig(containerConfig);
    graphSet.setConfigFile(containerConfig.getParent());

    Graph main = null;
    for(Graph graph : containerConfig.getGraphs()) {
      if(graph.getMain() != null && graph.getMain()) {
        if(main == null) {
          main = graph;
        } else {
          throw new RuntimeException("No two graphs can be flagged as main");
        }
      }
    }
    graphSet.setMain(main);

    return graphSet;
  }

  /**
   * Build load strategy and execute the loading
   *
   * Returns a map that maps
   *
   */
  public static HashMap<GraphVar, String> load(Container containerConfig, Connector connector, ContainerFile container, ConfigFile configFile) {

    Graph[] originalGraphs = containerConfig.getGraphs();

    connector.init();
    List<String> availableContexts = connector.getContexts();

    ArrayList<Graph> loadList = DescribeFactoryImpl.loadList(originalGraphs, container);

    // Keep a whiteList of keys that should not be loaded
    ArrayList<GraphVar> whiteList = new ArrayList();

    // Map source graphname to target graphname
    HashMap<GraphVar, String> mapping = containerConfig.getVariablesMap();
    if(Environment.HASH_IN_GRAPHNAME.equals(configFile.getEnvironment().getLoadingStrategy())) {

      HashMap<GraphVar, ArrayList<String>> keyToHashArray = new HashMap();
      for (Graph graph : loadList) {
        for(GraphVar key : graph.getAs()) {
          if(!keyToHashArray.containsKey(key)) {
            keyToHashArray.put(key, new ArrayList());
          }
          ArrayList<String> hashList = keyToHashArray.get(key);
          String hash = FileFactory.getFileHash(graph.getSource(), container);
          if(!hashList.contains(hash)) {
            hashList.add(hash);
          }
        }
      }
      HashMap<GraphVar, String> sortedHashMapping = new HashMap();
      for(GraphVar key : keyToHashArray.keySet()) {
        sort(keyToHashArray.get(key));
        String fullNamespace = mapping.get(key)+"-"+String.join("-", keyToHashArray.get(key));
        log.info("Use for "+ key + " graphname "+ fullNamespace);
        sortedHashMapping.put(key, fullNamespace);

        // Fill the whiteList
        if(Utils.containsNamespace(fullNamespace, availableContexts)) {
          log.info("Adding key "+ key+" to the whiteList, this graph is already available: "+ fullNamespace);
          whiteList.add(key);
        }
      }


      for (Graph graph : loadList) {
        executeLoad(graph, connector, container, sortedHashMapping, whiteList);
      }
      return sortedHashMapping;

    } else {

      for (Graph graph : loadList) {
        executeLoad(graph, connector, container, mapping, whiteList);
      }
      return mapping;
    }
  }

  private static void executeLoad(Graph graph, Connector connector, ContainerFile container, HashMap<GraphVar, String> mapping, ArrayList<GraphVar> whiteList) {

    ArrayList<String> graphNames = new ArrayList();
    for(int i = 0; i < graph.getAs().size(); i++) {
      GraphVar key = graph.getAs().get(i);
      if(!whiteList.contains(key)) {
        graphNames.add(mapping.get(key));
      }
    }
    String fileName = graph.getSource().getPath();
    if(fileName == null) {
      fileName = graph.getSource().getUri();
    }

    if(!graphNames.isEmpty()) {
      log.info("Upload rdf file to connector: " + fileName);
      connector.uploadFile(FileFactory.toInputStream(graph.getSource(), container), fileName, graph.getSource().getGraphname(), graphNames);
    }

  }

}
