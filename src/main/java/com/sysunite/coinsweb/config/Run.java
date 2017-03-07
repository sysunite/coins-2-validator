package com.sysunite.coinsweb.config;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.util.StdConverter;
import org.apache.log4j.Logger;

import static com.sysunite.coinsweb.config.Parser.isNotEmpty;

/**
 * @author bastbijl, Sysunite 2017
 */
@JsonDeserialize(converter=RunSanitizer.class)
public class Run {

  private static Logger log = Logger.getLogger(Run.class);

  private Locator[] containers;
  private Step[] steps;
  private Report[] reports;

  public Locator[] getContainers() {
    return containers;
  }
  public Step[] getSteps() {
    return steps;
  }
  public Report[] getReports() {
    return reports;
  }

  public void setContainers(Locator[] containers) {
    this.containers = containers;
  }

  public void setSteps(Step[] steps) {
    this.steps = steps;
  }

  public void setReports(Report[] reports) {
    this.reports = reports;
  }
}

class RunSanitizer extends StdConverter<Run, Run> {

  private static Logger log = Logger.getLogger(RunSanitizer.class);

  @Override
  public Run convert(Run obj) {

    isNotEmpty(obj.getContainers());
    isNotEmpty(obj.getSteps());
    isNotEmpty(obj.getReports());

    return obj;
  }
}