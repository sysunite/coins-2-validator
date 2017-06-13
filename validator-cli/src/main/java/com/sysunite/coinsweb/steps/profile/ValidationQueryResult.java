/**
 * MIT License
 *
 * Copyright (c) 2016 Bouw Informatie Raad
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 *
 **/
package com.sysunite.coinsweb.steps.profile;


import com.sysunite.coinsweb.parser.profile.Query;
import com.sysunite.coinsweb.steps.ValidationStepResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author Bastiaan Bijl, Sysunite 2016
 */
public class ValidationQueryResult implements ValidationStepResult {

  private static final Logger log = LoggerFactory.getLogger(ValidationQueryResult.class);

  private String id;
  private String reference;
  private String description;
  private String sparqlQuery;
  private Iterator<Map<String,String>> resultSet;
  private List<String> formattedResults;
  private boolean passed;
  private String errorMessage;
  private long executionTime;
  private ArrayList<Map<String, Long>> runStatistics = new ArrayList<>();

  public ValidationQueryResult(Query queryConfig) {

    // Set passed attributes
    this.id = Long.toHexString(Double.doubleToLongBits(Math.random()));
    this.reference = queryConfig.getReference();
    this.description = queryConfig.getDescription();
    this.passed = passed;
    this.errorMessage = errorMessage;
    this.executionTime = executionTime;
//    this.triplesAdded = new HashMap<>();
  }

  public void setExecutedQuery(String sparqlQuery) {
    this.sparqlQuery = sparqlQuery;
  }
  public void setResultSet(Iterator<Map<String,String>> resultSet) {
    this.resultSet = resultSet;
  }
  public void setFormattedResults(List<String> formattedResults) {
    this.formattedResults = formattedResults;
  }


  public String getId() {
    return id;
  }
  public String getReference() {
    return reference;
  }
  public String getDescription() {
    return description;
  }
  public String getSparqlQuery() {
    return sparqlQuery;
  }
  public Iterator<Map<String,String>> getResultSet() {
    return resultSet;
  }
  public List<String> getFormattedResults() {
    return formattedResults;
  }
  public boolean getPassed() {
    return passed;
  }
  public String getErrorMessage() {
    return errorMessage;
  }
  public long getExecutionTime() {
    return executionTime;
  }

  public void addRunStatistics(Map<String, Long> quadCount) {
    runStatistics.add(quadCount);
    log.info("Finished run "+runStatistics.size()+" for query \""+reference+"\", this total amount of quads was added: "+quadsAddedLastRun());
  }
  public long quadsAddedLastRun() {
    if(runStatistics.isEmpty()) {
      return 0l;
    }
    if(runStatistics.size() == 1) {
      long count = 0l;
      for(Long graphCount : runStatistics.get(0).values()) {
        count += graphCount;
      }
      return count;
    }

    Map<String, Long> previous = runStatistics.get(runStatistics.size()-2);
    Map<String, Long> current = runStatistics.get(runStatistics.size()-1);
    long count = 0l;
    for(String graphName : current.keySet()) {
      if(!previous.containsKey(graphName)) {
        count += current.get(graphName);
      } else {
        count += (current.get(graphName) - previous.get(graphName));
      }
    }
    return count;
  }


}