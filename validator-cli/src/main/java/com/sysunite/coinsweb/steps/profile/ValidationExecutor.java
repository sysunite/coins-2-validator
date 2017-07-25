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
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.BindingSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author Bastiaan Bijl
 */
public class ValidationExecutor {

  private static final Logger log = LoggerFactory.getLogger(ValidationExecutor.class);

  private ProfileFile profile;
  private ContainerGraphSet graphSet;
  private ProfileValidation validationConfig;

  private String defaultPrefixes = null;
  private Map<String, String> validationGraphs;
  Map<String, List<String>> executedInferences;        // map context to list of inferenceCode


  public ValidationExecutor(ProfileFile profile, ContainerGraphSet graphSet, ProfileValidation validationConfig) {
    this.profile = profile;
    this.graphSet = graphSet;
    this.validationConfig = validationConfig;

    if(profile.getQueryConfiguration() != null) {
      defaultPrefixes = profile.getQueryConfiguration().getDefaultPrefixes();
    }


    log.info("Using contextMap ("+graphSet.contextMap().keySet().size()+"):");
    validationGraphs = new HashMap<>();
    executedInferences = new HashMap();

    for(GraphVar graphVar : graphSet.contextMap().keySet()) {
      String context = graphSet.contextMap().get(graphVar);
      log.info("- " + graphVar + " > "+context);

      String originalContext = graphSet.graphExists(graphVar);
      if(originalContext == null) {
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


    // Execute bundles in order of appearance
    for(Bundle bundle : profile.getBundles()) {

      long start = new Date().getTime();

      if(Bundle.INFERENCE.equals(bundle.getType())) {

        InferenceBundleStatistics enhanced = executeInferenceBundle(bundle);
        validationConfig.addBundle(enhanced);
        long executionTime = (new Date().getTime()) - start;
        enhanced.addExecutionTimeMs(executionTime);

      } else if(Bundle.VALIDATION.equals(bundle.getType())) {

        ValidationBundleStatistics enhanced = executeValidationBundle(bundle);
        validationConfig.addBundle(enhanced);

        valid &= enhanced.getValid();

        long executionTime = (new Date().getTime()) - start;
        enhanced.addExecutionTimeMs(executionTime);

      } else {
        throw new RuntimeException("Bundle type "+bundle.getType()+" not supported");
      }
    }

    validationConfig.setFailed(false);
    validationConfig.setValid(valid);
  }



  private InferenceBundleStatistics executeInferenceBundle(Bundle bundle) {


    InferenceBundleStatistics bundleStats = new InferenceBundleStatistics(bundle);


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
          log.info("\u2728 Inference >>" + inferenceCode + "<< was executed before");
          bundleStats.setSkipped(true);
          return bundleStats;
        } else {
          throw new RuntimeException("Some other >>" + executedInferences.get(context) + "<< inference was executed before, this connector can not be used");
        }
      }

      log.info("Inference >>" + inferenceCode + "<< was not executed before");
    }


    long quadsAddedThisRunSum = 0l;
    Map<GraphVar, Long> previous = graphSet.quadCount();
    Map<GraphVar, Long> beforeBundle = previous;

    log.info("\uD83D\uDC1A Will perform bundle \""+bundle.getReference()+"\"");

    int run = 1;
    do {

      if(run > validationConfig.getMaxInferenceRuns()) {
        throw new RuntimeException("Break running, max number of repeated runs reached for bundle: "+bundle.getReference());
      }

      long start = new Date().getTime();


      for (Query query : bundleStats.getQueries()) {

        long startQuery = new Date().getTime();
        QueryStatistics queryStats = bundleStats.getQuery(query.getReference());

        String queryString = QueryFactory.buildQuery(query, validationGraphs, defaultPrefixes);
        queryStats.setExecutedQuery(queryString);
        graphSet.update(queryString);

        long executionTimeQuery = (new Date().getTime()) - startQuery;
        queryStats.addExecutionTimeMs(executionTimeQuery);

        // Do extra run outside executionTime
        if(validationConfig.getReportInferenceResults()) {

          List<Object> result = graphSet.select(QueryFactory.toSelectQuery(queryString));

          if(queryStats.getResultSet() == null) {
            queryStats.setResultSet(new LinkedList<>());
          }
          List<Map<String, String>> results = queryStats.getResultSet();

          if(!result.isEmpty()) {
            log.info("Reporting "+results.size()+" results of inference query \""+query.getReference()+"\"");

            for(Object bindingSet : result) {
              BindingSet resultRow = (BindingSet) bindingSet;
              HashMap<String, String> row = new HashMap<>();
              for(String binding : resultRow.getBindingNames()) {
                Value value = resultRow.getValue(binding);
                if(value == null) {
                  row.put(binding, "NULL");
                } else {
                  row.put(binding, value.stringValue());
                }
              }
              results.add(row);
            }
          }
        }

      }

      long executionTime = (new Date().getTime()) - start;


      Map<GraphVar, Long> current = graphSet.quadCount();

      Map<GraphVar, Long> quadsAddedThisRun = quadsAddedPerGraph(previous, current);
      bundleStats.addRunStatistics(quadsAddedThisRun);
      quadsAddedThisRunSum = quadsAdded(previous, current);
      previous = current;
      bundleStats.addQuadsAdded(quadsAddedThisRunSum);
      bundleStats.addExecutionTimeMs(executionTime);
      bundleStats.addRun();

      log.info("Finished run "+run+" for bundle \""+bundle.getReference()+"\", this total amount of quads was added this run: "+quadsAddedThisRun);


      run++;
    } while(quadsAddedThisRunSum > 0);

