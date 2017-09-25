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


import com.sysunite.coinsweb.connector.ConnectorException;
import com.sysunite.coinsweb.graphset.ContainerGraphSet;
import com.sysunite.coinsweb.graphset.GraphVar;
import com.sysunite.coinsweb.graphset.QueryFactory;
import com.sysunite.coinsweb.parser.profile.factory.ProfileFactory;
import com.sysunite.coinsweb.parser.profile.pojo.Bundle;
import com.sysunite.coinsweb.parser.profile.pojo.ProfileFile;
import com.sysunite.coinsweb.parser.profile.pojo.Query;
import com.sysunite.coinsweb.report.ReportFactory;
import com.sysunite.coinsweb.steps.ProfileValidation;
import freemarker.template.Template;
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
  private Map<String, String> validationGraphs = new HashMap<>();
//  // Map context to list of inferenceCode
//  Map<String, List<String>> executedInferences = new HashMap();


  public ValidationExecutor(ProfileFile profile, ContainerGraphSet graphSet, ProfileValidation validationConfig) {
    this.profile = profile;
    this.graphSet = graphSet;
    this.validationConfig = validationConfig;

    if(profile.getQueryConfiguration() != null) {
      defaultPrefixes = profile.getQueryConfiguration().getDefaultPrefixes();
    }

    log.info("Using contextMap ("+graphSet.contextMap().keySet().size()+"):");
    for(GraphVar graphVar : graphSet.contextMap().keySet()) {
      String context = graphSet.contextMap().get(graphVar);
      log.info("- " + graphVar + " > "+context);

      validationGraphs.put(graphVar.toString(), '<'+context+'>');
    }
  }

  public void validate() {


    log.info("Execute profile");



    boolean valid = true;

    try {

//      // Load executed interferences
//      for(GraphVar graphVar : graphSet.contextMap().keySet()) {
//        String context = graphSet.contextMap().get(graphVar);
//        List<String> list = getFinishedInferences(context);
//        executedInferences.put(context, list);
//      }

      // Execute bundles in order of appearance
      for (Bundle bundle : profile.getBundles()) {

        long start = new Date().getTime();

        if (Bundle.INFERENCE.equals(bundle.getType())) {

          InferenceBundleStatistics enhanced = executeInferenceBundle(bundle);
          validationConfig.addBundle(enhanced);
          long executionTime = (new Date().getTime()) - start;
          enhanced.addExecutionTimeMs(executionTime);

        } else if (Bundle.VALIDATION.equals(bundle.getType())) {

          ValidationBundleStatistics enhanced = executeValidationBundle(bundle);
          validationConfig.addBundle(enhanced);

          valid &= enhanced.getValid();

          long executionTime = (new Date().getTime()) - start;
          enhanced.addExecutionTimeMs(executionTime);

        } else {
          throw new RuntimeException("Bundle type " + bundle.getType() + " not supported");
        }
      }
    } catch (ConnectorException e) {
      log.error("Executing bundle failed", e);

      validationConfig.setFailed(true);
      return;
    }

    validationConfig.setFailed(false);
    validationConfig.setValid(valid);
  }



  private InferenceBundleStatistics executeInferenceBundle(Bundle bundle) throws ConnectorException {


    InferenceBundleStatistics bundleStats = new InferenceBundleStatistics(bundle);


    String inferenceCode = ProfileFactory.inferenceCode(profile, bundle);

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


      for (int i = 0; i < bundleStats.getQueries().size(); i++) {

        long startQuery = new Date().getTime();
        Query query = bundleStats.getQueries().get(i);
        QueryStatistics queryStats = bundleStats.getQuery(query.getReference());

        String queryString = QueryFactory.buildQuery(query, validationGraphs, defaultPrefixes);
        queryStats.setExecutedQuery(queryString);
        try {
          graphSet.update(queryString);
        } catch (RuntimeException e) {
          throw new RuntimeException("Error during execution of update query "+query.getReference() +" of bundle "+bundle.getReference());
        }

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

    // Store that this inference bundle was executed
    if(Bundle.INFERENCE.equals(bundle.getType())) {

      Set<GraphVar> usedVars = QueryFactory.usedVars(bundle);
      graphSet.getConnector().storeFinishedInferences(graphSet.getCompositionFingerPrint(usedVars), usedVars, graphSet.contextMap(), inferenceCode);

    }

    // Push changes to any copy graphs
    graphSet.pushUpdatesToCompose();

    return bundleStats;
  }

  private ValidationBundleStatistics executeValidationBundle(Bundle bundle) throws ConnectorException {

    ValidationBundleStatistics bundleStats = new ValidationBundleStatistics(bundle);

    boolean valid = true;

    for (int i = 0; i < bundleStats.getQueries().size(); i++) {

      Query query = bundleStats.getQueries().get(i);
      QueryStatistics queryStats = bundleStats.getQuery(query.getReference());

      log.info("Execute \""+query.getReference()+"\":");

      int max = validationConfig.getMaxResults();

      String queryString = QueryFactory.buildQuery(query, validationGraphs, defaultPrefixes, max);
      queryStats.setExecutedQuery(queryString);

      long start = new Date().getTime();

      List<Object> result = graphSet.select(queryString, Integer.toUnsignedLong(max));


      LinkedList<Map<String, String>> results = new LinkedList<>();
      ArrayList<String> formattedResults = new ArrayList<>();

      boolean hasNoResults;

      if(result.isEmpty()) {
        hasNoResults = true;
        log.info("No results, which is good");

      } else {
        hasNoResults = false;
        log.info("Results found, this is bad");



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





  public List<String> getFinishedInferences(String context) throws ConnectorException {

    String query =

    "PREFIX val: <"+QueryFactory.VALIDATOR_NS+"> " +
    "SELECT ?inferenceCode ?fingerPrint " +
    "FROM NAMED <"+context+"> " +
    "WHERE { graph ?g { " +
    "  <"+context+"> val:bundle ?inferenceCode . " +
    "  <"+context+"> val:compositionFingerPrint ?fingerPrint . " +
    "}}";

    List<String> inferenceCodes = new ArrayList<>();
    List<Object> result = graphSet.select(query);
    for(Object bindingSet : result) {
      String inferenceCode = ((BindingSet)bindingSet).getBinding("inferenceCode").getValue().stringValue();
      String fingerPrint = ((BindingSet)bindingSet).getBinding("fingerPrint").getValue().stringValue();
      inferenceCodes.add(fingerPrint + "|" + inferenceCode);
    }
    return inferenceCodes;
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
}
