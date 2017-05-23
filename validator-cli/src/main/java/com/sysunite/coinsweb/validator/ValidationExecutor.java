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


import com.sysunite.coinsweb.graphset.ContainerGraphSet;
import com.sysunite.coinsweb.graphset.GraphSetFactory;
import com.sysunite.coinsweb.parser.profile.ProfileFile;
import com.sysunite.coinsweb.parser.profile.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Bastiaan Bijl
 */
public class ValidationExecutor {

  private static final Logger log = LoggerFactory.getLogger(ValidationExecutor.class);

  private ProfileFile profile;
  private ContainerGraphSet graphSet;

  public ValidationExecutor(ProfileFile profile, ContainerGraphSet graphSet) {
   this.profile = profile;
   this.graphSet = graphSet;
  }

  public Map<String, Object> validate() {


    log.info("Execute profile.");
    ProfileExecution execution = new ProfileExecution();

    long start = new Date().getTime();




    Runtime runtime = Runtime.getRuntime();

    log.info("\uD83D\uDC1A Will perform profile checks.");
//    boolean profileChecks = executeQueries(profile.getRequirements(), execution.getProfileCheckResults());
    execution.updateMemMaxUsage(runtime.totalMemory());

    log.info("\uD83D\uDC1A Will add schema inferences.");
//    addInferences(profile.getSchemaInferences(), execution.getSchemaInferenceResults());
    execution.updateMemMaxUsage(runtime.totalMemory());

    log.info("\uD83D\uDC1A Will add data inferences.");
//    addInferences(profile.getDataInferences(), execution.getDataInferenceResults());
    execution.updateMemMaxUsage(runtime.totalMemory());

    log.info("\uD83D\uDC1A Will perform validation checks.");
//    boolean validationRules = executeQueries(profile.getRules(), execution.getValidationRuleResults());
    execution.updateMemMaxUsage(runtime.totalMemory());

//    execution.setProfileChecksPassed(profileChecks);
//    execution.setValidationPassed(validationRules);
    execution.setExecutionTime(new Date().getTime() - start);





    log.info("Build report.");



    boolean valid = true;

    // Prepare data to transfer to the template
    Map<String, Object> reportItems = new HashMap();


    reportItems.put("valid",      valid);
//    data.put("filename", model.getCoinsContainer().getFileName());
//    data.put("libraries", libraries);
//    data.put("online", online);
//    data.put("imports", imports);
//    data.put("graphs", graphs);
//    data.put("attachments", model.getCoinsContainer().getAttachments());
//    data.put("date", new Date().toString());
//    data.put("executionTime", execution.getExecutionTime());
//    data.put("memLimit", execution.getMemLimit());
//    data.put("memMaxUsage", execution.getMemMaxUsage());
//    data.put("graphSetImpl", connector.getClass().getCanonicalName());
//    data.put("profileName", this.profile.getName());
//    data.put("profileVersion", this.profile.getVersion());
//    data.put("profileChecksPassed", execution.profileChecksPassed());
//    data.put("fileStructureSanity", fileStructureSanity);
//    data.put("fileStructureMessage", fileStructureMessage);
//    data.put("allImportsAvailable", allImportsAvailable);
//    data.put("validationPassed", execution.validationPassed());
//    data.put("profileChecks", execution.getProfileCheckResults());
//    data.put("schemaInferences", execution.getSchemaInferenceResults());
//    data.put("dataInferences", execution.getDataInferenceResults());
//    data.put("validationRules", execution.getValidationRuleResults());

    return reportItems;
  }







  private boolean executeQueries(List<Query> queries, List<ValidationQueryResult> resultCollection) {

    boolean allChecksPassed = true;
    for(Query query : queries) {

      ValidationQueryResult result = (ValidationQueryResult)graphSet.select(query);
      allChecksPassed &= result.getPassed();
      resultCollection.add(result);
    }

    return allChecksPassed;
  }

  private void addInferences(List<Query> queries, InferenceExecution inferenceExecution) {
    addInferences(queries, inferenceExecution, true);
  }
  private void addInferences(List<Query> queries, final InferenceExecution inferenceExecution, boolean recursive) {

    // Build a map of all results in resultList
    HashMap<String, InferenceQueryResult> resultByReference = new HashMap<>();

    long triplesAddedThisRun = 0l;
    long start = new Date().getTime();

    int run = 1;

    do {

      for (Query step : queries) {


        // Prepare a resultCarrier
        if(!resultByReference.containsKey(step.getReference())) {
          InferenceQueryResult resultCarrier = new InferenceQueryResult(step.getReference(), step.getDescription(), step.cleanQuery(), null);
          resultByReference.put(step.getReference(), resultCarrier);
        }
        InferenceQueryResult resultCarrier = resultByReference.get(step.getReference());

        Map<String, Long> before = graphSet.numTriples();
        graphSet.insert(step, resultCarrier);
        Map<String, Long> diff = GraphSetFactory.diffNumTriples(before, graphSet.numTriples());

        inferenceExecution.addTriplesAdded(Integer.toString(run), step.getReference(), diff);

      }


//      String lastRunNr = Integer.toString(inferenceExecution.getNumRuns());
//      Map<String, Long> statistics = inferenceExecution.getTriplesAdded(lastRunNr);
//      if(statistics.containsKey(connector.getFullUnionNamespace())) {
//        triplesAddedThisRun = statistics.get(connector.getFullUnionNamespace());
//        log.info("This round " + triplesAddedThisRun + " triples were added.");
//      } else {
//        triplesAddedThisRun = 0;
//        log.warn("This round no triples were added to the full union graph, which might have unexpected reasons.");
//      }

      run++;

    // Loop
    } while (recursive && triplesAddedThisRun > 0l);

    // Store all resultCarriers in the allocated list
    long executionTime = new Date().getTime() - start;
    inferenceExecution.setExecutionTime(executionTime);
    inferenceExecution.getQueryResults().addAll(resultByReference.values());
  }
}