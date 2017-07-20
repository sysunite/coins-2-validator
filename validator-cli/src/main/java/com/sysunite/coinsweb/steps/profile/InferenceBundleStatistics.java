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


import com.sysunite.coinsweb.graphset.GraphVar;
import com.sysunite.coinsweb.parser.profile.pojo.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Bastiaan Bijl, Sysunite 2016
 */
public class InferenceBundleStatistics {

  private static final Logger log = LoggerFactory.getLogger(InferenceBundleStatistics.class);

  private String id;
  private String reference;
  private String description;
  private long executionTime = 0l;
  private List<Map<GraphVar, Long>> runStatistics = new ArrayList<>();
  private int runs = 0;
  private long quadsAdded = 0l;

  public InferenceBundleStatistics(Bundle bundleConfig) {

    // Set passed attributes
    this.id = Long.toHexString(Double.doubleToLongBits(Math.random()));
    this.reference = bundleConfig.getReference();
    this.description = bundleConfig.getDescription();
  }


  public void addRun() {
    this.runs += 1;
  }
  public void addExecutionTime(long executionTime) {
    this.executionTime += executionTime;
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
  public long getExecutionTime() {
    return executionTime;
  }

  public void addRunStatistics(Map<GraphVar, Long> quadCount) {
    runStatistics.add(quadCount);
  }
  public List<Map<GraphVar, Long>> getRunStatistics() {
    return runStatistics;
  }
  public int getRuns() {
    return runs;
  }

  public void addQuadsAdded(long count) {
    this.quadsAdded += count;
  }
  public long getQuadsAdded() {
    return quadsAdded;
  }
}
