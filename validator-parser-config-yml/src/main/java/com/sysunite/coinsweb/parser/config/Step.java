package com.sysunite.coinsweb.parser.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.sysunite.coinsweb.steps.ValidationStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author bastbijl, Sysunite 2017
 */
@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonDeserialize(using=StepDeserializer.class, converter=StepSanitizer.class)
public class Step {

  private static final Logger log = LoggerFactory.getLogger(Step.class);

  private String type;
  private ValidationStep validationStep;

  public String getType() {
    return type;
  }


  public void setType(String type) {
    this.type = type;
  }



  public ValidationStep getValidationStep() {
    return validationStep;
  }

  public void setValidationStep(ValidationStep validationStep) {
    this.validationStep = validationStep;
  }
}

