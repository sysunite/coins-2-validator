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
import com.sysunite.coinsweb.parser.profile.ProfileFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Bastiaan Bijl
 */
public class ValidationExecutor {

  private static final Logger log = LoggerFactory.getLogger(ValidationExecutor.class);

  private ProfileFile profile;

  public ValidationExecutor(ProfileFile profile) {
   this.profile = profile;
  }

  public Map<String, Object> validate(ContainerGraphSet graphSet) {


//
//
//    log.info("Execute profile.");
//    ProfileExecution execution = new ProfileExecution();
//
//    long start = new Date().getTime();
//
//
//
//
//    Runtime runtime = Runtime.getRuntime();
//
//    log.info("\uD83D\uDC1A Will perform profile checks.");
//    boolean profileChecks = executeQueries(profile.getProfileChecks(), execution.getProfileCheckResults());
//    execution.updateMemMaxUsage(runtime.totalMemory());
//
//    log.info("\uD83D\uDC1A Will add schema inferences.");
//    addInferences(profile.getSchemaInferences(), execution.getSchemaInferenceResults());
//    execution.updateMemMaxUsage(runtime.totalMemory());
//
//    log.info("\uD83D\uDC1A Will add data inferences.");
//    addInferences(profile.getDataInferences(), execution.getDataInferenceResults());
//    execution.updateMemMaxUsage(runtime.totalMemory());
//
//    log.info("\uD83D\uDC1A Will perform validation checks.");
//    boolean validationRules = executeQueries(profile.getValidationRules(), execution.getValidationRuleResults());
//    execution.updateMemMaxUsage(runtime.totalMemory());
//
//    execution.setProfileChecksPassed(profileChecks);
//    execution.setValidationPassed(validationRules);
//    execution.setExecutionTime(new Date().getTime() - start);
//
//
//    log.info("Check ontology imports.");
//    boolean allImportsAvailable = true;
//
//    List<String> libraries = new ArrayList<>();
//    List<String> online = new ArrayList<>();
//    List<String> graphs = new ArrayList<>();
//    List<String> imports = new ArrayList<>();
//    for(Namespace ns : ((JenaCoinsContainer) model.getCoinsContainer()).getAvailableLibraryFiles().keySet()) {
//      graphs.add(ns.toString());
//      libraries.add(((JenaCoinsContainer) model.getCoinsContainer()).getAvailableLibraryFiles().get(ns).getName());
//    }
//    Collections.sort(libraries);
//
//    OntModel instanceOntModel = model.getCoinsGraphSet().getInstanceOntModel();
//    for(String importedUri : instanceOntModel.listImportedOntologyURIs()) {
//      Namespace importedUriNs = new Namespace(importedUri);
//      imports.add(importedUriNs.toString());
//
//      // Is it available from the ccr repository
//      if(graphs.contains(importedUriNs)) {
//
//        allImportsAvailable &= true;
//
//      // Try to find it online
//      } else {
//        try {
//          URL resourceAsUrl = new URL(importedUriNs.withoutHash());
//          HttpURLConnection connection = (HttpURLConnection) resourceAsUrl.openConnection();
//          connection.setRequestMethod("HEAD");
//          int responseCode = connection.getResponseCode();
//          if (responseCode == 200) {
//            log.info("Found active link online: "+importedUri);
//            allImportsAvailable &= true;
//            online.add(importedUriNs.toString());
//            continue;
//          }
//        } catch (MalformedURLException e) {
//        } catch (ProtocolException e) {
//        } catch (IOException e) {
//        }
//
//        log.info("Tried, but did not find active link online for: "+importedUri);
//        allImportsAvailable &= false;
//      }
//    }
//
//
//    log.info("Build report.");


    // Prepare data to transfer to the template
    Map<String, Object> data = new HashMap<>();
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

    return data;
  }







  private boolean executeQueries(List<ValidationQuery> queries, List<ValidationQueryResult> resultCollection) {

    boolean allChecksPassed = true;
//    for(ValidationQuery query : queries) {
//      if(query.hasSparqlQuery()) {
//
//        ValidationQueryResult result = connector.select(query);
//        allChecksPassed &= result.getPassed();
//        resultCollection.add(result);
//      }
//    }
//
    return allChecksPassed;
  }

  private void addInferences(List<InferenceQuery> queries, InferenceExecution inferenceExecution) {
//    addInferences(queries, inferenceExecution, true);
  }
  private void addInferences(List<InferenceQuery> queries, final InferenceExecution inferenceExecution, boolean recursive) {
//
//    // Build a map of all results in resultList
//    HashMap<String, InferenceQueryResult> resultByReference = new HashMap<>();
//
//    long triplesAddedThisRun;
//    long start = new Date().getTime();
//
//    int run = 1;
//
//    do {
//
//      // Prepare list of all queries to be executed this round
//      ExecutorService executor = Executors.newSingleThreadExecutor();
//
//      List<Callable<Object>> todo = new ArrayList<>(queries.size());
//
//      for (InferenceQuery query : queries) {
//        if (query.hasSparqlQuery()) {
//
//          // Prepare a resultCarrier
//          if(!resultByReference.containsKey(query.getReference())) {
//            InferenceQueryResult resultCarrier = new InferenceQueryResult(query.getReference(), query.getDescription(), query.getSparqlQuery(connector), null);
//            resultByReference.put(query.getReference(), resultCarrier);
//          }
//          InferenceQueryResult resultCarrier = resultByReference.get(query.getReference());
//
//          Map<String, Long> before = connector.numTriples();
//          connector.insert(query, resultCarrier);
//          Map<String, Long> diff = InMemGraphSet.diffNumTriples(before, connector.numTriples());
//
//          inferenceExecution.addTriplesAdded(Integer.toString(run), query.getReference(), diff);
//        }
//      }
//
//      // Execute them, calculate the result
//      try {
//        executor.invokeAll(todo);
//      } catch (CancellationException e) {
//        log.error(e.getMessage(), e);
//      } catch (InterruptedException e) {
//        log.error(e.getMessage(), e);
//      }
//
//      String lastRunNr = Integer.toString(inferenceExecution.getNumRuns());
//      Map<String, Long> statistics = inferenceExecution.getTriplesAdded(lastRunNr);
//      if(statistics.containsKey(connector.getFullUnionNamespace())) {
//        triplesAddedThisRun = statistics.get(connector.getFullUnionNamespace());
//        log.info("This round " + triplesAddedThisRun + " triples were added.");
//      } else {
//        triplesAddedThisRun = 0;
//        log.warn("This round no triples were added to the full union graph, which might have unexpected reasons.");
//      }
//
//      run++;
//
//    // Loop
//    } while (recursive && triplesAddedThisRun > 0l);
//
//    // Store all resultCarriers in the allocated list
//    long executionTime = new Date().getTime() - start;
//    inferenceExecution.setExecutionTime(executionTime);
////    inferenceExecution.getQueryResults().addAll(resultByReference.values()); // todo: this might be important
  }
}