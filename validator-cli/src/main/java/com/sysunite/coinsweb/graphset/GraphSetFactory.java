package com.sysunite.coinsweb.graphset;

import com.sysunite.coinsweb.connector.Connector;
import com.sysunite.coinsweb.filemanager.ContainerFile;
import com.sysunite.coinsweb.filemanager.DescribeFactoryImpl;
import com.sysunite.coinsweb.parser.config.factory.FileFactory;
import com.sysunite.coinsweb.parser.config.pojo.*;
import com.sysunite.coinsweb.rdfutil.Utils;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

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

        // Only consider these now
        if(!Source.ONLINE.equals(graph.getSource().getType()) &&
           !Source.CONTAINER.equals(graph.getSource().getType()) &&
           !Source.FILE.equals(graph.getSource().getType())) {
          continue;
        }

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
      for(GraphVar key : mapping.keySet()) {
        if(keyToHashArray.containsKey(key)) {
          sort(keyToHashArray.get(key));
          String fullNamespace = mapping.get(key) + "-" + String.join("-", keyToHashArray.get(key));
          log.info("Use sorted hash url for " + key + " graphname: " + fullNamespace);
          sortedHashMapping.put(key, fullNamespace);

          // Fill the whiteList
          if (Utils.containsNamespace(fullNamespace, availableContexts)) {
            whiteList.add(key);
          }
        } else {
          sortedHashMapping.put(key, mapping.get(key) + "-" + RandomStringUtils.random(8, true, true));
        }
      }

      containerConfig.updateVariables(sortedHashMapping);
      mapping = sortedHashMapping;
    }

    for (Graph graph : loadList) {
      if(Source.ONLINE.equals(graph.getSource().getType()) ||
         Source.CONTAINER.equals(graph.getSource().getType()) ||
         Source.FILE.equals(graph.getSource().getType())) {
        executeLoad(graph, connector, container, mapping, whiteList);
      }
    }
    executeCompose(originalGraphs, connector, mapping);
    return mapping;
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
      log.info("Upload rdf-file to connector: " + fileName);
      connector.uploadFile(FileFactory.toInputStream(graph.getSource(), container), fileName, graph.getSource().getGraphname(), graphNames);
      for(String context : graphNames) {
        connector.storeGraphExists(context, graph.getSource().getGraphname());
      }
    } else {
      log.info("\u2728 Not uploading file because it is already uploaded: "+fileName);
    }
  }

  public static boolean testCompose(Graph[] graphs, HashMap<GraphVar, String> mapping) {
    log.info("Test compose: ");
    for(Graph graph : graphs) {
      if(Source.FILE.equals(graph.getSource().getType())) {
        log.info("(" + graph.getSource().getPath() + ") -> (" + String.join(", ", graph.getAs()) + ")");
      }
      if(Source.CONTAINER.equals(graph.getSource().getType())) {
        log.info("(" + graph.getSource().getPath() + ") -> (" + String.join(", ", graph.getAs()) + ")");
      }
      if(Source.ONLINE.equals(graph.getSource().getType())) {
        log.info("(" + graph.getSource().getUri() + ") -> (" + String.join(", ", graph.getAs()) + ")");
      }
      if(Source.STORE.equals(graph.getSource().getType())) {
        log.info("(" + String.join(", ", graph.getSource().getGraphs()) + ") -> (" + String.join(", ", graph.getAs()) + ")");
      }
    }

    try {
      executeCompose(graphs, null, mapping);
    } catch(RuntimeException e) {
      return false;
    }
    return true;
  }

  private static void executeCompose(Graph[] graphs, Connector connector, HashMap<GraphVar, String> mapping) {

    if(graphs == null || graphs.length < 1) {
      return;
    }

    // Collect available graphVars for copying
    List<GraphVarImpl> resolvedGraphs = new ArrayList();
    for (Graph graph : graphs) {
      if(!Source.STORE.equals(graph.getSource().getType())) {
        resolvedGraphs.addAll(graph.getAs());
      }
    }


    int benchmark = graphs.length;
    int toDo = 0;

    Set<Graph> finished = new HashSet();

    do {

      log.info("Go trough graphs to copy/add context to context");

      // If previous run had the same amount as the benchmark some copy actions are not possible to execute;
      if(toDo == benchmark) {
        String graphList = "";
        for(Graph graph : graphs) {
          if (Source.STORE.equals(graph.getSource().getType())) {
            graphList += "\n(" + String.join(", ", graph.getSource().getGraphs()) + ") -> (" + String.join(", ", graph.getAs()) + ")";
          }
        }
        throw new RuntimeException("Some "+toDo+" Sources of type 'store' can not be mapped: "+graphList);
      }
      benchmark = toDo;
      toDo = 0;

      for (Graph graph : graphs) {
        if (Source.STORE.equals(graph.getSource().getType())) {

          log.info("Check (" + String.join(", ", graph.getSource().getGraphs()) + ") -> (" + String.join(", ", graph.getAs()) + ")");

          if(finished.contains(graph)) {
            continue;
          }

          // It is not allowed to copy to an existing graph
          for (GraphVarImpl to : graph.getAs()) {
            if (resolvedGraphs.contains(to)) {
              throw new RuntimeException("Some Source of type 'store' wants to map to an already existing graphVar: " + to.toString());
            }
          }

          boolean resolvable = true;
          for (GraphVarImpl from : graph.getSource().getGraphs()) {
            resolvable &= resolvedGraphs.contains(from);
          }

          // Execute it
          if(resolvable) {

            // First use COPY and for all the others ADD
            for (GraphVarImpl to : graph.getAs()) {
              boolean first = true;
              for (GraphVarImpl from : graph.getSource().getGraphs()) {

                String fromContext = mapping.get(from);
                String toContext = mapping.get(to);

                if(connector != null) {
                  if (first) {
                    log.info("Copy " + fromContext + " to " + toContext);
                    connector.sparqlCopy(fromContext, toContext);
                    first = false;
                  } else {
                    log.info("Add all triples from " + fromContext + " to " + toContext);
                    connector.sparqlAdd(fromContext, toContext);
                  }
                }
              }
              resolvedGraphs.add(to);
            }
            finished.add(graph);


          // Or keep it for next run
          } else {
            toDo++;
          }
        }
      }

    } while(toDo > 0);
  }

}
