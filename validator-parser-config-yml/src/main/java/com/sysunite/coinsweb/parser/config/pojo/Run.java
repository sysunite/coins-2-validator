package com.sysunite.coinsweb.parser.config.pojo;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.util.StdConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.sysunite.coinsweb.parser.Parser.isNotEmpty;
import static com.sysunite.coinsweb.parser.Parser.isNotNull;

/**
 * @author bastbijl, Sysunite 2017
 */
@JsonDeserialize(converter=RunSanitizer.class)
public class Run extends ConfigPart {

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

    // Set pointers to root ConfigFile
    for(Container container : this.containers) {
      container.setParent(this.getParent());
    }
  }

  public void setSteps(Step[] steps) {
    this.steps = steps;
    for(Step step : this.steps) {
      step.setParent(this.getParent());
    }
  }

  public void setReports(Report[] reports) {
    this.reports = reports;
    for(Report report : this.reports) {
      report.setParent(this.getParent());
    }
  }

  @Override
  public void setParent(ConfigFile parent) {
    super.setParent(parent);
    for(Container container : this.containers) {
      container.setParent(this.getParent());
    }
    for(Step step : this.steps) {
      step.setParent(this.getParent());
    }
    for(Report report : this.reports) {
      report.setParent(this.getParent());
    }
  }
}

class RunSanitizer extends StdConverter<Run, Run> {

  private static final Logger log = LoggerFactory.getLogger(RunSanitizer.class);

  @Override
  public Run convert(Run obj) {

    isNotEmpty(obj.getContainers());
    isNotNull(obj.getSteps());


    return obj;
  }
}
