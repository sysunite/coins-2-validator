package com.sysunite.coinsweb.config;

/**
 * @author bastbijl, Sysunite 2017
 */
public class Run {
  private Locator[] containers;
  private Step[] steps;
  private Report[] reports;

  public Locator[] getContainers() {
    return containers;
  }

  public void setContainers(Locator[] containers) {
    this.containers = containers;
  }

  public Step[] getSteps() {
    return steps;
  }

  public void setSteps(Step[] steps) {
    this.steps = steps;
  }

  public Report[] getReports() {
    return reports;
  }

  public void setReports(Report[] reports) {
    this.reports = reports;
  }
}
