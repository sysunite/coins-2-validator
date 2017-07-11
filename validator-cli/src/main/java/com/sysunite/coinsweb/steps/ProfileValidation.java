package com.sysunite.coinsweb.steps;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.sysunite.coinsweb.filemanager.ContainerFile;
import com.sysunite.coinsweb.parser.config.factory.FileFactory;
import com.sysunite.coinsweb.graphset.ContainerGraphSet;
import com.sysunite.coinsweb.parser.config.pojo.ConfigFile;
import com.sysunite.coinsweb.parser.config.pojo.Locator;
import com.sysunite.coinsweb.parser.config.pojo.Mapping;
import com.sysunite.coinsweb.parser.profile.pojo.ProfileFile;
import com.sysunite.coinsweb.steps.profile.ValidationExecutor;

import java.util.Map;

/**
 * @author bastbijl, Sysunite 2017
 */
@JsonIgnoreProperties({"type"})
public class ProfileValidation implements ValidationStep {





  private Mapping[] graphs;
  private Locator profile;
  private int maxResults;

  public Mapping[] getGraphs() {
    return graphs;
  }
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
  public void setGraphs(Mapping[] graphs) {
    this.graphs = graphs;
    this.profile.setParent(this.getParent());
    for(Mapping mapping : this.graphs) {
      mapping.setParent(this.getParent());
    }
  }


  @Override
  public Map<String, Object> execute(ContainerFile container, ContainerGraphSet graphSet) {

    // Load the profile file
    ProfileFile profileFile = ProfileFile.parse(FileFactory.toInputStream(profile));
    ValidationExecutor executor = new ValidationExecutor(profileFile, graphSet);

    // Execute the validation
    return executor.validate();
  }

  @JsonIgnore
  private ConfigFile configFile;
  @Override
  public void setParent(Object configFile) {
    this.configFile = (ConfigFile) configFile;
    this.profile.setParent(this.getParent());
    for(Mapping mapping : this.graphs) {
      mapping.setParent(this.getParent());
    }
  }
  public ConfigFile getParent() {
    return this.configFile;
  }
}
