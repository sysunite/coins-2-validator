package com.sysunite.coinsweb.parser.profile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.junit.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;


/**
 * @author bastbijl, Sysunite 2017
 */
public class ProfileFileTest {

  Logger log = Logger.getLogger(ProfileFileTest.class);

  @Test
  public void test() {

    ObjectMapper objectMapper = new XmlMapper();
    try {
      File file = new File(getClass().getClassLoader().getResource("profile.lite-9.60.xml").getFile());
      ProfileFile profileFile = objectMapper.readValue(StringUtils.toEncodedString(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8), ProfileFile.class);

      log.warn("name: " + profileFile.getName());
      log.warn("version: " + profileFile.getVersion());
      log.warn("author: " + profileFile.getAuthor());

      for(Step requirement : profileFile.getRequirements()) {
        log.warn("requirement: "+requirement.getReference());
        log.warn("requirement: "+requirement.buildQuery());
      }

      for(Run runs : profileFile.getRuns()) {
        log.warn("run name: "+ runs.getName());
        for(Step step : runs.getSteps()) {
          log.warn("step: "+step.getReference());
          log.warn("step: "+step.getDescription());
          log.warn("step: "+step.getFormat());
          log.warn("step: "+step.buildQuery());
        }
      }





      profileFile.getRequirements();
    } catch (Exception e) {
      System.out.println(e.getLocalizedMessage());
    }
  }
}