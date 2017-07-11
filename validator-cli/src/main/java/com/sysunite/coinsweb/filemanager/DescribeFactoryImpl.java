package com.sysunite.coinsweb.filemanager;

import com.sysunite.coinsweb.parser.config.factory.DescribeFactory;
import com.sysunite.coinsweb.parser.config.factory.FileFactory;
import com.sysunite.coinsweb.parser.config.pojo.ConfigFile;
import com.sysunite.coinsweb.parser.config.pojo.Container;
import com.sysunite.coinsweb.parser.config.pojo.Graph;
import com.sysunite.coinsweb.parser.config.pojo.Source;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.helpers.StatementCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

/**
 * @author bastbijl, Sysunite 2017
 */
public class DescribeFactoryImpl implements DescribeFactory {

  private static final Logger log = LoggerFactory.getLogger(DescribeFactoryImpl.class);






  public static void expandGraphConfig(ConfigFile configFile) {
    for(Container container : configFile.getRun().getContainers()) {
      ContainerFile containerFile = null;
      if(!container.isVirtual()) {
        containerFile = new ContainerFileImpl(FileFactory.toFile(container.getLocation()).getPath());
      }
      container.setGraphs(loadList(container.getGraphs(), containerFile).toArray(new Graph[0]));
    }
  }

