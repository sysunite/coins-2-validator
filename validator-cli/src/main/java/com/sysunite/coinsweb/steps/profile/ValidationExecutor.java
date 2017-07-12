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


import com.sysunite.coinsweb.graphset.ContainerGraphSet;
import com.sysunite.coinsweb.graphset.QueryFactory;
import com.sysunite.coinsweb.parser.profile.pojo.Bundle;
import com.sysunite.coinsweb.parser.profile.pojo.ProfileFile;
import com.sysunite.coinsweb.parser.profile.pojo.Query;
import com.sysunite.coinsweb.rdfutil.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Bastiaan Bijl
 */
public class ValidationExecutor {

  private static final Logger log = LoggerFactory.getLogger(ValidationExecutor.class);

  private static final int MAX_RUNS = 50;

  private ProfileFile profile;
  private ContainerGraphSet graphSet;

  public ValidationExecutor(ProfileFile profile, ContainerGraphSet graphSet) {
   this.profile = profile;
   this.graphSet = graphSet;
  }

  public Map<String, Object> validate() {


    log.info("Execute profile");


    long start = new Date().getTime();
    boolean valid = true;


    String defaultPrefixes = null;
    if(profile.getQueryConfiguration() != null) {
      defaultPrefixes = profile.getQueryConfiguration().cleanDefaultPrefixes();
    }


    // Assumption is that there are certain graphs available, check this
    Map<String, String> validationGraphs = new HashMap<>();
    for(String code : graphSet.contextMap().keySet()) {
      validationGraphs.put(code, '<'+graphSet.contextMap().get(code)+'>');
    }

    for(String code : validationGraphs.keySet()) {
      String graphName = validationGraphs.get(code);
      if(!graphSet.select("SELECT * WHERE { GRAPH "+graphName+" {?s ?p ?o}} LIMIT 1", null, null)) {
//        throw new RuntimeException("Store not loaded properly, this context is empty: "+graphName);
      }
    }

//    Runtime runtime = Runtime.getRuntime();

    HashMap<String, HashMap<String, QueryResult>> bundleResults = new HashMap();

    // Execute bundles in order of appearance
    for(Bundle bundle : profile.getBundles()) {
      boolean containsUpdate = false;
      boolean someTripleWasAdded = false;

      Map<String, Long> previous = graphSet.quadCount();
      HashMap<String, QueryResult> resultMap = new HashMap();

      log.info("\uD83D\uDC1A Will perform bundle \""+bundle.getReference()+"\"");

      int run = 1;
      do {
        someTripleWasAdded = false;
        if(run > MAX_RUNS) {
          throw new RuntimeException("Break running, max number of repeated runs reached for bundle: "+bundle.getReference());
        }

        for (Query query : bundle.getQueries()) {

          QueryResult resultCarrier;
          if(!resultMap.containsKey(query.getReference())) {
            if(Bundle.INFERENCE.equals(bundle.getType())) {

              resultCarrier = new InferenceQueryResult(query);
            } else if(Bundle.VALIDATION.equals(bundle.getType())) {

              resultCarrier = new ValidationQueryResult(query);
            } else {
              throw new RuntimeException("No supported bundle type.");
            }
            resultMap.put(query.getReference(), resultCarrier);
          } else {
            resultCarrier = resultMap.get(query.getReference());
          }


          if (resultCarrier instanceof InferenceQueryResult) {
            containsUpdate = true;

            String queryString = QueryFactory.buildQuery(query, validationGraphs, defaultPrefixes);
            graphSet.update(queryString, resultCarrier);

            Map<String, Long> current = graphSet.quadCount();
            resultCarrier.addRunStatistics(current);
            long quadsAdded = quadsAdded(previous, current);
            previous = current;
            ((InferenceQueryResult)resultCarrier).addQuadsAdded(quadsAdded);

            log.info("Finished run "+resultCarrier.getRunStatistics().size()+" for query \""+query.getReference()+"\", this total amount of quads was added: "+quadsAdded);
            if(quadsAdded > 0) {
              someTripleWasAdded = true;
            }


          }
          if (resultCarrier instanceof ValidationQueryResult) {

            String queryString = QueryFactory.buildQuery(query, validationGraphs, defaultPrefixes);
            graphSet.select(queryString, query.getFormatTemplate(), resultCarrier);
            boolean hasNoResults = ((ValidationQueryResult)resultCarrier).getFormattedResults().isEmpty();
            ((ValidationQueryResult)resultCarrier).setPassed(hasNoResults);

            valid &= hasNoResults;

          }

        }

        run++;
      } while(containsUpdate && someTripleWasAdded);

      bundleResults.put(bundle.getReference(), resultMap);
    }


    // Prepare data to transfer to the template
    Map<String, Object> reportItems = new HashMap();

    reportItems.put("valid",         valid);
    reportItems.put("bundleResults", bundleResults);

    return reportItems;
  }


  public static long quadsAdded(Map<String, Long> previous, Map<String, Long> current) {
    if(current == null || current.isEmpty()) {
      return 0l;
    }
    if(previous == null || previous.isEmpty()) {
      long count = 0l;
      for(Long graphCount : current.values()) {
        count += graphCount;
      }
      return count;
    }

    long count = 0l;
    for(String graphName : current.keySet()) {
      if(!Utils.containsNamespace(graphName, previous.keySet())) {
        count += current.get(graphName);
      } else {
        count += (current.get(graphName) - previous.get(graphName));
      }
    }
    return count;
  }
}
