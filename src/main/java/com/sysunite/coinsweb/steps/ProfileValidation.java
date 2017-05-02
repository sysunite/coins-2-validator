package com.sysunite.coinsweb.steps;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.sysunite.coinsweb.parser.config.Locator;

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
}
