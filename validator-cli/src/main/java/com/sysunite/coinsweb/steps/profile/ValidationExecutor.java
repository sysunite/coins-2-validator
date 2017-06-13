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
import com.sysunite.coinsweb.parser.profile.Bundle;
import com.sysunite.coinsweb.parser.profile.ProfileFile;
import com.sysunite.coinsweb.parser.profile.Query;
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




//    Runtime runtime = Runtime.getRuntime();

    // Execute bundles in order of appearance
    for(Bundle bundle : profile.getBundles()) {
      boolean containsUpdate = false;
      boolean someTripleWasAdded = false;

      HashMap<String, ValidationQueryResult> resultMap = new HashMap();

      log.info("\uD83D\uDC1A Will perform bundle \""+bundle.getReference()+"\"");

      int run = 1;
      do {

        if(run > MAX_RUNS) {
          throw new RuntimeException("Break running, max number of repeated runs reached for bundle: "+bundle.getReference());
        }

        for (Query query : bundle.getQueries()) {

          ValidationQueryResult resultCarrier;
          if(!resultMap.containsKey(query.getReference())) {
            resultCarrier = new ValidationQueryResult(query);
            resultMap.put(query.getReference(), resultCarrier);
          } else {
            resultCarrier = resultMap.get(query.getReference());
          }


          if (Query.UPDATE.equals(query.getType())) {
            containsUpdate = true;

            String queryString = QueryFactory.buildQuery(query, profile.getQueryConfiguration());
            graphSet.update(queryString, resultCarrier);
            resultCarrier.addRunStatistics(graphSet.quadCount());
            if(resultCarrier.quadsAddedLastRun() > 0) {
              someTripleWasAdded = true;
            }


          }
          if (Query.NO_RESULT.equals(query.getType())) {

            String queryString = QueryFactory.buildQuery(query, profile.getQueryConfiguration());
//            graphSet.select(queryString, resultCarrier);

          }

        }

        run++;
      } while(containsUpdate && someTripleWasAdded);
    }


//    boolean profileChecks = executeQueries(profile.getRequirements(), execution.getProfileCheckResults());
//    execution.updateMemMaxUsage(runtime.totalMemory());



//    execution.setProfileChecksPassed(profileChecks);
//    execution.setValidationPassed(validationRules);
//    execution.setExecutionTime(new Date().getTime() - start);





    log.info("Built report");



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
}
