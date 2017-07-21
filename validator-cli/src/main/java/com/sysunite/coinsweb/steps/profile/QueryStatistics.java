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


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.sysunite.coinsweb.parser.profile.pojo.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Bastiaan Bijl, Sysunite 2016
 */
public class QueryStatistics extends Query {

  private static final Logger log = LoggerFactory.getLogger(QueryStatistics.class);

  private long executionTimeMs;
  @JsonInclude(Include.NON_NULL)
  private Map<String,String> resultSet;
  @JsonIgnore
  private List<String> formattedResults = new ArrayList<>();
  private String executedQuery;




  public QueryStatistics(Query queryConfig) {

    // Copy fields
    setReference(queryConfig.getReference());
    setDescription(queryConfig.getDescription());
    setQuery(queryConfig.getQuery());
    setResultFormat(queryConfig.getResultFormat());
  }


  @Override
  @JsonIgnore
  public String getResultFormat() {
    return super.getResultFormat();
  }
  @Override
  public String getQuery() {
    return executedQuery;
  }

  public long getExecutionTimeMs() {
    return executionTimeMs;
  }
  public Map<String,String> getResultSet() {
    return resultSet;
  }
  public List<String> getFormattedResults() {
    return formattedResults;
  }



  public void setExecutedQuery(String executedQuery) {
    this.executedQuery = executedQuery;
  }
  public void addExecutionTimeMs(long executionTimeMs) {
    this.executionTimeMs += executionTimeMs;
  }
  public void setResultSet(Map<String, String> resultSet) {
    this.resultSet = resultSet;
  }
  public void addFormattedResults(List<String> formattedResults) {
    this.formattedResults.addAll(formattedResults);
  }

}
