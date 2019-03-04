package com.sysunite.coinsweb.parser.config.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.sysunite.coinsweb.filemanager.ContainerFile;
import com.sysunite.coinsweb.graphset.ContainerGraphSet;
import com.sysunite.coinsweb.steps.ValidationStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author bastbijl, Sysunite 2017
 */
@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)

public class StepStub extends ConfigPart implements ValidationStep {
  private static final Logger log = LoggerFactory.getLogger(StepStub.class);

  private String type;

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  @Override
  public void checkConfig() {
    // stub
  }

  @Override
  public boolean getFailed() {
    return false;
  }

  @Override
  public ValidationStep clone() {
    StepStub clone = new StepStub();

    // Configuration
    clone.setType(this.getType());
    clone.setParent(this.getParent());

    return clone;
  }

  @Override
  public boolean getValid() {
    return false;
  }

  @Override
  public void execute(ContainerFile container, ContainerGraphSet graphSet) {
    // stub
  }

}
