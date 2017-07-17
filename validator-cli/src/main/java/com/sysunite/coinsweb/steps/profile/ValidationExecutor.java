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


import com.sysunite.coinsweb.connector.Connector;
import com.sysunite.coinsweb.graphset.ContainerGraphSet;
import com.sysunite.coinsweb.graphset.GraphVar;
import com.sysunite.coinsweb.graphset.QueryFactory;
import com.sysunite.coinsweb.parser.profile.pojo.Bundle;
import com.sysunite.coinsweb.parser.profile.pojo.ProfileFile;
import com.sysunite.coinsweb.parser.profile.pojo.Query;
import com.sysunite.coinsweb.rdfutil.Utils;
import com.sysunite.coinsweb.report.ReportFactory;
import com.sysunite.coinsweb.steps.ProfileValidation;
import freemarker.template.Template;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.rdf4j.query.BindingSet;
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
  private ProfileValidation validationConfig;
  private Connector connector;

  private String defaultPrefixes = null;
  private Map<String, String> validationGraphs;
  Map<String, List<String>> executedInferences;        // map context to list of inferenceCode


  public ValidationExecutor(ProfileFile profile, ContainerGraphSet graphSet, ProfileValidation validationConfig, Connector connector) {
    this.profile = profile;
    this.graphSet = graphSet;
    this.validationConfig = validationConfig;
    this.connector = connector;

    if(profile.getQueryConfiguration() != null) {
      defaultPrefixes = profile.getQueryConfiguration().cleanDefaultPrefixes();
    }


    log.info("Using contextMap ("+graphSet.contextMap().keySet().size()+"):");
    validationGraphs = new HashMap<>();
    executedInferences = new HashMap();

    for(GraphVar graphVar : graphSet.contextMap().keySet()) {
      String context = graphSet.contextMap().get(graphVar);
      log.info("- " + graphVar + " > "+context);

      String uploadDate = graphSet.graphExists(graphVar);
      if(uploadDate == null) {
        throw new RuntimeException("The graph "+graphVar+" ("+context+") is not available in the store");
      }

      validationGraphs.put(graphVar.toString(), '<'+context+'>');
      List<String> list = getFinishedInferences(context);
      executedInferences.put(context, list);
    }
  }

  public void validate() {


    log.info("Execute profile");



    boolean valid = true;


    HashMap<String, HashMap<String, Object>> bundleResults = new HashMap();

    // Execute bundles in order of appearance
    for(Bundle bundle : profile.getBundles()) {

      if(Bundle.INFERENCE.equals(bundle.getType())) {

        HashMap<String, Object> resultMap = executeInferenceBundle(bundle);
        bundleResults.put(bundle.getReference(), resultMap);

      } else if(Bundle.VALIDATION.equals(bundle.getType())) {

        HashMap<String, Object> resultMap = executeValidationBundle(bundle);
        bundleResults.put(bundle.getReference(), resultMap);

      } else {
        throw new RuntimeException("Bundle type "+bundle.getType()+" not supported");
      }


      valid &= bundle.getValid();

    }

    validationConfig.setFailed(false);
    validationConfig.setValid(valid);
    validationConfig.setBundleResults(bundleResults);
  }



  private HashMap<String, Object> executeInferenceBundle(Bundle bundle) {

    boolean containsUpdate = false;
    boolean someTripleWasAdded = false;
    HashMap<String, Object> resultMap = new HashMap();

    String inferenceCode = profile.getName()+"/"+profile.getVersion()+"/"+bundle.getReference();

    log.info("Will check all for these graphs if some inference was executed before");
    for(GraphVar graphVar : QueryFactory.usedVars(bundle)) {
      String context = graphSet.contextMap().get(graphVar);
      List<String> list = new ArrayList<>();
      if(Utils.containsNamespace(context, executedInferences.keySet())) {
        list = executedInferences.get(context);
      }
      log.info("- "+graphVar+" > "+context+" has >>"+String.join("<<, >>", list)+"<<");
      for(String existingInferenceCode : list) {
        if(inferenceCodeWithoutRef(existingInferenceCode).equals(inferenceCodeWithoutRef(inferenceCode))) {
          log.info("Inference >>" + inferenceCode + "<< was executed before");
          return resultMap;
        } else {
          throw new RuntimeException("Some other >>" + executedInferences.get(context) + "<< inference was executed before, this connector can not be used");
        }
      }

      log.info("Inference >>" + inferenceCode + "<< was not executed before");
    }


    Map<String, Long> previous = graphSet.quadCount();
    Map<String, Long> beforeBundle = previous;

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

          resultCarrier = new InferenceQueryResult(query);
          resultMap.put(query.getReference(), resultCarrier);

        } else {

          resultCarrier = (InferenceQueryResult) resultMap.get(query.getReference());

        }


        containsUpdate = true;

        String queryString = QueryFactory.buildQuery(query, validationGraphs, defaultPrefixes);

        if(graphSet.requiresLoad()) {
          graphSet.load();
        }

        long start = new Date().getTime();

        connector.update(queryString);

        long executionTime = new Date().getTime() - start;
        resultCarrier.setExecutionTime(executionTime);
        resultCarrier.setExecutedQuery(queryString);



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

    return resultMap;
  }

  private HashMap<String, Object> executeValidationBundle(Bundle bundle) {

    boolean valid = true;
    HashMap<String, Object> resultMap = new HashMap();

    for (Query query : bundle.getQueries()) {

      ValidationQueryResult resultCarrier;
      if(!resultMap.containsKey(query.getReference())) {
        resultCarrier = new ValidationQueryResult(query);
        resultMap.put(query.getReference(), resultCarrier);
      } else {
        resultCarrier = (ValidationQueryResult)resultMap.get(query.getReference());
      }



      String queryString = QueryFactory.buildQuery(query, validationGraphs, defaultPrefixes, validationConfig.getMaxResults());

      Template formatTemplate = query.getFormatTemplate();




      if(graphSet.requiresLoad()) {
        graphSet.load();
      }

      long startQuery = new Date().getTime();

      ArrayList<String> formattedResults = new ArrayList<>();
      List<Object> result = connector.query(queryString);

      if(result.isEmpty()) {
        log.info("No results for \""+query.getReference()+"\", which is good");
      } else {
        log.info("Results found for \""+query.getReference()+"\", this is bad");


        if(formatTemplate != null) {
          for(Object bindingSet : result) {
            formattedResults.add(ReportFactory.formatResult((BindingSet) bindingSet, formatTemplate));
          }
        }
      }

      long executionTime = new Date().getTime() - startQuery;

      resultCarrier.setExecutionTime(executionTime);
      resultCarrier.setExecutedQuery(queryString);
      resultCarrier.addFormattedResults(formattedResults);





      boolean hasNoResults = resultCarrier.getFormattedResults().isEmpty();
      resultCarrier.setPassed(hasNoResults);

      valid &= hasNoResults;

    }
    bundle.setValid(valid);

    return resultMap;
  }





  public List<String> getFinishedInferences(String context) {



    String query =

    "PREFIX val: <"+QueryFactory.VALIDATOR_NS+"> " +
    "SELECT ?inferenceCode " +
    "FROM NAMED <"+context+"> " +
    "WHERE { graph ?g { " +
    "  <"+context+"> val:bundle ?inferenceCode . " +
    "}}";

    List<String> inferenceCodes = new ArrayList<>();
    List<Object> result = graphSet.select(query);
    for(Object bindingSet : result) {
      String inferenceCode = ((BindingSet)bindingSet).getBinding("inferenceCode").getValue().stringValue();
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
        result.put(graphName, current.get(graphName) - previous.get(graphName));
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

  public static String inferenceCodeWithoutRef(String inferenceCode) {
    if(inferenceCode == null || inferenceCode.isEmpty() || StringUtils.countMatches(inferenceCode, "/") != 2) {
      throw new RuntimeException("InferenceCode >>"+inferenceCode+"<< is not valid");
    }
    return inferenceCode.substring(0, inferenceCode.lastIndexOf("/"));
  }
}
