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


import com.sysunite.coinsweb.parser.profile.pojo.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Bastiaan Bijl, Sysunite 2016
 */
public class InferenceQueryResult extends QueryResult {

  private static final Logger log = LoggerFactory.getLogger(InferenceQueryResult.class);

  public InferenceQueryResult(Query queryConfig) {
    super(queryConfig);
  }
  private long quadsAdded = 0l;

  public void addQuadsAdded(long count) {
    this.quadsAdded += count;
  }

  public boolean isValidationQuery() {
    return false;
  }
  public boolean isInferenceQuery() {
    return true;
  }


  public long getQuadsAdded() {
    return quadsAdded;
  }
}
