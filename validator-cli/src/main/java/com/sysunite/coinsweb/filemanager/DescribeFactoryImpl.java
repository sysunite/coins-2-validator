package com.sysunite.coinsweb.filemanager;

import com.sysunite.coinsweb.connector.Connector;
import com.sysunite.coinsweb.connector.NullModel;
import com.sysunite.coinsweb.connector.Rdf4jUtil;
import com.sysunite.coinsweb.graphset.ContainerGraphSetFactory;
import com.sysunite.coinsweb.graphset.QueryFactory;
import com.sysunite.coinsweb.parser.config.factory.ConfigFactory;
import com.sysunite.coinsweb.parser.config.factory.DescribeFactory;
import com.sysunite.coinsweb.parser.config.pojo.*;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.helpers.StatementCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

/**
 * @author bastbijl, Sysunite 2017
 */
public class DescribeFactoryImpl implements DescribeFactory {

  private static final Logger log = LoggerFactory.getLogger(DescribeFactoryImpl.class);

  public static void expandGraphConfig(ConfigFile configFile) {
    for(Container container : configFile.getRun().getContainers()) {
      expandGraphConfig(container);
    }
  }
  public static void expandGraphConfig(Container container) {

    log.info("Expand graph settings for container of type "+container.getType());
    ContainerFile containerFile = null;
    if(!container.isVirtual()) {
      containerFile = new ContainerFileImpl(container);
    }
    ArrayList<Graph> expandedGraphs = ContainerGraphSetFactory.loadList(container.getGraphs(), containerFile);
    container.setGraphs(expandedGraphs);

    if(container.getVariables().isEmpty()) {
      container.setVariables(ConfigFactory.getDefaultMapping(expandedGraphs));
    }
  }
  public static void expandGraphConfig(Container container, ContainerFileImpl containerFile) {

    log.info("Expand graph settings for container of type "+container.getType());
    ArrayList<Graph> expandedGraphs = ContainerGraphSetFactory.loadList(container.getGraphs(), containerFile);
    container.setGraphs(expandedGraphs);

    if(container.getVariables().isEmpty()) {
      container.setVariables(ConfigFactory.getDefaultMapping(expandedGraphs));
    }
  }

  public ArrayList<Graph> graphsInContainerGraphSet(Connector connector) {
    ArrayList<Graph> graphs = new ArrayList();
    return graphs;
  }


  public ArrayList<Graph> graphsInContainerFile(File file, ArrayList<GraphVarImpl> dataGraphs, ArrayList<GraphVarImpl> schemaGraphs) {
    if(!(file instanceof ContainerFileImpl)) {
      throw new RuntimeException("Please call graphsInContainer with a ContainerFileImpl as argument");
    }
    ContainerFileImpl containerFile = (ContainerFileImpl) file;
    ArrayList<Graph> graphs = new ArrayList();
    graphs.addAll(contentGraphsInContainer(containerFile, dataGraphs));
    graphs.addAll(libraryGraphsInContainer(containerFile, schemaGraphs));
    return graphs;
  }
  public static ArrayList<Graph> contentGraphsInContainer(ContainerFile containerFile, ArrayList<GraphVarImpl> content) {
    ArrayList<Graph> graphs = new ArrayList();
    for(String contentFile : containerFile.getContentFiles()) {
      
      log.info("Look for graphs in content file "+contentFile);
      try {
        for (String namespace : containerFile.getContentFileNamespaces(contentFile)) {

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
  public static ArrayList<Graph> libraryGraphsInContainer(ContainerFile containerFile, ArrayList<GraphVarImpl> content) {
    ArrayList<Graph> graphs = new ArrayList();
    for(String repositoryFile : containerFile.getRepositoryFiles()) {

      log.info("Look for graphs in content file "+repositoryFile);
      try {
        for (String namespace : containerFile.getRepositoryFileNamespaces(repositoryFile)) {

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


  public static void namespacesForFile(InputStream inputStream, String fileName, HashMap<String, ArrayList<String>> namespacesMap, HashMap<String, ArrayList<String>> importsMap) {

    ArrayList<String> namespaces = new ArrayList<>();
    ArrayList<String> imports = new ArrayList<>();

    namespacesForFile(inputStream, fileName, namespaces, imports);

    namespacesMap.put(fileName, namespaces);
    importsMap.put(fileName, imports);
  }
  public static void namespacesForFile(InputStream inputStream, String fileName, ArrayList<String> namespaces, ArrayList<String> imports) {

    String backupNamespace = QueryFactory.VALIDATOR_HOST + fileName;

    ArrayList<String> result = new ArrayList();


    RDFFormat format = Rdf4jUtil.interpretFormat(fileName);
    if(format == null) {
      throw new RuntimeException("Not able to determine format of file: " + fileName);
    }
    log.info("Found file type "+format.getName()+" for file: "+fileName);
    log.info("Parse the file to determine contexts");

    NullModel model = new NullModel();
    RDFParser rdfParser = Rio.createParser(format);
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
        result.add(context.toString());
      }
    }

    // If no contexts, use the empty namespace
    if(result.size() < 1) {
      Optional<Namespace> namespace = model.getNamespace("");
      if (namespace.isPresent()) {
        log.info("No context found to represent this file, falling back to empty prefix namespace: "+namespace.get().getName());
        result.add(namespace.get().getName());
      }
    }

    // If still no namespace
    if(result.size() < 1) {
      log.warn("No context found to represent this file, falling back to " + backupNamespace);
      result.add(backupNamespace);
    }

    namespaces.addAll(result);
    imports.addAll(model.getImports());
  }
}
