package com.sysunite.coinsweb.steps;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.sysunite.coinsweb.filemanager.ContainerFile;
import com.sysunite.coinsweb.graphset.ContainerGraphSet;
import com.sysunite.coinsweb.parser.config.Locator;
import com.sysunite.coinsweb.parser.profile.ProfileFile;
import com.sysunite.coinsweb.validator.ValidationExecutor;

import java.util.Map;

/**
 * @author bastbijl, Sysunite 2017
 */
@JsonIgnoreProperties({"type"})
public class ProfileValidation implements ValidationStep {
  private Locator profile;
  private int maxResults;
  private int minResults;
  public Locator getProfile() {
    return profile;
  }
  public int getMaxResults() {
    return maxResults;
  }
  public int getMinResults() {
    return minResults;
  }
  public void setProfile(Locator profile) {
    this.profile = profile;
  }
  public void setMaxResults(int maxResults) {
    this.maxResults = maxResults;
  }
  public void setMinResults(int minResults) {
    this.minResults = minResults;
  }

  @Override
  public Map<String, Object> execute(ContainerFile container, ContainerGraphSet graphSet) {

    // Load the profile file
    ProfileFile profileFile = ProfileFile.parse(profile);
    ValidationExecutor executor = new ValidationExecutor(profileFile, graphSet);

    // Execute the validation
    return executor.validate();
  }
}
