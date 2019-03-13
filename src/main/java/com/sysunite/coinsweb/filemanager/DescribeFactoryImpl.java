package com.sysunite.coinsweb.filemanager;

import com.sysunite.coinsweb.connector.Connector;
import com.sysunite.coinsweb.connector.OutlineModel;
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
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;

import static com.sysunite.coinsweb.rdfutil.Utils.equalNamespace;
import static com.sysunite.coinsweb.rdfutil.Utils.withoutHash;

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
  public static void expandGraphConfig(Container containerConfig, ContainerFileImpl containerFile) {

    log.info("Expand graph settings for container of type "+containerConfig.getType());
    ArrayList<Graph> expandedGraphs = ContainerGraphSetFactory.loadList(containerConfig.getGraphs(), containerFile);
    containerConfig.setGraphs(expandedGraphs);

    if(containerConfig.getVariables().isEmpty()) {
      containerConfig.setVariables(ConfigFactory.getDefaultMapping(expandedGraphs));
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
    return contentGraphsInContainer(containerFile, content, "bim/*", "*");
  }
  public static ArrayList<Graph> contentGraphsInContainer(ContainerFile containerFile, ArrayList<GraphVarImpl> content, String pathPattern, String namespacePattern) {
    ArrayList<Graph> graphs = new ArrayList();
    for(String contentFile : containerFile.getContentFiles()) {

      log.info("Look for graphs in content file " + contentFile);
      try {
        for (String namespace : containerFile.getContentFileNamespaces(contentFile)) {
          if(filterNamespace(namespace, namespacePattern)) {
            String path = containerFile.getContentFilePath(contentFile).toString();
            if (filterPath(path, pathPattern)) {

              Source source = new Source();
              source.setType("container");
              source.setPath(path);
              source.setGraphname(namespace);

              Graph graph = new Graph();
              graph.setSource(source);
              graph.setAs(content);
              graphs.add(graph);
            }
          }
        }
      } catch (RuntimeException e) {
        log.warn(e.getMessage());
      }
    }
    return graphs;
  }
  public static ArrayList<Graph> libraryGraphsInContainer(ContainerFile containerFile, ArrayList<GraphVarImpl> content) {
    return libraryGraphsInContainer(containerFile, content, "bim/repository/*", "*");
  }
  public static ArrayList<Graph> libraryGraphsInContainer(ContainerFile containerFile, ArrayList<GraphVarImpl> content, String pathPattern, String namespacePattern) {
    ArrayList<Graph> graphs = new ArrayList();
    for(String repositoryFile : containerFile.getRepositoryFiles()) {

      log.info("Look for graphs in content file " + repositoryFile);
      try {
        ArrayList<String> namespaces = containerFile.getRepositoryFileNamespaces(repositoryFile);
        for (String namespace : namespaces) {
          if(filterNamespace(namespace, namespacePattern)) {
            String path = containerFile.getRepositoryFilePath(repositoryFile).toString();
            if(filterPath(path, pathPattern)) {

              Source source = new Source();
              source.setType("container");
              source.setPath(path);
              source.setGraphname(namespace);

              Graph graph = new Graph();
              graph.setSource(source);
              graph.setAs(content);
              graphs.add(graph);
            }
          }
        }
      } catch (RuntimeException e) {
        log.warn(e.getMessage());
      }
    }
    return graphs;
  }

  /**
   * To represent each set of triples in a file URI's are used
   *
   * Finding them is tried in this order:
   *
   * - If the file consists of contexts itself (e.g. nquad or trix files) these are returned
   * - If no context is specified (subj) -rdf:type-&gt;  owl:Ontology definitions are collected and returned
   * - Use the namespace with the empty prefix
   * - Fall back to the backup namespace that is composed of the fileName
   */
  public static void contextsInFile(InputStream inputStream, String fileName,
                                    ArrayList<String> resultContexts,
                                    ArrayList<String> resultImports,
                                    ArrayList<String> resultOntologies,
                                    String namespacePattern) {

    // Init
    String backupNamespace = QueryFactory.VALIDATOR_HOST + fileName;
    RDFFormat format = Rdf4jUtil.interpretFormat(fileName);
    if(format == null) {
      throw new RuntimeException("Not able to determine format of file: " + fileName);
    }
    log.info("Found file type "+format.getName()+" for file: "+fileName);
    log.info("Parse the file to determine contexts");
    OutlineModel model = new OutlineModel();
    RDFParser rdfParser = Rio.createParser(format);
    rdfParser.setRDFHandler(new StatementCollector(model));
    try {
      rdfParser.parse(inputStream, backupNamespace);
    } catch (Exception e) {
      throw new RuntimeException(e.getMessage());
    }

    // Fill lists
    for(Resource resource : model.getOntologies()) {
      resultOntologies.add(resource.stringValue());
    }
    resultImports.addAll(model.getImports());

    // If there are contexts, use these
    HashSet<String> uniqueContexts = new HashSet<>();
    String contextString;
    for(Resource context : model.getContexts()) {
      if(context != null) {
        contextString = withoutHash(context.stringValue());
        if(filterNamespace(contextString, namespacePattern)) {
          log.info("Add context to represent set triples in file: " + contextString);
          uniqueContexts.add(contextString);
        }
      }
    }
    if(uniqueContexts.size() > 0) {
      resultContexts.addAll(uniqueContexts);
      return;
    }

    // If not, look for ontologies
    for(String ontology : resultOntologies) {
      if(ontology != null && !equalNamespace(backupNamespace, ontology)) {
        contextString = withoutHash(ontology);
        if(filterNamespace(contextString, namespacePattern)) {
          log.info("Add ontology to represent set triples in file: " + contextString);
          uniqueContexts.add(contextString);
        }
      }
    }
    if(uniqueContexts.size() > 0) {
      resultContexts.addAll(uniqueContexts);
      return;
    }

    // If no contexts, use the empty namespace
    Optional<Namespace> namespace = model.getNamespace("");
    if (namespace.isPresent()) {
      contextString = withoutHash(namespace.get().getName());
      if(filterNamespace(contextString, namespacePattern)) {
        log.info("Add empty-prefix namespace to represent set triples in file: " + namespace.get().getName());
        uniqueContexts.add(contextString);
      }
    }
    if(uniqueContexts.size() > 0) {
      resultContexts.addAll(uniqueContexts);
      return;
    }

    // If still no namespace
    if(filterNamespace(backupNamespace, namespacePattern)) {
      log.info("Add default namespace to represent set triples in file: " + backupNamespace);
      resultContexts.add(backupNamespace);
      return;
    }
  }

  public static void contextsInFile(InputStream inputStream, String fileName,
                                    HashMap<String, ArrayList<String>> namespacesMap,
                                    HashMap<String, ArrayList<String>> importsMap,
                                    HashMap<String, ArrayList<String>> ontologiesMap,
                                    String namespacePattern) {

    ArrayList<String> contexts = new ArrayList<>();
    ArrayList<String> imports = new ArrayList<>();
    ArrayList<String> ontologies = new ArrayList<>();

    contextsInFile(inputStream, fileName, contexts, imports, ontologies, namespacePattern);

    namespacesMap.put(fileName, contexts);
    importsMap.put(fileName, imports);
    ontologiesMap.put(fileName, ontologies);
  }

  public static boolean filterPath(String path, String pattern) {
    PathMatcher matcher =  FileSystems.getDefault().getPathMatcher("glob:" + pattern);
    return matcher.matches(Paths.get(path));
  }

  public static boolean filterNamespace(String namespace, String pattern) {
    return "*".equals(pattern) || namespace.equals(pattern);
  }
}
