package com.sysunite.coinsweb.graphset;

import com.sysunite.coinsweb.connector.Connector;
import com.sysunite.coinsweb.connector.ConnectorException;
import com.sysunite.coinsweb.filemanager.ContainerFile;
import com.sysunite.coinsweb.filemanager.ContainerFileImpl;
import com.sysunite.coinsweb.filemanager.DescribeFactoryImpl;
import com.sysunite.coinsweb.parser.config.factory.FileFactory;
import com.sysunite.coinsweb.parser.config.pojo.*;
import com.sysunite.coinsweb.parser.profile.factory.ProfileFactory;
import com.sysunite.coinsweb.parser.profile.pojo.ProfileFile;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.security.DigestInputStream;
import java.util.*;

import static com.sysunite.coinsweb.connector.Rdf4jConnector.asResource;
import static com.sysunite.coinsweb.rdfutil.Utils.containsNamespace;
import static com.sysunite.coinsweb.rdfutil.Utils.withoutHash;
import static java.util.Collections.sort;

/**
 * @author bastbijl, Sysunite 2017
 */
public class ContainerGraphSetFactory {
  private static final Logger log = LoggerFactory.getLogger(ContainerGraphSetFactory.class);

  public static ContainerGraphSet lazyLoad(ContainerFile container, Container containerConfig, Connector connector, Map<String, Set<GraphVar>> inferencePreference) {

    log.info("Construct and lazy load graphSet");
    ContainerGraphSet graphSet = new ContainerGraphSetImpl(containerConfig.getVariables(), connector);
    graphSet.lazyLoad(container, inferencePreference);

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

  public static Map<String, Set<GraphVar>> inferencePreference(ProfileFile profileFile) {
    HashMap<String, Set<GraphVar>> result = new HashMap<>();
    Map<String, Set<String>> stringMap = ProfileFactory.inferencesOverVars(profileFile);
    for(String inferenceCode : stringMap.keySet()) {
      Set<GraphVar> graphVars = new HashSet<>();
      for(String graphVarString : stringMap.get(inferenceCode)) {
        graphVars.add(new GraphVarImpl(graphVarString));
      }
      result.put(inferenceCode, graphVars);
    }
    return result;
  }

  /**
   * Build load strategy and execute the loading
   *
   * Returns a map that maps
   *
   */
  public static ComposePlan load(ContainerGraphSetImpl graphSet, Connector connector, ContainerFile container, Map<String, Set<GraphVar>> inferencePreference) {

    Container containerConfig = ((ContainerFileImpl)container).getConfig();
    List<Graph> loadList = containerConfig.getGraphs();

    // Load all files (φ - graphs)
    ArrayList<Graph> phiGraphs = new ArrayList<>();
    for (Graph graph : loadList) {
      if(Source.ONLINE.equals(graph.getSource().getType()) ||
      Source.CONTAINER.equals(graph.getSource().getType()) ||
      Source.FILE.equals(graph.getSource().getType())) {
        phiGraphs.add(graph);
      }
    }

    HashMap<String, String> changeMap = new HashMap<>();
    for(Graph phiGraph : phiGraphs) {

      try {
        executeLoad(phiGraph.getSource(), connector, container);
      } catch (ConnectorException e) {
        log.error("Loading this graph to the connector failed: "+phiGraph.getSource().getGraphname(), e);
        graphSet.setFailed();
        return null;
      }
      changeMap.put(withoutHash(phiGraph.getSource().getGraphname()), withoutHash(phiGraph.getSource().getStoreContext()));

      for(String originalContext : changeMap.keySet()) {
        try {
          connector.replaceResource(phiGraph.getSource().getStoreContext(), originalContext, changeMap.get(originalContext));
        } catch (ConnectorException e) {
          log.error("Failed replacing resource", e);
          graphSet.setFailed();
        }
      }
    }

    // Create all composition graphs (σ - graphs)
    Container containerConfig2 = ((ContainerFileImpl) container).getConfig();  // todo check if this is a duplicate
    List<Mapping> variables = containerConfig2.getVariables();
    ComposePlan composePlan = composeSigmaList(graphSet, connector, variables, loadList, inferencePreference);

    executeCompose(graphSet, composePlan, connector, false);

    return composePlan;
  }

  public static void executeCompose(ContainerGraphSetImpl graphSet, ComposePlan composePlan, Connector connector, boolean updateMode) {
    List<ComposePlan.Move> list;
    try {
      list = composePlan.get();
    } catch (RuntimeException e) {
      log.error(e.getMessage());
      graphSet.setFailed();
      return;
    }
    for (ComposePlan.Move move : list) {

      // Skip if this operation only needs to be executed when updating
      if(!updateMode && move.onlyUpdate) {
        continue;
      }

      try {
        if(!updateMode && move.action == ComposePlan.Action.COPY) {
          log.info("Execute compose copy " + move.toString());
          connector.sparqlCopy(move.from.toString(), move.to.toString());
        } else {

          log.info("Execute compose add " + move.toString());
          connector.sparqlAdd(move.from.toString(), move.to.toString());
        }
      } catch (ConnectorException e) {
        log.error("Failed executing compose copy or add operation", e);
        graphSet.setFailed();
        return;
      }
    }

    for(Mapping mapping : graphSet.getMappings()) {
      try {
        connector.storeSigmaGraphExists(mapping.getGraphname(), mapping.getInclusionSet());
      } catch (ConnectorException e) {
        log.error("Failed saving sigma graph header", e);
        graphSet.setFailed();
        return;
      }
    }
  }

  // Returns true if loading was successful
  private static void executeLoad(Source source, Connector connector, ContainerFile container) throws ConnectorException {

    String fileName;
    String filePath = source.getPath();
    if(filePath == null) {
      fileName = source.getUri();
    } else {
      fileName = new File(source.getPath()).getName();
    }

    String context = generatePhiContext();
    ArrayList<String> contexts = new ArrayList<>();
    contexts.add(context);

    source.setStoreContext(context);

    log.info("Upload rdf-file to connector: " + filePath);
    DigestInputStream inputStream = FileFactory.toInputStream(source, container);
    connector.uploadFile(inputStream, fileName, source.getGraphname(), contexts);

    log.info("Uploaded, store phi graph header");
    connector.storePhiGraphExists(source, context, fileName, source.getHash());
  }

  public static String generatePhiContext() {
    return QueryFactory.VALIDATOR_HOST + "uploadedFile-" + RandomStringUtils.random(8, true, true);
  }

  // Extends wildcards and loads the namespace for each file
  public static ArrayList<Graph> loadList(List<Graph> originalGraphs, ContainerFile container) {

    // Each namespace should be filled from only one source (Graph)
    HashMap<String, Graph> namespaceToGraph = new HashMap<>();
    ArrayList<Graph> loadList = new ArrayList<>();

    // Explicit graphs
    for(Graph graph : originalGraphs) {

      if(Source.STORE.equals(graph.getSource().getType())) {
        loadList.add(graph);
      }

      else if(Source.FILE.equals(graph.getSource().getType()) ||
         Source.ONLINE.equals(graph.getSource().getType())) {

        File file = FileFactory.toFile(graph.getSource().asLocator());
        try {
          ArrayList<String> namespaces = new ArrayList<>();
          ArrayList<String> imports = new ArrayList<>();
          ArrayList<String> ontologies = new ArrayList<>();
          DescribeFactoryImpl.contextsInFile(new FileInputStream(file), file.getName(), namespaces, imports, ontologies, graph.getSource().getGraphname());
          for (String graphName : namespaces) {
            log.info("Found graph in file/online: " + graphName);

            if (containsNamespace(graphName, namespaceToGraph.keySet())) {
              throw new RuntimeException("Collision in graphs names, this one can be found in more than one source: " + graphName);
            }

            Graph clone = graph.clone();
            clone.getSource().setGraphname(graphName);
            namespaceToGraph.put(graphName, clone);
          }
        } catch (FileNotFoundException e) {
          throw new RuntimeException(e);
        }
      }

      else if(Source.CONTAINER.equals(graph.getSource().getType())) {
        Source source = graph.getSource();

        if(source.isContentFile()) {
          ArrayList<Graph> contentGraphs = DescribeFactoryImpl.contentGraphsInContainer(container, graph.getAs(), source.getPath(), source.getGraphname());
          for (Graph clone : contentGraphs) {
            String graphName = clone.getSource().getGraphname();
            log.info("Found graph in content file: " + graphName);
            if (containsNamespace(graphName, namespaceToGraph.keySet())) {
              throw new RuntimeException("Collision in graphs names, this one can be found in more than one source: " + graphName);
            }
            namespaceToGraph.put(graphName, clone);
          }

        } else if(source.isLibraryFile()) {
          ArrayList<Graph> libraryGraphs = DescribeFactoryImpl.libraryGraphsInContainer(container, graph.getAs(), source.getPath(), source.getGraphname());
          for (Graph clone : libraryGraphs) {
            String graphName = clone.getSource().getGraphname();
            log.info("Found graph in library file: " + graphName);
            if (containsNamespace(graphName, namespaceToGraph.keySet())) {
              throw new RuntimeException("Collision in implicit graphs names, this one can be found in more than one source: " + graphName);
            }
            namespaceToGraph.put(graphName, clone);
          }

        } else {
          throw new RuntimeException("The only location inside a container to address is inside the /bim/ or /bim/repository/ folder");
        }
      }

      else {
        throw new RuntimeException("Unsupported graph source type");
      }
    }

    loadList.addAll(namespaceToGraph.values());
    return loadList;
  }

  private static Map<String, Set<String>> reverseSigmaGraphsMap(Map<Set<String>, Set<String>> sigmaGraphs) {
    Map<String, Set<String>> map = new HashMap<>();
    for(Set<String> sigmaGraph : sigmaGraphs.keySet()) {
      String fingerPrint = fingerPrint(sigmaGraphs.get(sigmaGraph), "-");
      map.put(fingerPrint, sigmaGraph);
    }
    return map;
  }

  public static String fingerPrint(Set<String> items, String delimiter) {
    List<String> list = new ArrayList<>();
    list.addAll(items);
    sort(list);
    return "".join(delimiter, list);
  }

  public static String fingerPrintComposition(String context, Map<Set<String>, Set<String>> sigmaGraphsMap, Map<String, Set<String>> actualInferences, Set<String> cycleDetection) {
    if(cycleDetection == null) {
      cycleDetection = new HashSet<>();
      cycleDetection.add(context);
    }

    String inferencesFingerPrint = "";
    if(actualInferences.containsKey(context)) {
      inferencesFingerPrint = "("+fingerPrint(actualInferences.get(context), "&")+")";
    }

    String subGraphsFingerPrint = "";
    String subFingerPrint = "";
    for(Set<String> sigmaGraphs : sigmaGraphsMap.keySet()) {
      if(sigmaGraphs.contains(context)) {
        Set<String> subGraphs = sigmaGraphsMap.get(sigmaGraphs);
        subGraphsFingerPrint = fingerPrint(subGraphs, "-");

        Set<String> fragments = new HashSet<>();
        for(String subContext : subGraphs) {
          if(cycleDetection.contains(subContext)) {
            throw new RuntimeException("A cycle detected in the sigma graph inclusion");
          }
          fragments.add(fingerPrintComposition(subContext, sigmaGraphsMap, actualInferences, cycleDetection));
        }

        subFingerPrint = "["+fingerPrint(fragments, ",")+"]";
        break;
      }
    }
    return subGraphsFingerPrint + inferencesFingerPrint + subFingerPrint;
  }

  // Currently only support for inferencePreference map with one inferenceCode mapping to one graphVar
  // and each graphVar only mentioned once in the whole list
  private static String inferenceCodeNeedle(GraphVarImpl var, HashMap<GraphVarImpl, List<String>> phiGraphMap, HashMap<GraphVarImpl, List<GraphVarImpl>> graphVarMap, Map<String, Set<GraphVar>> inferencePreference, Map<String, String> contextToHash) {

    String needle = null;
    for(String inferenceCode : inferencePreference.keySet()) {
      if(inferencePreference.get(inferenceCode).contains(var)) {
        if(needle != null || inferencePreference.get(inferenceCode).size() > 1) {
          return null;
        }

        HashSet<GraphVarImpl> allVars = new HashSet<>();
        allVars.add(var);

        int size;
        do {
          size = allVars.size();
          for(GraphVarImpl item : allVars) {
            if(graphVarMap.containsKey(item)) {
              allVars.addAll(graphVarMap.get(item));
            }
          }
        } while (size < allVars.size());

        HashSet<String> phiGraphs = new HashSet<>();
        for(GraphVar graphVar : allVars) {
          if(phiGraphMap.containsKey(graphVar)) {
            phiGraphs.addAll(phiGraphMap.get(graphVar));
          }
        }

        Set<String> hashes = new HashSet<>();
        for(String context : phiGraphs) {
          hashes.add(contextToHash.get(context));
        }

        needle = fingerPrint(hashes, "-") +"|"+ inferenceCode ;
      }
    }
    return needle;
  }

  public static ComposePlan composeSigmaList(ContainerGraphSetImpl graphSet, Connector connector, List<Mapping> variables, List<Graph> originalGraphs,  Map<String, Set<GraphVar>> inferencePreference) {

    ComposePlan composePlan = new ComposePlan();

    HashMap<GraphVar, String> varMap = new HashMap();
    for(Mapping mapping : variables) {
      varMap.put(mapping.getVariable(), mapping.getGraphname());
    }

    // Create wish lists
    HashSet<GraphVarImpl> allGraphVars = new HashSet<>();
    HashMap<GraphVarImpl, List<String>> graphVarIncludesPhiGraphMap = new HashMap<>();
    HashMap<GraphVarImpl, List<GraphVarImpl>> graphVarIncludesGraphVarMap = new HashMap<>();
    for (Graph graph : originalGraphs) {

      if(Source.STORE.equals(graph.getSource().getType())) {

        for (GraphVarImpl as : graph.getAs()) {
          if(!allGraphVars.contains(as)) {
            allGraphVars.add(as);
          }
          if (!graphVarIncludesGraphVarMap.keySet().contains(as)) {
            graphVarIncludesGraphVarMap.put(as, new ArrayList<>());
          }
          List<GraphVarImpl> inclusions = graphVarIncludesGraphVarMap.get(as);
          if (graph.getSource().getGraph() == null) {
            throw new RuntimeException("Assuming a source that has one graph if it is not a source of type store");
          }
          inclusions.add(graph.getSource().getGraph());
        }

      } else {

        for (GraphVarImpl as : graph.getAs()) {
          if(!allGraphVars.contains(as)) {
            allGraphVars.add(as);
          }
          if (!graphVarIncludesPhiGraphMap.keySet().contains(as)) {
            graphVarIncludesPhiGraphMap.put(as, new ArrayList<>());
          }
          List<String> inclusions = graphVarIncludesPhiGraphMap.get(as);
          if (graph.getSource().getStoreContext() == null || graph.getSource().getStoreContext().isEmpty()) {
            throw new RuntimeException("Assuming a source that has no graphs (so it is not a source of type store) should have a store context");
          }
          inclusions.add(graph.getSource().getStoreContext());
        }
      }
    }

    List<Mapping> updatedVarList = new ArrayList<>();
    HashMap<Set<String>, List<GraphVarImpl>> fromSetsToFix = new HashMap<>();

    // Find a graphVar that is not mapped and is not included by an unmapped graphVar
    HashMap<GraphVarImpl, String> mappedGraphVars = new HashMap<>();
    GraphVarImpl next = null;

    while(mappedGraphVars.size() < allGraphVars.size()) {
      for (GraphVarImpl var : allGraphVars) {
        if (mappedGraphVars.keySet().contains(var)) {
          continue;
        }

        GraphVar includesThisUnprocessedGraph = null;
        for (GraphVar includer : graphVarIncludesGraphVarMap.keySet()) {
          if (!mappedGraphVars.keySet().contains(includer) && graphVarIncludesGraphVarMap.get(includer).contains(var)) {
            includesThisUnprocessedGraph = includer;
            break;
          }
        }
        // Some graph includes this one, try the next
        if (includesThisUnprocessedGraph != null) {
          continue;
        }
        next = var;
      }

      if (next == null) {
        throw new RuntimeException("Not able to find the next GraphVar to map");
      }

      // Now mapping this graphVar
      String mappedContext = varMap.get(next);

      // Include phi graph
      if(graphVarIncludesPhiGraphMap.containsKey(next)) {
        List<String> froms = graphVarIncludesPhiGraphMap.get(next);
        if (froms.size() > 0) {
          for (int i = 0; i < froms.size() - 1; i++) {
            composePlan.addReversed(ComposePlan.Action.ADD, asResource(froms.get(i)), asResource(mappedContext), false);
          }
          composePlan.addReversed(ComposePlan.Action.COPY, asResource(froms.get(froms.size() - 1)), asResource(mappedContext), false);
        }

        Set<String> fromSet = new HashSet<>();
        fromSet.addAll(froms);

        Mapping mapping = new Mapping(next, mappedContext, null, null, fromSet, new HashSet());
        mapping.setInitialized();
        updatedVarList.add(mapping);
      }

      // Include sigma graph
      if (graphVarIncludesGraphVarMap.containsKey(next)) {
        List<GraphVarImpl> froms = graphVarIncludesGraphVarMap.get(next);
        if (froms.size() > 0) {
          for (int i = 0; i < froms.size() - 1; i++) {
            composePlan.addReversed(ComposePlan.Action.ADD, froms.get(i), asResource(mappedContext), false);
          }
          composePlan.addReversed(ComposePlan.Action.COPY, froms.get(froms.size() - 1), asResource(mappedContext), false);
        }

        Set<String> fromSet = new HashSet<>();
        fromSetsToFix.put(fromSet, froms);

        Mapping mapping = new Mapping(next, mappedContext, null, null, fromSet, new HashSet());
        mapping.setInitialized();
        updatedVarList.add(mapping);
      }
      mappedGraphVars.put(next, mappedContext);
      composePlan.updateFroms(next, asResource(mappedContext));
    }

    for(Set<String> setToFill : fromSetsToFix.keySet()) {
      for(GraphVarImpl var : fromSetsToFix.get(setToFill)) {
        setToFill.add(mappedGraphVars.get(var));
      }
    }

    graphSet.setVariables(updatedVarList);
    return composePlan;
  }
}