    // Store that this  inference bundle was executed
    if(Bundle.INFERENCE.equals(bundle.getType())) {
      Map<GraphVar, Long> bundleTotal = quadsAddedPerGraph(beforeBundle, previous);
      for(GraphVar graphVar : bundleTotal.keySet()) {
        if(bundleTotal.get(graphVar) > 0l) {
          storeFinishedInferences(graphVar, inferenceCode);
        }
      }
    }

    // Push changes to any copy graphs
    graphSet.pushUpdatesToCompose();

    return bundleStats;
  }

  private ValidationBundleStatistics executeValidationBundle(Bundle bundle) {

    ValidationBundleStatistics bundleStats = new ValidationBundleStatistics(bundle);

    boolean valid = true;

    for (Query query : bundleStats.getQueries()) {

      QueryStatistics queryStats = bundleStats.getQuery(query.getReference());

      String queryString = QueryFactory.buildQuery(query, validationGraphs, defaultPrefixes, validationConfig.getMaxResults());
      queryStats.setExecutedQuery(queryString);

      long start = new Date().getTime();

      List<Object> result = graphSet.select(queryString);


      LinkedList<Map<String, String>> results = new LinkedList<>();
      ArrayList<String> formattedResults = new ArrayList<>();

      boolean hasNoResults;

      if(result.isEmpty()) {
        hasNoResults = true;
        log.info("No results for \""+query.getReference()+"\", which is good");

      } else {
        hasNoResults = false;
        log.info("Results found for \""+query.getReference()+"\", this is bad");



        Template formatTemplate = query.getFormatTemplate();

        if(formatTemplate != null) {
          for(Object bindingSet : result) {
            BindingSet resultRow = (BindingSet) bindingSet;
            HashMap<String, String> row = new HashMap<>();
            for(String binding : resultRow.getBindingNames()) {
              Value value = resultRow.getValue(binding);
              if(value == null) {
                row.put(binding, "NULL");
              } else {
                row.put(binding, value.stringValue());
              }
            }
            results.add(row);
            formattedResults.add(ReportFactory.formatResult(resultRow, formatTemplate));
          }
        }
      }

      long executionTime = new Date().getTime() - start;





      queryStats.addExecutionTimeMs(executionTime);
      queryStats.setResultSet(results);
      queryStats.addFormattedResults(formattedResults);


      valid &= hasNoResults;

    }

    bundleStats.setValid(valid);
    return bundleStats;
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

  public void storeFinishedInferences(GraphVar graphVar, String inferenceCode) {

    String context = graphSet.contextMap().get(graphVar);

    log.info("Store inferenceCode >>"+inferenceCode+"<< for "+context);

    String query =

    "PREFIX val: <"+QueryFactory.VALIDATOR_NS+"> " +
    "INSERT DATA { GRAPH <"+context+"> { <"+context+"> val:bundle \""+inferenceCode+"\" . }}";

    graphSet.update(query);
  }




  public static Map<GraphVar, Long> quadsAddedPerGraph(Map<GraphVar, Long> previous, Map<GraphVar, Long> current) {

    HashMap<GraphVar, Long> result = new HashMap();
    if(current == null && previous == null) {
      return result;
    }

    if(current == null) {
      for(GraphVar graphVar : previous.keySet()) {
        result.put(graphVar, - previous.get(graphVar));
      }
    }

    if(previous == null) {
      return current;
    }

    for(GraphVar graphVar : current.keySet()) {
      if(previous.keySet().contains(graphVar)) {
        result.put(graphVar, current.get(graphVar) - previous.get(graphVar));
      }
    }
    for(GraphVar graphVar : previous.keySet()) {
      if(!current.keySet().contains(graphVar)) {
        result.put(graphVar, - previous.get(graphVar));
      }
    }

    return result;
  }

  public static long quadsAdded(Map<GraphVar, Long> previous, Map<GraphVar, Long> current) {
    long count = 0l;
    Map<GraphVar, Long> results = quadsAddedPerGraph(previous, current);
    for(GraphVar graphVar : results.keySet()) {
      count += Math.abs(results.get(graphVar)); // negative values also count as change
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
