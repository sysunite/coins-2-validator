package com.sysunite.coinsweb.config;

/**
 * @author bastbijl, Sysunite 2017
 */
public class Report {
  private String type;
  private String path;
  private Locator template;

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public Locator getTemplate() {
    return template;
  }

  public void setTemplate(Locator template) {
    this.template = template;
  }
}
