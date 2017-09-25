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
import com.sysunite.coinsweb.rdfutil.Utils;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.util.*;

import static com.sysunite.coinsweb.connector.Rdf4jConnector.asResource;
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

//    Map<String, String> originalContextToHashMap = new HashMap<>();
//    ArrayList<Graph> loadList = loadList(originalGraphs, container);
    ArrayList<Graph> phiGraphs = new ArrayList<>();
    for (Graph graph : loadList) {
      if(Source.ONLINE.equals(graph.getSource().getType()) ||
      Source.CONTAINER.equals(graph.getSource().getType()) ||
      Source.FILE.equals(graph.getSource().getType())) {
        phiGraphs.add(graph);


//        FileFactory.calculateAndSetHash(graph.getSource(), container);
//        originalContextToHashMap.put(withoutHash(graph.getSource().getGraphname()), graph.getSource().getHash());

      }
    }



    HashMap<String, String> changeMap = new HashMap<>();
    HashSet<String> doneImports = new HashSet<>();
    while(!phiGraphs.isEmpty()) {

      boolean foundOne = false;
      for(Graph phiGraph : phiGraphs) {
        ArrayList<String> imports = FileFactory.getImports(phiGraph.getSource(), container);
        if (doneImports.containsAll(imports)) {

//          HashMap<String, String> originalContextsWithHash = new HashMap<>();
//          for(String context : imports) {
//
//
//            // If one was already selected, use that
//            if(doneImports.keySet().contains(context)) {
//              originalContextsWithHash.put(doneImports.get(context), null);
//
//            } else {
//              originalContextsWithHash.put(context, originalContextToHashMap.get(context));
//            }
//          }


          // If there is already an identical file with identical uploads use that instead

//          String register = null;


          executeLoad(phiGraph.getSource(), connector, container);
//          register = withoutHash(phiGraph.getSource().getStoreContext());

          changeMap.put(withoutHash(phiGraph.getSource().getGraphname()), withoutHash(phiGraph.getSource().getStoreContext()));

          for(String originalContext : changeMap.keySet()) {
            try {
              connector.replaceResource(phiGraph.getSource().getStoreContext(), originalContext, changeMap.get(originalContext));
            } catch (ConnectorException e) {
              log.error("Failed replacing resource", e);
              graphSet.setFailed();
            }
          }




          doneImports.add(withoutHash(phiGraph.getSource().getGraphname()));


          foundOne = true;
          phiGraphs.remove(phiGraph);
          break;
        }
      }
      if(!foundOne) {
        log.error("For these graphs no source can be found: ");
        for(Graph graph : phiGraphs) {
          log.error("- " + graph.getSource().getGraphname());
        }
        graphSet.setFailed();
        return null;
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
  private static boolean executeLoad(Source source, Connector connector, ContainerFile container) {

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
    try {
      connector.uploadFile(inputStream, fileName, source.getGraphname(), contexts);
    } catch (ConnectorException e) {
      log.error("Error uploading file", e);
      return false;
    }

    log.info("Uploaded, store phi graph header");
    try {
      connector.storePhiGraphExists(source, context, fileName, source.getHash());
    } catch (ConnectorException e) {
      log.error("Failed saving phi graph header", e);
      return false;
    }
    return true;
  }


  public static String generatePhiContext() {
    return QueryFactory.VALIDATOR_HOST + "uploadedFile-" + RandomStringUtils.random(8, true, true);
  }

  // Extends wildcards and loads the namespace for each file
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

    ArrayList<Graph> contentGraphs;
    ArrayList<Graph> libraryGraphs;

    if(allContentFile != null) {
      contentGraphs = DescribeFactoryImpl.contentGraphsInContainer(container, allContentFile.getAs());
      for(Graph graph : contentGraphs) {
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
      libraryGraphs = DescribeFactoryImpl.libraryGraphsInContainer(container, allLibraryFile.getAs());
      for(Graph graph : libraryGraphs) {
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
          ArrayList<String> namespaces = new ArrayList<>();
          ArrayList<String> imports = new ArrayList<>();
          DescribeFactoryImpl.contextsInFile(new FileInputStream(file), file.getName(), namespaces, imports);
          for (String graphName : namespaces) {
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

//    Map<String, Set<String>> hashToContext = connector.listPhiContextsPerHash();
//    Map<String, String> contextToHash = new HashMap<>();
//    for(String hash : hashToContext.keySet()) {
//      Set<String> contexts = hashToContext.get(hash);
//      for(String context : contexts) {
//        contextToHash.put(context, hash);
//      }
//    }
//    Map<String, String> contextToFileName = connector.listFileNamePerPhiContext();


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
