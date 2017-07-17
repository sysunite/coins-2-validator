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
public class ValidationQueryResult extends QueryResult {

  private static final Logger log = LoggerFactory.getLogger(ValidationQueryResult.class);

  private Iterator<Map<String,String>> resultSet;
  @JacksonXmlCData
  private List<String> formattedResults = new ArrayList<>();
  private boolean passed;

  public ValidationQueryResult(Query queryConfig) {
    super(queryConfig);
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

  public boolean isValidationQuery() {
    return true;
  }
  public boolean isInferenceQuery() {
    return false;
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
