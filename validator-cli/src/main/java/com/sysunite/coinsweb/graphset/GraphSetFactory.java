package com.sysunite.coinsweb.graphset;

import com.sysunite.coinsweb.connector.Connector;
import com.sysunite.coinsweb.connector.ConnectorFactory;
import com.sysunite.coinsweb.connector.ConnectorFactoryImpl;
import com.sysunite.coinsweb.filemanager.ContainerFile;
import com.sysunite.coinsweb.parser.config.factory.FileFactory;
import com.sysunite.coinsweb.parser.config.factory.ConfigFactory;
import com.sysunite.coinsweb.parser.config.pojo.ConfigFile;
import com.sysunite.coinsweb.parser.config.pojo.Container;
import com.sysunite.coinsweb.parser.config.pojo.Environment;
import com.sysunite.coinsweb.parser.config.pojo.Graph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;

import static java.util.Collections.sort;

/**
 * @author bastbijl, Sysunite 2017
 */
public class GraphSetFactory {

  private static final Logger log = LoggerFactory.getLogger(GraphSetFactory.class);

  public static ContainerGraphSet lazyLoad(ContainerFile container, Container containerConfig) {
    Environment environment = containerConfig.getParent().getEnvironment();
    if("none".equals(environment.getStore().getType())) {
      return new ContainerGraphSetImpl();
    }


    HashMap<String, String> graphs = containerConfig.getParent().getEnvironment().getMapping();

    log.info("Construct graphset and lazy load connector");
    ConnectorFactory factory = new ConnectorFactoryImpl();
    Connector connector = factory.build(environment);
    ContainerGraphSet graphSet = new ContainerGraphSetImpl(connector, graphs);
    graphSet.setContainerFile(container);
    graphSet.setContainerConfig(containerConfig);
    graphSet.setConfigFile(containerConfig.getParent());
    return graphSet;
  }

  /**
   * Build load strategy and execute the loading
   *
   * Returns a map that maps
   *
   * @param originalGraphs
   * @param connector
   */
  public static HashMap<String, String> load(Graph[] originalGraphs, Connector connector, ContainerFile container, ConfigFile configFile) {

    ArrayList<Graph> loadList = ConfigFactory.loadList(originalGraphs, container);

    // Keep a whitelist of keys that should not be loaded
    ArrayList<String> whitelist = new ArrayList();

    // Map source graphname to target graphname
    HashMap<String, String> mapping = configFile.getEnvironment().getMapping();
    if(Environment.HASH_IN_GRAPHNAME.equals(configFile.getEnvironment().getLoadingStrategy())) {

      HashMap<String, ArrayList<String>> keyToHashArray = new HashMap();
      for (Graph graph : loadList) {
        for(String key : graph.getAs()) {
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
      HashMap<String, String> sortedHashMapping = new HashMap();
      for(String key : keyToHashArray.keySet()) {
        sort(keyToHashArray.get(key));
        String fullNamespace = mapping.get(key)+"-"+String.join("-", keyToHashArray.get(key));
        log.info("Use for "+ key + " graphname "+ fullNamespace);
        sortedHashMapping.put(key, fullNamespace);

        // Fill the blacklist
        if(connector.containsContext(fullNamespace)) {
          log.info("Adding key "+ key+" to the whitelist, this graph is already available: "+ fullNamespace);
          whitelist.add(key);
        }
      }




      for (Graph graph : loadList) {
        load(graph, connector, container, sortedHashMapping, whitelist);
      }
      return sortedHashMapping;

    } else {


      for (Graph graph : loadList) {
        load(graph, connector, container, mapping, whitelist);
      }
      return mapping;
    }
  }

  public static void load(Graph graph, Connector connector, ContainerFile container, HashMap<String, String> mapping, ArrayList<String> blacklist) {

    ArrayList<String> graphNames = new ArrayList();
    for(int i = 0; i < graph.getAs().size(); i++) {
      String key = graph.getAs().get(i);
      if(!blacklist.contains(key)) {
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
