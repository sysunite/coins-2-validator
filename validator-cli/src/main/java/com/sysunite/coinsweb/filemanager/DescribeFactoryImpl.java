package com.sysunite.coinsweb.filemanager;

import com.sysunite.coinsweb.connector.Connector;
import com.sysunite.coinsweb.graphset.ContainerGraphSetFactory;
import com.sysunite.coinsweb.graphset.QueryFactory;
import com.sysunite.coinsweb.parser.config.factory.ConfigFactory;
import com.sysunite.coinsweb.parser.config.factory.DescribeFactory;
import com.sysunite.coinsweb.parser.config.pojo.*;
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
import java.io.InputStream;
import java.security.DigestInputStream;
import java.util.ArrayList;
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

      DigestInputStream inputStream = containerFile.getContentFile(contentFile);
      inputStream.on(false);
      log.info("Look for graphs in content file "+contentFile);
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
  public static ArrayList<Graph> libraryGraphsInContainer(ContainerFile containerFile, ArrayList<GraphVarImpl> content) {
    ArrayList<Graph> graphs = new ArrayList();
    for(String repositoryFile : containerFile.getRepositoryFiles()) {

      DigestInputStream inputStream = containerFile.getRepositoryFile(repositoryFile);
      inputStream.on(false);
      log.info("Look for graphs in content file "+repositoryFile);
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


  public static ArrayList<String> namespacesForFile(File file) throws FileNotFoundException {
    return namespacesForFile(new FileInputStream(file), file.getName());
  }
  public static ArrayList<String> namespacesForFile(InputStream inputStream, String fileName) {

    String backupNamespace = QueryFactory.VALIDATOR_HOST + fileName;

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
