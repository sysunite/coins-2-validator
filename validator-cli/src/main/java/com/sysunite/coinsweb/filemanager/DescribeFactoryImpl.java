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
        ArrayList<String> namespaces = containerFile.getRepositoryFileNamespaces(repositoryFile);
        for (String namespace : namespaces) {

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
  public static void contextsInFile(InputStream inputStream, String fileName, ArrayList<String> resultContexts, ArrayList<String> resultImports) {

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

    // Store imports
    resultImports.addAll(model.getImports());


    HashSet<String> result = new HashSet<>();

    // If there are contexts, use these
    for(Resource context : model.getContexts()) {
      if(context != null) {
        String contextString = withoutHash(context.stringValue());
        log.info("Add context to represent set triples in file: "+contextString);
        result.add(contextString);
      }
    }
    if(result.size() > 0) {
      resultContexts.addAll(result);
      return;
    }

    // If not, look for ontologies
    for(Resource ontology : model.getOntologies()) {
      if(ontology != null && !equalNamespace(backupNamespace, ontology.stringValue())) {
        String ontologyString = withoutHash(ontology.stringValue());
        log.info("Add ontology to represent set triples in file: "+ontologyString);
        result.add(ontologyString);
      }
    }
    if(result.size() > 0) {
      resultContexts.addAll(result);
      return;
    }

    // If no contexts, use the empty namespace
    Optional<Namespace> namespace = model.getNamespace("");
    if (namespace.isPresent()) {
      String namespaceString = withoutHash(namespace.get().getName());
      log.info("Add empty-prefix namespace to represent set triples in file: "+namespace.get().getName());
      result.add(namespaceString);
    }
    if(result.size() > 0) {
      resultContexts.addAll(result);
      return;
    }

    // If still no namespace
    log.info("Add default namespace to represent set triples in file: "+namespace.get().getName());
    resultContexts.add(backupNamespace);
  }

  public static void contextsInFile(InputStream inputStream, String fileName, HashMap<String, ArrayList<String>> namespacesMap, HashMap<String, ArrayList<String>> importsMap) {

    ArrayList<String> contexts = new ArrayList<>();
    ArrayList<String> imports = new ArrayList<>();

    contextsInFile(inputStream, fileName, contexts, imports);

    namespacesMap.put(fileName, contexts);
    importsMap.put(fileName, imports);
  }

}
