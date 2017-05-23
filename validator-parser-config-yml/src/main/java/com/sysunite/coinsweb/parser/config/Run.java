package com.sysunite.coinsweb.parser.config;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author bastbijl, Sysunite 2017
 */
@JsonDeserialize(converter=RunSanitizer.class)
public class Run {

  private static final Logger log = LoggerFactory.getLogger(Run.class);

  private Container[] containers;
  private Step[] steps;
  private Report[] reports;

  public Container[] getContainers() {
    return containers;
  }
  public Step[] getSteps() {
    return steps;
  }
  public Report[] getReports() {
    return reports;
  }

  public void setContainers(Container[] containers) {
    this.containers = containers;
  }

  public void setSteps(Step[] steps) {
    this.steps = steps;
  }

  public void setReports(Report[] reports) {
    this.reports = reports;
  }
}
