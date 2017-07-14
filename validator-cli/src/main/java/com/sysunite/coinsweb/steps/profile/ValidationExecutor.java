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
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

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

    log.info("Using contextMap: ");
    Map<String, String> validationGraphs = new HashMap<>();
    Set<String> executedInferences = new HashSet();
    for(String graphVar : graphSet.contextMap().keySet()) {

      String context = graphSet.contextMap().get(graphVar);
      String uploadDate = graphSet.graphExists(graphVar);
      if(uploadDate == null) {
        throw new RuntimeException("The graph "+graphVar+" ("+context+") is not available in the store");
      }
      log.info(graphVar + " > "+context);
      validationGraphs.put(graphVar, '<'+context+'>');
      executedInferences.addAll(getFinishedInferences(graphVar));
    }


    HashMap<String, HashMap<String, QueryResult>> bundleResults = new HashMap();

    // Execute bundles in order of appearance
    for(Bundle bundle : profile.getBundles()) {
      boolean containsUpdate = false;
      boolean someTripleWasAdded = false;

      String inferenceCode = profile.getName()+"/"+profile.getVersion()+"/"+bundle.getReference();
      if(Bundle.INFERENCE.equals(bundle.getType())) {
        if(executedInferences.contains(inferenceCode)) {
          log.info("Check if >>" + inferenceCode + "<< was executed before, it was");
          continue;
        } else {
          log.info("Check if >>" + inferenceCode + "<< was executed before, it was not");
        }
      }

      Map<String, Long> previous = graphSet.quadCount();
      Map<String, Long> beforeBundle = previous;
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

      // Store that this  inference bundle was executed
      if(Bundle.INFERENCE.equals(bundle.getType())) {
        Map<String, Long> bundleTotal = quadsAddedPerGraph(beforeBundle, previous);
        for(String graphName : bundleTotal.keySet()) {
          if(bundleTotal.get(graphName) > 0l) {
            storeFinishedInferences(graphName, inferenceCode);
          }
        }
      }

      bundleResults.put(bundle.getReference(), resultMap);
    }


    // Prepare data to transfer to the template
    if(valid) {
      log.info("\uD83E\uDD47 valid");
    } else {
      log.info("\uD83E\uDD48 invalid");
    }

    Map<String, Object> reportItems = new HashMap();

    reportItems.put("valid",         valid);
    reportItems.put("bundleResults", bundleResults);

    return reportItems;
  }


  public List<String> getFinishedInferences(String graphVar) {

    String context = graphSet.contextMap().get(graphVar);

    String query =

    "PREFIX val: <"+QueryFactory.VALIDATOR_NS+"> " +
    "SELECT ?inferenceCode " +
    "FROM NAMED <"+context+"> " +
    "WHERE { graph ?g { " +
    "  <"+context+"> val:bundle ?inferenceCode . " +
    "}}";

    List<String> inferenceCodes = new ArrayList<>();
    TupleQueryResult result = (TupleQueryResult) graphSet.select(query);
    while (result.hasNext()) {
      BindingSet row = result.next();
      String inferenceCode = row.getBinding("inferenceCode").getValue().stringValue();
      inferenceCodes.add(inferenceCode);
      log.info("The inference >>"+inferenceCode+"<< was previously executed for "+context);
    }
    return inferenceCodes;
  }

  public void storeFinishedInferences(String context, String inferenceCode) {

    log.info("Store inferenceCode >>"+inferenceCode+"<< for "+context);

    String query =

    "PREFIX val: <"+QueryFactory.VALIDATOR_NS+"> " +
    "INSERT DATA { GRAPH <"+context+"> { <"+context+"> val:bundle \""+inferenceCode+"\" . }}";

    graphSet.update(query);
  }




  public static Map<String, Long> quadsAddedPerGraph(Map<String, Long> previous, Map<String, Long> current) {

    HashMap<String, Long> result = new HashMap();
    if(current == null && previous == null) {
      return result;
    }

    if(current == null) {
      for(String graphName : previous.keySet()) {
        result.put(graphName, - previous.get(graphName));
      }
    }

    if(previous == null) {
      return current;
    }

    for(String graphName : current.keySet()) {
      if(Utils.containsNamespace(graphName, previous.keySet())) {
        result.put(graphName, current.get(graphName) - previous.get(graphName)); // todo: reading this graphName from previous might be scary
      }
    }
    for(String graphName : previous.keySet()) {
      if(!Utils.containsNamespace(graphName, current.keySet())) {
        result.put(graphName, - previous.get(graphName));
      }
    }

    return result;
  }

  public static long quadsAdded(Map<String, Long> previous, Map<String, Long> current) {
    long count = 0l;
    Map<String, Long> results = quadsAddedPerGraph(previous, current);
    for(String graphName : results.keySet()) {
      count += Math.abs(results.get(graphName)); // negative values also count as change
    }
    return count;
  }
}
