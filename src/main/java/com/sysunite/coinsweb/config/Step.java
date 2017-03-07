package com.sysunite.coinsweb.config;

/**
 * @author bastbijl, Sysunite 2017
 */
public class Step {
  private String type;
  private Locator profile;
  private int maxResults;

  public int getMaxResults() {
    return maxResults;
  }

  public void setMaxResults(int maxResults) {
    this.maxResults = maxResults;
  }

  public Locator getProfile() {
    return profile;
  }

  public void setProfile(Locator profile) {
    this.profile = profile;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }
}
