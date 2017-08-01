package com.sysunite.coinsweb.graphset;

import com.sysunite.coinsweb.connector.Connector;
import com.sysunite.coinsweb.filemanager.ContainerFile;
import com.sysunite.coinsweb.filemanager.ContainerFileImpl;
import com.sysunite.coinsweb.filemanager.DescribeFactoryImpl;
import com.sysunite.coinsweb.parser.config.factory.FileFactory;
import com.sysunite.coinsweb.parser.config.pojo.*;
import com.sysunite.coinsweb.rdfutil.Utils;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.util.*;

import static com.sysunite.coinsweb.connector.Rdf4jConnector.asResource;
import static java.util.Collections.sort;

/**
 * @author bastbijl, Sysunite 2017
 */
public class ContainerGraphSetFactory {

  private static final Logger log = LoggerFactory.getLogger(ContainerGraphSetFactory.class);

  public static ContainerGraphSet lazyLoad(ContainerFile container, Container containerConfig, Connector connector) {
    Environment environment = containerConfig.getParent().getEnvironment();

    if("none".equals(environment.getStore().getType())) {
      return new ContainerGraphSetImpl(containerConfig);
    }

    log.info("Construct and lazy load graphSet");
    ContainerGraphSet graphSet = new ContainerGraphSetImpl(containerConfig, connector);
    graphSet.setConfigFile(containerConfig.getParent());
    graphSet.lazyLoad(container);

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
  public static ComposePlan load(Connector connector, ContainerFile container) {

    Container containerConfig = ((ContainerFileImpl)container).getConfig();
    List<Graph> originalGraphs = containerConfig.getGraphs();

    // Load all files (φ - graphs)
    ArrayList<Graph> loadList = loadList(originalGraphs, container);
    for (Graph graph : loadList) {
      if(Source.ONLINE.equals(graph.getSource().getType()) ||
      Source.CONTAINER.equals(graph.getSource().getType()) ||
      Source.FILE.equals(graph.getSource().getType())) {
        executeLoad(graph.getSource(), connector, container);
      }
    }


    // Update import statements
    for(Graph graph : loadList) {
      String resource = graph.getSource().getGraphname();
      String sourceId = graph.getSource().getId();
      if(sourceId == null) {
        throw new RuntimeException("raar");
      }
      if(resource != null && sourceId != null) {
        String replace = mapPhiContext(sourceId);
        for(Graph graph2 : loadList) {
          String context = mapPhiContext(graph2.getSource().getId());
          log.info("Replace resource " + resource + " with " + replace + " in " +context);
          connector.replaceResource(context, resource, replace);
        }
      }
    }





    Map<String, Set<String>> hashMap = connector.listPhiSourceIdsPerHash();
    Map<String, Set<String>> sigmaGraphs = connector.listSigmaGraphs();

    // Create all composition graphs (σ - graphs)
    ComposePlan composePlan = composePhiList(originalGraphs, ((ContainerFileImpl) container).getConfig().getVariablesContextMap(), hashMap, sigmaGraphs);
    composePlan = composeSigmaList(composePlan, originalGraphs, ((ContainerFileImpl) container).getConfig().getVariablesContextMap(), hashMap, sigmaGraphs);
    executeCompose(composePlan, connector,true);

    return composePlan;
  }

  public static void executeCompose(ComposePlan composePlan, Connector connector, boolean allowCopy) {
    log.info(composePlan.toString());
    for (ComposePlan.Move move : composePlan.get()) {
      log.info("execute compose");
      if(allowCopy && move.action == ComposePlan.Action.COPY) {
        connector.sparqlCopy(move.from.toString(), move.to.toString());
      } else {
//      if(move.action == ComposePlan.Action.ADD) {
        connector.sparqlCopy(move.from.toString(), move.to.toString());
      }
    }
    for(Mapping mapping : composePlan.getVarMap()) {
      if(mapping.getInclusionSet() != null || !mapping.getInclusionSet().isEmpty()) {
        connector.storeSigmaGraphExists(mapping.getGraphname(), mapping.getInclusionSet());
      }
    }
  }

  private static void executeLoad(Source source, Connector connector, ContainerFile container) {

    String fileName;
    String filePath = source.getPath();
    if(filePath == null) {
      fileName = source.getUri();
    } else {
      fileName = new File(source.getPath()).getName();
    }

    String context = mapPhiContext(source);
    ArrayList<String> contexts = new ArrayList<>();
    contexts.add(context);

    log.info("Upload rdf-file to connector: " + filePath);
    DigestInputStream inputStream = FileFactory.toInputStream(source, container);
    connector.uploadFile(inputStream, fileName, source.getGraphname(), contexts);
    String hash = FileFactory.getFileHash(inputStream);
    source.setHash(hash);

    log.info("Uploaded, the file has this hash: " + hash);

    connector.storePhiGraphExists(source, context, fileName, hash);
  }

  public static String mapPhiContext(Source source) {
    String sourceId = source.getId();
    if(sourceId == null) {
      sourceId = RandomStringUtils.random(8, true, true);
      source.setId(sourceId);
    }
    return mapPhiContext(sourceId);
  }
  public static String mapPhiContext(String sourceId) {
    return "http://validator/uploadedFile#" + sourceId;
  }
  public static String mapSigmaContext(String confContext) {
    String rand = RandomStringUtils.random(8, true, true);
    return confContext + "-" + rand;
  }

  public static ArrayList<Graph> loadList(List<Graph> originalGraphs, ContainerFile container) {

    Graph allContentFile = null;
    Graph allLibraryFile = null;

    // Explicit graphs
    ArrayList<String> explicitGraphs = new ArrayList();
    for(Graph graph : originalGraphs) {

      // Only consider these now
      if(!Source.ONLINE.equals(graph.getSource().getType()) &&
         !Source.CONTAINER.equals(graph.getSource().getType()) &&
         !Source.FILE.equals(graph.getSource().getType())) {
        continue;
      }

      if(!graph.getSource().anyGraph()) {
        String graphName = graph.getSource().getGraphname();
        if(Utils.containsNamespace(graphName, explicitGraphs)) {
          throw new RuntimeException("The namespace "+graphName+ " is being mentioned more than once, this is not allowed");
        }
        log.info("Reserve this namespace to load from explicitly mentioned source: "+graphName);
        explicitGraphs.add(graphName);
      }

      if(Source.CONTAINER.equals(graph.getSource().getType())) {

        // Keep track of fallback graph definitions
        if (graph.getSource().anyContentFile()) {
          if (allContentFile != null) {
            throw new RuntimeException("Only one graph with content file asterisk allowed");
          }
          allContentFile = graph;
        }
        if (graph.getSource().anyLibraryFile()) {
          if (allLibraryFile != null) {
            throw new RuntimeException("Only one graph with library file asterisk allowed");
          }
          allLibraryFile = graph;
        }
      }
    }

    // Implicit graphs
    ArrayList<Graph> loadList = new ArrayList();
    ArrayList<String> implicitGraphs = new ArrayList();

    if(allContentFile != null) {
      for(Graph graph : DescribeFactoryImpl.contentGraphsInContainer(container, allContentFile.getAs())) {
        String graphName = graph.getSource().getGraphname();
        log.info("Found graph in content file: "+graphName);
        if(!Utils.containsNamespace(graphName, explicitGraphs)) {
          log.info("Will load content file from wildcard definition");
          if(Utils.containsNamespace(graphName, implicitGraphs)) {
            throw new RuntimeException("Collision in implicit graphs names, this one can be found in more than one source: "+graphName);
          }
          implicitGraphs.add(graphName);
          loadList.add(graph);
        }
      }
    }

    if(allLibraryFile != null) {
      for(Graph graph : DescribeFactoryImpl.libraryGraphsInContainer(container, allLibraryFile.getAs())) {
        String graphName = graph.getSource().getGraphname();
        log.info("Found graph in library file: "+graphName);
        if(!Utils.containsNamespace(graphName, explicitGraphs)) {
          log.info("Will load library file from wildcard definition");
          if(Utils.containsNamespace(graphName, implicitGraphs)) {
            throw new RuntimeException("Collision in implicit graphs names, this one can be found in more than one source: "+graphName);
          }
          implicitGraphs.add(graphName);
          loadList.add(graph);
        }
      }
    }

    // If a graph points to a file or link online instead of a file in a container
    for(Graph originalGraph : originalGraphs) {
      if(originalGraph.getSource().anyGraph() &&
      (Source.FILE.equals(originalGraph.getSource().getType()) || Source.ONLINE.equals(originalGraph.getSource().getType()))) {

        File file = FileFactory.toFile(originalGraph.getSource().asLocator());
        try {
          for (String graphName : DescribeFactoryImpl.namespacesForFile(file)) {
            log.info("Found graph in file/online: "+graphName);
            if (!Utils.containsNamespace(graphName, explicitGraphs)) {
              log.info("Will load graph from file because of wildcard graph definition");
              if (Utils.containsNamespace(graphName, implicitGraphs)) {
                throw new RuntimeException("Collision in implicit graphs names, this one can be found in more than one source: " + graphName);
              }
              implicitGraphs.add(graphName);

              Graph graph = originalGraph.clone();
              graph.getSource().setGraphname(graphName);
              loadList.add(graph);
            }
          }
        } catch (FileNotFoundException e) {
          throw new RuntimeException(e);
        }
      }
    }

    // Now load the explicit graphs
    for(Graph graph : originalGraphs) {

      // Only consider these now
      if(!Source.ONLINE.equals(graph.getSource().getType()) &&
         !Source.CONTAINER.equals(graph.getSource().getType()) &&
         !Source.FILE.equals(graph.getSource().getType())) {
        loadList.add(graph);
      } else {

        if (!graph.getSource().anyGraph()) {

          // Check if the file in the container is available
          if (Source.CONTAINER.equals(graph.getSource().getType())) {
            try {
              container.getFile(Paths.get(graph.getSource().getPath()));
            } catch (RuntimeException e) {
              throw e;
            }
          }

          log.info("Will load explicitly defined file for context: " + graph.getSource().getGraphname());
          loadList.add(graph);
        }
      }
    }
    return loadList;
  }

  private static Map<String, String> reverseSigmaGraphsMap(Map<String, Set<String>> sigmaGraphs) {
    Map<String, String> map = new HashMap<>();
    for(String sigmaGraph : sigmaGraphs.keySet()) {
      List<String> list = new ArrayList<>();
      list.addAll(sigmaGraphs.get(sigmaGraph));
      sort(list);
      map.put("".join("-", list), sigmaGraph);
    }
    return map;
  }

  /**
   * An ordered list of tuples which context to copy to which context
   */
  public static ComposePlan composePhiList(List<Graph> originalGraphs, HashMap<GraphVar, String> confVarMap, Map<String, Set<String>> hashMap, Map<String, Set<String>> sigmaGraphs) {

    // Implicit graphs
    ComposePlan composePlan = new ComposePlan();

    if (originalGraphs == null || originalGraphs.isEmpty()) {
      return composePlan;
    }

    List<Mapping> varMap = new ArrayList<>();
    Map<String, String> reversedSigmaMap = reverseSigmaGraphsMap(sigmaGraphs);

    // Collect available φ graphVars and σ graphVars
    Map<GraphVarImpl, String> mappedGraphs = new HashMap<>();
    for (Graph graph : originalGraphs) {

      // Graphs that can be filled directly from a file graph
      if (!Source.STORE.equals(graph.getSource().getType())) {

        // Decide which phiGraph to use (the new or a previously uploaded)
        String from = null;
        String to = null;
        String hash = graph.getSource().getHash();
        Set<String> options = hashMap.get(hash);
        if(options.isEmpty()) {
          throw new RuntimeException("No sigma Graph found with this hash");
        }
        Iterator<String> optionIterator = options.iterator();
        while(optionIterator.hasNext()) {
          String candidate = mapPhiContext(optionIterator.next());
          if(reversedSigmaMap.containsKey(candidate)) {
            from = candidate;
            to = reversedSigmaMap.get(from);
            break;
          }
        }

        // Never was some phiGraph with this hash mapped to any sigmaGraph
        if(from == null) {
          from = mapPhiContext(graph.getSource());

          for (GraphVarImpl as : graph.getAs()) {

            to = mapSigmaContext(confVarMap.get(as));
            if (!mappedGraphs.keySet().contains(as)) {
              composePlan.add(ComposePlan.Action.COPY, asResource(from), asResource(to));
              mappedGraphs.put(as, to);
            } else {
              composePlan.add(ComposePlan.Action.ADD, asResource(from), asResource(to));
            }
          }
        }
        Set<String> fromSet = new HashSet<>();
        fromSet.add(from);
        for (GraphVarImpl as : graph.getAs()) {
          varMap.add(new Mapping(as, to, graph.getSource().getDefaultFileName(), fromSet));
        }
      }
    }

    composePlan.setVarMap(varMap);
    return composePlan;
  }

  public static ComposePlan composeSigmaList(ComposePlan composePlan, List<Graph> originalGraphs, HashMap<GraphVar, String> confVarMap, Map<String, Set<String>> hashMap, Map<String, Set<String>> sigmaGraphs) {

    // Graphs that will have to be composed of other σ graphs
    List<Graph> todoGraphs = new ArrayList();
    for (Graph graph : originalGraphs) {
      if (Source.STORE.equals(graph.getSource().getType())) {
        todoGraphs.add(graph);
      }
    }

    List<Mapping> varMap = composePlan.getVarMap();
    Map<String, String> reversedSigmaMap = reverseSigmaGraphsMap(sigmaGraphs);
    Map<GraphVarImpl, String> mappedGraphs = new HashMap<>();
    for(Mapping mapping : composePlan.getVarMap()) {
      mappedGraphs.put(mapping.getVariable(), mapping.getGraphname());
    }
    while(!todoGraphs.isEmpty()) {

      for(Graph graph : todoGraphs) {
        if(mappedGraphs.keySet().containsAll(graph.getSource().getGraphs())) {

          // Build the fingerprint
          List<String> fromList = new ArrayList<>();
          for (GraphVarImpl graphVar : graph.getSource().getGraphs()) {
            String from = mappedGraphs.get(graphVar);
            fromList.add(from);
          }
          sort(fromList);

          String fingerPrint = "".join("-", fromList);

          Set<String> fromSet = new HashSet<>();
          fromSet.addAll(fromList);

          // If it already exists
          if(reversedSigmaMap.keySet().contains(fingerPrint)) {

            for (GraphVarImpl as : graph.getAs()) {
              String to = reversedSigmaMap.get(fingerPrint);
              varMap.add(new Mapping(as, to, null, fromSet));
            }

          // If decided to not reuse something
          } else {

            for (GraphVarImpl graphVar : graph.getSource().getGraphs()) {

              String from = mappedGraphs.get(graphVar);


              for (GraphVarImpl as : graph.getAs()) {

                String to = mapSigmaContext(confVarMap.get(as));
                if (!mappedGraphs.keySet().contains(as)) {
                  composePlan.add(ComposePlan.Action.COPY, asResource(from), asResource(to));
                  mappedGraphs.put(as, to);


                  varMap.add(new Mapping(as, to, null, fromSet));

                } else {
                  composePlan.add(ComposePlan.Action.ADD, asResource(from), asResource(to));
                }

              }

            }
          }

          todoGraphs.remove(graph);
          break;
        }
      }

    }

    return composePlan;
  }
}
