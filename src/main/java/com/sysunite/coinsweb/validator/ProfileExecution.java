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
package com.sysunite.coinsweb.validator;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Bastiaan Bijl, Sysunite 2016
 */
public class ProfileExecution {

  private static final Logger log = LoggerFactory.getLogger(ProfileExecution.class);



  private boolean profileChecksPassed = false;
  private boolean validationPassed = false;
  private long executionTime;
  private long memLimit;
  private long memMaxUsage = 0l;


  private List<ValidationQueryResult> profileChecks = new ArrayList<>();
  private List<ValidationQueryResult> validationRules = new ArrayList<>();

  private InferenceExecution schemaInferences = new InferenceExecution();
  private InferenceExecution dataInferences = new InferenceExecution();

  public ProfileExecution() {

    // Read the jvms max memory limit
    Runtime runtime = Runtime.getRuntime();
    memLimit = runtime.maxMemory();
  }

  public void setProfileChecksPassed(boolean passed) {
    this.profileChecksPassed = passed;
  }
  public boolean profileChecksPassed() {
    return this.profileChecksPassed;
  }
  public void setValidationPassed(boolean passed) {
    this.validationPassed = passed;
  }
  public boolean validationPassed() {
    return this.validationPassed;
  }
  public void setExecutionTime(long executionTime) {
    this.executionTime = executionTime;
  }
  public long getExecutionTime() {
    return this.executionTime;
  }
  public long getMemLimit() {
    return this.memLimit;
  }
  public long getMemMaxUsage() {
    return memMaxUsage;
  }
  public void updateMemMaxUsage(long sample) {
    if(sample > this.memMaxUsage) {
      this.memMaxUsage = sample;
    }
  }

  public void addProfileCheckResult(ValidationQueryResult queryResult) {
    profileChecks.add(queryResult);
  }
  public List<ValidationQueryResult> getProfileCheckResults() {
    return profileChecks;
  }

  public InferenceExecution getSchemaInferenceResults() {
    return schemaInferences;
  }

  public InferenceExecution getDataInferenceResults() {
    return dataInferences;
  }

  public void addValidationRuleResult(ValidationQueryResult queryResult) {
    validationRules.add(queryResult);
  }
  public List<ValidationQueryResult> getValidationRuleResults() {
    return validationRules;
  }
}