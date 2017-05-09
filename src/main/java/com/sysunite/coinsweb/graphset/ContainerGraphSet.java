package com.sysunite.coinsweb.graphset;

import com.sysunite.coinsweb.connector.Connector;
import com.sysunite.coinsweb.parser.profile.Step;
import com.sysunite.coinsweb.validator.InferenceQueryResult;
import com.sysunite.coinsweb.validator.ValidationQueryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author bastbijl, Sysunite 2017
 */
public class ContainerGraphSet {

  private static final Logger log = LoggerFactory.getLogger(ContainerGraphSet.class);

  private boolean disabled;
  private Connector connector;

  public ContainerGraphSet() {
    this.disabled = true;
  }

  public ContainerGraphSet(Connector connector) {
    this.disabled = false;
    this.connector = connector;
  }

  public ValidationQueryResult select(Step step) {
    String errorMessage = null;
    boolean passed = false;
    long start = new Date().getTime();
    String queryString = step.buildQuery();
    Iterator<Map<String, String>> resultSet = null;
    ArrayList<String> formattedResults = new ArrayList<>();

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
    return new ValidationQueryResult(step.getReference(), step.getDescription(), queryString, resultSet, formattedResults, passed, errorMessage, executionTime);
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

  public static Map<String, Long> diffNumTriples(Map<String, Long> oldValues, Map<String, Long> newValues) {
    HashMap<String, Long> result = new HashMap<>();

    Iterator<String> graphNameIterator = newValues.keySet().iterator();
    while(graphNameIterator.hasNext()) {
      String graphName = graphNameIterator.next();

      Long oldValue;
      if(oldValues.containsKey(graphName)) {
        oldValue = oldValues.get(graphName);
      } else {
        oldValue = 0l;
      }
      Long newValue = newValues.get(graphName);
      result.put(graphName, newValue - oldValue);
    }
    return result;
  }

  public void insert(Step query, InferenceQueryResult result) {

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
}
