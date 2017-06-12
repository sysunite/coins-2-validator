package com.sysunite.coinsweb.graphset;

import com.sysunite.coinsweb.connector.Connector;
import com.sysunite.coinsweb.filemanager.ContainerFile;
import com.sysunite.coinsweb.filemanager.FileFactory;
import com.sysunite.coinsweb.parser.config.ConfigFile;
import com.sysunite.coinsweb.parser.config.Container;
import com.sysunite.coinsweb.parser.config.Graph;
import com.sysunite.coinsweb.steps.ValidationStepResult;
import com.sysunite.coinsweb.validator.InferenceQueryResult;
import com.sysunite.coinsweb.validator.ValidationQueryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author bastbijl, Sysunite 2017
 */
public class ContainerGraphSetImpl implements ContainerGraphSet {

  private static final Logger log = LoggerFactory.getLogger(ContainerGraphSetImpl.class);

  private boolean disabled;
  private Connector connector;
  private ContainerFile container;
  private Container containerConfig;
  private ConfigFile configFile;
  private HashMap<String, String> graphs;



  public ContainerGraphSetImpl() {
    this.disabled = true;
  }

  public ContainerGraphSetImpl(Connector connector, HashMap<String, String> graphs) {
    this.disabled = false;
    this.connector = connector;
    this.graphs = graphs;
  }

  private void load() {

    if(disabled) {
      return;
    }

    for(Graph graph : containerConfig.getGraphs()) {

      String[] graphNames = new String[graph.getContent().size()];
      for(int i = 0; i < graph.getContent().size(); i++) {
        graphNames[i] = graphs.get(graph.getContent().get(i));
      }
      String fileName = graph.getPath();
      if(fileName == null) {
        fileName = graph.getUri();
      }
      connector.uploadFile(FileFactory.toInputStream(graph, container, configFile), fileName, graph.getGraphname(), graphNames);
    }
    this.connector.setAllLoaded();
  }

  public ValidationQueryResult select(String query) {

    String errorMessage = null;
    boolean passed = false;
    long start = new Date().getTime();

    Iterator<Map<String, String>> resultSet = null;
    ArrayList<String> formattedResults = new ArrayList<>();

    if(connector.requiresLoad()) {
      load();
    }
    connector.query(query);

//    try {
//
//      List<Map<String, String>> result = new ArrayList<>();
//
//      ResultSet results = getResultSet(queryString, getValidationDataset());
//
//      passed = !results.hasNext();
//
//      // Output query results
//      while (results.hasNext()) {
//
//        HashMap<String, String> resultRow = new HashMap();
//
//        QuerySolution row = results.next();
//
//        Iterator columnNames = row.varNames();
//        while(columnNames.hasNext()) {
//          String columnName = (String) columnNames.next();
//          RDFNode item = row.get(columnName);
//          if(item.isAnon()) {
//            resultRow.put(columnName, "BLANK");
//          } else if(item.isResource()) {
//            String value = item.asResource().getURI();
//            if(value == null) {
//              value = "NON INTERPRETABLE URI";
//            }
//            resultRow.put(columnName, value);
//          } else if(item.isLiteral()) {
//            String value = item.asLiteral().getLexicalForm();
//            if(value == null) {
//              value = "NON INTERPRETABLE LITERAL";
//            }
//            resultRow.put(columnName, value);
//          } else {
//            resultRow.put(columnName, "NOT INTERPRETED");
//            log.warn("Skipping a result from the query "+validationQuery.getReference()+".");
//          }
//        }
//
//        formattedResults.add(validationQuery.formatResult(resultRow));
//
//        result.add(resultRow);
//      }
//
//      resultSet = result.iterator();
//
//
//      if(passed) {
//        log.trace("Query "+validationQuery.getReference()+" found no results, passed.");
//      } else {
//        log.trace("For query "+validationQuery.getReference()+" results where found, not passing validation.");
//
//      }
//
//    } catch (QueryParseException e) {
//
//      errorMessage = "Problem executing query "+validationQuery.getReference()+": ";
//      errorMessage += escapeHtml4("\n" + queryString + "\n" + e.getMessage());
//      log.error(errorMessage);
//      passed = false;
//
//    } catch (OutOfMemoryError e) {
//
//      errorMessage = "Problem executing query "+validationQuery.getReference()+", not enough memory.";
//      log.error(errorMessage);
//      passed = false;
//
//      // Free up variables
//      resultSet = null;
//      formattedResults = null;
//    }

    long executionTime = new Date().getTime() - start;
    return new ValidationQueryResult("ref", "desc", query, resultSet, formattedResults, passed, errorMessage, executionTime);
  }

  public Map<String, Long> numTriples() {
    HashMap<String, Long> result = new HashMap<>();
//    Iterator<String> graphNameIterator = getValidationDataset().listNames();
//    while(graphNameIterator.hasNext()) {
//      String graphName = graphNameIterator.next();
//      long size = getValidationDataset().getNamedModel(graphName).size();
//      result.put(graphName, size);
//    }
    return result;
  }



  public void update(String query, ValidationStepResult validationStepResult) {

    InferenceQueryResult result = (InferenceQueryResult) validationStepResult;

//    validationDataset = getValidationDataset();
//
    long start = new Date().getTime();
//    String queryString = query.getSparqlQuery(this);
//
//    try {
//
//      UpdateRequest request = new UpdateRequest();
//      request.add(queryString);
//      UpdateAction.execute(request, validationDataset);
//
//    } catch (QueryException e) {
//      throw new RuntimeException("There is a problem with this query: " + queryString, e);
//    }

    long executionTime = new Date().getTime() - start;
    result.addExecutionTime(executionTime);
  }

  @Override
  public void setContainerFile(ContainerFile container) {
    this.container = container;
  }

  @Override
  public void setContainerConfig(Object config) {
    if(config instanceof Container) {
      this.containerConfig = (Container) config;
      return;
    }
    throw new RuntimeException("The object is not an instance of a Container configuration");
  }

  @Override
  public void setConfigFile(Object configFile) {
    if(configFile instanceof ConfigFile) {
      this.configFile = (ConfigFile) configFile;
      return;
    }
    throw new RuntimeException("The object is not an instance of a ConfigFile ");
  }

}
