package com.sysunite.coinsweb.steps;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.sysunite.coinsweb.filemanager.ContainerFile;
import com.sysunite.coinsweb.filemanager.FileFactory;
import com.sysunite.coinsweb.graphset.ContainerGraphSet;
import com.sysunite.coinsweb.parser.config.ConfigFile;
import com.sysunite.coinsweb.parser.config.Locator;
import com.sysunite.coinsweb.parser.profile.ProfileFile;
import com.sysunite.coinsweb.validator.ValidationExecutor;

import java.util.Map;

/**
 * @author bastbijl, Sysunite 2017
 */
@JsonIgnoreProperties({"type"})
public class ProfileValidation implements ValidationStep {



  @JsonIgnore
  private ConfigFile configFile;
  @JsonIgnore
  public void setConfigFile(ConfigFile configFile) {
    this.configFile = configFile;
  }

  private Locator profile;
  private int maxResults;
  public Locator getProfile() {
    return profile;
  }
  public int getMaxResults() {
    return maxResults;
  }
  public void setProfile(Locator profile) {
    this.profile = profile;
  }
  public void setMaxResults(int maxResults) {
    this.maxResults = maxResults;
  }


  @Override
  public Map<String, Object> execute(ContainerFile container, ContainerGraphSet graphSet) {

    // Load the profile file
    ProfileFile profileFile = ProfileFile.parse(FileFactory.toInputStream(profile, configFile));
    ValidationExecutor executor = new ValidationExecutor(profileFile, graphSet);

    // Execute the validation
    return executor.validate();
  }
}
