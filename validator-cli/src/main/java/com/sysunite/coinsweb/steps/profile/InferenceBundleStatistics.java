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
import com.sysunite.coinsweb.graphset.GraphVar;
import com.sysunite.coinsweb.parser.profile.pojo.Bundle;
import com.sysunite.coinsweb.parser.profile.pojo.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Bastiaan Bijl, Sysunite 2016
 */
@JsonInclude(Include.NON_NULL)
public class InferenceBundleStatistics extends Bundle {

  private static final Logger log = LoggerFactory.getLogger(InferenceBundleStatistics.class);

  @JsonIgnore
  private String id;
  private long executionTimeMs = 0l;
  @JsonIgnore
  private ArrayList<Query> queryList;
  private List<Map<GraphVar, Long>> runStatistics = new ArrayList<>();
  private int runs = 0;
  private long quadsAdded = 0l;
  @JsonInclude(Include.NON_NULL)
  private Boolean valid;

  @JsonInclude(Include.NON_NULL)
  private Boolean skipped;

  public InferenceBundleStatistics(Bundle bundleConfig) {

    if(!Bundle.INFERENCE.equals(bundleConfig.getType())) {
      throw new RuntimeException("Can not upgrade Bundle of other than type 'inference' to InferenceBundleStatistics");
    }

    // Copy fields
    setType(bundleConfig.getType());
    setReference(bundleConfig.getReference());
    setDescription(bundleConfig.getDescription());
    setQueries(bundleConfig.getQueries());

    // Make new fields
    this.id = Long.toHexString(Double.doubleToLongBits(Math.random()));
    this.queryList = bundleConfig.getQueries();
  }


  public String getId() {
    return id;
  }
  public long getExecutionTimeMs() {
    return executionTimeMs;
  }
  public List<Map<GraphVar, Long>> getRunStatistics() {
    return runStatistics;
  }
  public int getRuns() {
    return runs;
  }
  public long getQuadsAdded() {
    return quadsAdded;
  }
  public Boolean getSkipped() {
    return skipped;
  }

  @Override
  public ArrayList<Query> getQueries() {
    return queryList;
  }

  public void addExecutionTimeMs(long executionTimeMs) {
    this.executionTimeMs += executionTimeMs;
  }
  public void addRunStatistics(Map<GraphVar, Long> quadCount) {
    runStatistics.add(quadCount);
  }
  public void addRun() {
    this.runs += 1;
  }
  public void addQuadsAdded(long count) {
    this.quadsAdded += count;
  }
  public void setSkipped(Boolean skipped) {
    this.skipped = skipped;
    if(this.skipped != null && this.skipped) {
      this.queryList = new ArrayList<>();
    }
  }

  public void updateQuery(Query query) {

    for(int i = 0; i < queryList.size(); i++) {
      if(queryList.get(i).getReference().equals(query.getReference())) {
        queryList.remove(i);
        queryList.add(i, query);
        return;
      }
    }
    queryList.add(query);
  }
  public QueryStatistics getQuery(String reference) {
    for(Query query : queryList) {
      if(query.getReference().equals(reference)) {
        if (!(query instanceof QueryStatistics)) {
          updateQuery(new QueryStatistics(query));
          return getQuery(reference);
        }
        return (QueryStatistics) query;
      }
    }
    throw new RuntimeException("No Query registered with this reference code: " + reference);
  }

  public Boolean getValid() {
    return valid;
  }
  public void setValid(Boolean valid) {
    this.valid = valid;
  }
}
