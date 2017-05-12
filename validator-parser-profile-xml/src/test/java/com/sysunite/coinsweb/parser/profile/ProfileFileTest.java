package com.sysunite.coinsweb.parser.profile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.junit.Test;

import java.io.InputStream;


/**
 * @author bastbijl, Sysunite 2017
 */
public class ProfileFileTest {



  @Test
  public void test() {

    ObjectMapper objectMapper = new XmlMapper();
    try {
      InputStream file = getClass().getClassLoader().getResource("profile.lite-9.60.xml").openStream();
      ProfileFile profileFile = objectMapper.readValue(file, ProfileFile.class);

      System.out.println();
      System.out.println("name: " + profileFile.getName());
      System.out.println("version: " + profileFile.getVersion());
      System.out.println("author: " + profileFile.getAuthor());

      for(Step requirement : profileFile.getRequirements()) {
        System.out.println("requirement: " + requirement.getReference());
        System.out.println("requirement: " + requirement.buildQuery());
      }

      for(Run runs : profileFile.getRuns()) {
        System.out.println("run name: " + runs.getName());
        for(Step step : runs.getSteps()) {
          System.out.println("step: " + step.getReference());
          System.out.println("step: " + step.getDescription());
          System.out.println("step: " + step.getFormat());
          System.out.println("step: " + step.buildQuery());
        }
      }





      profileFile.getRequirements();
    } catch (Exception e) {
      System.out.println(e.getLocalizedMessage());
    }
  }
}