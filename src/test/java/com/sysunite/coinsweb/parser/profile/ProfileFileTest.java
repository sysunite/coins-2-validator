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

      for(Step schemaInference : profileFile.getSchemaInferences()) {
        log.warn("schemaInference: "+schemaInference.getReference());
        log.warn("schemaInference: "+schemaInference.buildQuery());
      }

      for(Step dataInference : profileFile.getDataInferences()) {
        log.warn("dataInference: "+dataInference.getReference());
        log.warn("dataInference: "+dataInference.buildQuery());
      }

      for(Step rule : profileFile.getRules()) {
        log.warn("rule: "+rule.getReference());
        log.warn("rule: "+rule.buildQuery());
      }

      profileFile.getRequirements();
    } catch (Exception e) {
      System.out.println(e.getLocalizedMessage());
    }
  }
}