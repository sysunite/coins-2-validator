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
import com.sysunite.coinsweb.parser.profile.pojo.Bundle;
import com.sysunite.coinsweb.parser.profile.pojo.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author Bastiaan Bijl, Sysunite 2016
 */
@JsonInclude(Include.NON_NULL)
public class ValidationBundleStatistics extends Bundle {

  private static final Logger log = LoggerFactory.getLogger(ValidationBundleStatistics.class);

  @JsonIgnore
  private String id;
  private long executionTimeMs = 0l;
  @JsonIgnore
  private HashMap<String, Query> queryMap;
  private Boolean valid;

  public ValidationBundleStatistics(Bundle bundleConfig) {

    if(!Bundle.VALIDATION.equals(bundleConfig.getType())) {
      throw new RuntimeException("Can not upgrade Bundle of other than type 'validation' to ValidationBundleStatistics");
    }

    // Copy fields
    setType(bundleConfig.getType());
    setReference(bundleConfig.getReference());
    setDescription(bundleConfig.getDescription());
    setQueries(bundleConfig.getQueries());



    // Make new fields
    this.id = Long.toHexString(Double.doubleToLongBits(Math.random()));
    this.queryMap = new HashMap();
    for(Query query : bundleConfig.getQueries()) {
      this.queryMap.put(query.getReference(), query);
    }
  }



  public String getId() {
    return id;
  }
  public long getExecutionTimeMs() {
    return executionTimeMs;
  }

  @Override
  public ArrayList<Query> getQueries() {
    ArrayList<Query> queries = new ArrayList();
    queries.addAll(queryMap.values());
    return queries;
  }


  public void addExecutionTimeMs(long executionTimeMs) {
    this.executionTimeMs += executionTimeMs;
  }
  public void updateQuery(Query query) {
    this.queryMap.put(query.getReference(), query);
  }
  public QueryStatistics getQuery(String reference) {
    Query current = this.queryMap.get(reference);
    if(current == null) {
      throw new RuntimeException("No Query registered with this reference code: "+reference);
    }
    if(!(current instanceof QueryStatistics)) {
      updateQuery(new QueryStatistics(current));
    }
    return (QueryStatistics) this.queryMap.get(reference);
  }



  public boolean getValid() {
    return valid;
  }
  public void setValid(boolean valid) {
    this.valid = valid;
  }

}
