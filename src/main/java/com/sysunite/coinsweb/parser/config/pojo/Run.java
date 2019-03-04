package com.sysunite.coinsweb.parser.config.pojo;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.util.StdConverter;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.sysunite.coinsweb.steps.ValidationStep;
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
  @JsonDeserialize(using=StepDeserializer.class)
  private ValidationStep[] steps;
  private Report[] reports;

  @JacksonXmlProperty(localName = "container")
  @JacksonXmlElementWrapper(localName="containers")
  public Container[] getContainers() {
    return containers;
  }

  @JacksonXmlProperty(localName = "step")
  @JacksonXmlElementWrapper(localName="steps")
  public ValidationStep[] getSteps() {
    return steps;
  }

  @JacksonXmlProperty(localName = "report")
  @JacksonXmlElementWrapper(localName="reports")
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

  public void setSteps(ValidationStep[] steps) {
    this.steps = steps;
    for(ValidationStep step : this.steps) {
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
  public void setParent(Object parent) {
    super.setParent(parent);
    for(Container container : this.containers) {
      container.setParent(this.getParent());
    }
    for(ValidationStep step : this.steps) {
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
