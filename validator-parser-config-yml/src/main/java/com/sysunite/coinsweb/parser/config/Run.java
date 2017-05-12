package com.sysunite.coinsweb.parser.config;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.util.StdConverter;
import org.apache.log4j.Logger;

import static com.sysunite.coinsweb.parser.Parser.isNotEmpty;

/**
 * @author bastbijl, Sysunite 2017
 */
@JsonDeserialize(converter=RunSanitizer.class)
public class Run {

  private static final Logger log = Logger.getLogger(Run.class);

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
