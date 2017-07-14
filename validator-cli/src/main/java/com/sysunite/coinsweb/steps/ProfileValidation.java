package com.sysunite.coinsweb.steps;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sysunite.coinsweb.filemanager.ContainerFile;
import com.sysunite.coinsweb.graphset.ContainerGraphSet;
import com.sysunite.coinsweb.graphset.GraphVar;
import com.sysunite.coinsweb.graphset.QueryFactory;
import com.sysunite.coinsweb.parser.config.factory.FileFactory;
import com.sysunite.coinsweb.parser.config.pojo.ConfigPart;
import com.sysunite.coinsweb.parser.config.pojo.Locator;
import com.sysunite.coinsweb.parser.profile.pojo.ProfileFile;
import com.sysunite.coinsweb.steps.profile.ValidationExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;

/**
 * @author bastbijl, Sysunite 2017
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProfileValidation extends ConfigPart implements ValidationStep {

  private static final Logger log = LoggerFactory.getLogger(ProfileValidation.class);

  private String type = "ProfileValidation";
  private Locator profile;
  private int maxResults;

  public String getType() {
    return type;
  }
  public Locator getProfile() {
    return profile;
  }
  public int getMaxResults() {
    return maxResults;
  }

  public void setType(String type) {
    this.type = type;
  }
  public void setProfile(Locator profile) {
    this.profile = profile;
    this.profile.setParent(this.getParent());
  }
  public void setMaxResults(int maxResults) {
    this.maxResults = maxResults;
  }


  public void checkConfig() {
  }

  @Override
  public Map<String, Object> execute(ContainerFile container, ContainerGraphSet graphSet) {

    // Load the profile file
    ProfileFile profileFile = ProfileFile.parse(FileFactory.toInputStream(profile));

    // Check if the vars used in the profile are available
    Set<GraphVar> usedVars = QueryFactory.usedVars(profileFile);
    for(GraphVar graphVar : usedVars) {
      if(!graphSet.hasContext(graphVar)) {
        throw new RuntimeException("The specified profile requires "+graphVar+", please specify it in the config.yml");
      }
    }

    ValidationExecutor executor = new ValidationExecutor(profileFile, graphSet);

    // Execute the validation
    return executor.validate();
  }




}