  public static ArrayList<Graph> loadList(Graph[] originalGraphs, ContainerFile container) {

//    ArrayList<Graph> loadList = new ArrayList();

    Graph allContentFile = null;
    Graph allLibraryFile = null;

    // Explicit graphs
    ArrayList<String> explicitGraphs = new ArrayList();
    for(Graph graph : originalGraphs) {
      if(!graph.getSource().anyGraph()) {
        String graphName = graph.getSource().getGraphname();
        if(explicitGraphs.contains(graphName)) {
          throw new RuntimeException("The namespace "+graphName+ " is being mentioned more than once, this is not allowed");
        }
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
            throw new RuntimeException("Only one graph with content file asterisk allowed");
          }
          allLibraryFile = graph;
        }
      }
    }

    // Implicit graphs
    ArrayList<Graph> loadList = new ArrayList();
    ArrayList<String> implicitGraphs = new ArrayList();

    if(allContentFile != null) {
      for(Graph graph : contentGraphsInContainer(container, allContentFile.getAs())) {
        String graphName = graph.getSource().getGraphname();
        if(!explicitGraphs.contains(graphName)) {
          log.info("Will load content file from wildcard definition");
          if(implicitGraphs.contains(graphName)) {
            throw new RuntimeException("Collision in implicit graphs names, this one can be found in more than one source: "+graphName);
          }
          implicitGraphs.add(graphName);
          loadList.add(graph);
        }
      }
    }

    if(allLibraryFile != null) {
      for(Graph graph : libraryGraphsInContainer(container, allLibraryFile.getAs())) {
        String graphName = graph.getSource().getGraphname();
        if(!explicitGraphs.contains(graphName)) {
          log.info("Will load library file from wildcard definition");
          if(implicitGraphs.contains(graphName)) {
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
            if (!explicitGraphs.contains(graphName)) {
              log.info("Will load graph from file because of wildcard graph definition");
              if (implicitGraphs.contains(graphName)) {
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

    for(Graph graph : originalGraphs) {
      if(!graph.getSource().anyGraph()) {

        // Check if the file in the container is available
        if(Source.CONTAINER.equals(graph.getSource().getType())) {
          try {
            container.getFile(Paths.get(graph.getSource().getPath()));
          } catch(RuntimeException e) {
            throw e;
          }
        }

        log.info("Will load explicitly defined file");
        loadList.add(graph);
      }
    }
    return loadList;
  }


  public ArrayList<Graph> graphsInContainer(File file) {

    ContainerFileImpl containerFile = new ContainerFileImpl(file.getPath());

    ArrayList<Graph> graphs = new ArrayList();
    graphs.addAll(contentGraphsInContainer(containerFile));
    graphs.addAll(libraryGraphsInContainer(containerFile));
    return graphs;
  }
  public static ArrayList<Graph> contentGraphsInContainer(ContainerFile containerFile) {
    return contentGraphsInContainer(containerFile, new ArrayList<>(Arrays.asList("INSTANCE_UNION_GRAPH", "FULL_UNION_GRAPH")));
  }
  public static ArrayList<Graph> contentGraphsInContainer(ContainerFile containerFile, ArrayList<String> content) {
    ArrayList<Graph> graphs = new ArrayList();
    for(String contentFile : containerFile.getContentFiles()) {

      DeleteOnCloseFileInputStream inputStream = containerFile.getContentFile(contentFile);
      try {
        for (String namespace : namespacesForFile(inputStream, contentFile)) {

          Source source = new Source();
          source.setType("container");
          source.setPath(containerFile.getContentFilePath(contentFile).toString());
          source.setGraphname(namespace);

          Graph graph = new Graph();
          graph.setSource(source);
          graph.setAs(content);
          graphs.add(graph);
        }
      } catch (RuntimeException e) {
        log.warn(e.getMessage());
      }
    }
    return graphs;
  }
  public static ArrayList<Graph> libraryGraphsInContainer(ContainerFile containerFile) {
    return libraryGraphsInContainer(containerFile, new ArrayList<>(Arrays.asList("SCHEMA_UNION_GRAPH", "FULL_UNION_GRAPH")));
  }
  public static ArrayList<Graph> libraryGraphsInContainer(ContainerFile containerFile, ArrayList<String> content) {
    ArrayList<Graph> graphs = new ArrayList();
    for(String repositoryFile : containerFile.getRepositoryFiles()) {

      DeleteOnCloseFileInputStream inputStream = containerFile.getRepositoryFile(repositoryFile);
      try {
        for (String namespace : namespacesForFile(inputStream, repositoryFile)) {

          Source source = new Source();
          source.setType("container");
          source.setPath(containerFile.getRepositoryFilePath(repositoryFile).toString());
          source.setGraphname(namespace);

          Graph graph = new Graph();
          graph.setSource(source);
          graph.setAs(content);
          graphs.add(graph);
        }
      } catch (RuntimeException e) {
        log.warn(e.getMessage());
      }
    }
    return graphs;
  }


  // todo  handle error reading file: make sure the file system validator checks this first
  public static ArrayList<String> namespacesForFile(File file) throws FileNotFoundException {
    return namespacesForFile(new FileInputStream(file), file.getName());
  }
  public static ArrayList<String> namespacesForFile(FileInputStream inputStream, String fileName) {

    String backupNamespace = "http://default/"+fileName;

    ArrayList<String> namespaces = new ArrayList();

    log.info("Determine file type for file: "+fileName);
    Optional<RDFFormat> format = Rio.getParserFormatForFileName(fileName);
    if(!format.isPresent()) {
      throw new RuntimeException("Not able to determine format of file: " + fileName);
    }
    Model model = new LinkedHashModel();
    RDFParser rdfParser = Rio.createParser(format.get());
    rdfParser.setRDFHandler(new StatementCollector(model));

    try {
      rdfParser.parse(inputStream, backupNamespace);
    } catch (Exception e) {
      throw new RuntimeException(e.getMessage());
    }

    // If there are contexts, use these
    for(Resource context : model.contexts()) {
      if(context != null) {
        log.info("Found context in file: "+context.toString());
        namespaces.add(context.toString());
      }
    }

    // If no contexts, use the empty namespace
    if(namespaces.size() < 1) {
      Optional<Namespace> namespace = model.getNamespace("");
      if (namespace.isPresent()) {
        log.info("No context found to represent this file, falling back to empty prefix namespace: "+namespace.get().getName());
        namespaces.add(namespace.get().getName());
      }
    }

    // If still no namespace
    if(namespaces.size() < 1) {
      log.warn("No context found to represent this file, falling back to " + backupNamespace);
      namespaces.add(backupNamespace);
    }
    return namespaces;
  }
}
