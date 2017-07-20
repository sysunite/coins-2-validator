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


import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlCData;
import com.sysunite.coinsweb.parser.profile.pojo.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author Bastiaan Bijl, Sysunite 2016
 */
public class ValidationQueryResult {


  private static final Logger log = LoggerFactory.getLogger(ValidationQueryResult.class);

  private String id;
  private String reference;
  private String description;
  @JacksonXmlCData
  private String sparqlQuery;
  private long executionTime;
  private List<Map<String, Long>> runStatistics = new ArrayList<>();


  private Iterator<Map<String,String>> resultSet;
  @JacksonXmlCData
  private List<String> formattedResults = new ArrayList<>();
  private boolean passed;

  public ValidationQueryResult(Query queryConfig) {

    // Set passed attributes
    this.id = Long.toHexString(Double.doubleToLongBits(Math.random()));
    this.reference = queryConfig.getReference();
    this.description = queryConfig.getDescription();
  }

  public void setExecutionTime(long executionTime) {
    this.executionTime = executionTime;
  }
  public void setExecutedQuery(String sparqlQuery) {
    this.sparqlQuery = sparqlQuery;
  }


  public boolean isValidationQuery() {
    return true;
  }
  public boolean isInferenceQuery() {
    return false;
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
  public long getExecutionTime() {
    return executionTime;
  }

  public void addRunStatistics(Map<String, Long> quadCount) {
    runStatistics.add(quadCount);
  }
  public List<Map<String, Long>> getRunStatistics() {
    return runStatistics;
  }



  public void setResultSet(Iterator<Map<String,String>> resultSet) {
    this.resultSet = resultSet;
  }
  public void addFormattedResults(List<String> formattedResults) {
    this.formattedResults.addAll(formattedResults);
  }
  public void setPassed(boolean passed) {
    this.passed = passed;
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




}
