package com.sysunite.coinsweb.parser.config.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.util.StdConverter;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.sysunite.coinsweb.steps.StepFactory;
import com.sysunite.coinsweb.steps.ValidationStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static com.sysunite.coinsweb.parser.Parser.isNotNull;

/**
 * @author bastbijl, Sysunite 2017
 */
@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonDeserialize(using=StepDeserializer.class, converter=StepSanitizer.class)
public class Step extends ConfigPart {

  private static final Logger log = LoggerFactory.getLogger(Step.class);

  public static StepFactory factory;

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
    this.validationStep.setParent(this.getParent());
  }



  @Override
  public void setParent(ConfigFile parent) {
    super.setParent(parent);
    if(this.validationStep != null) {
      this.validationStep.setParent(this.getParent());
    }
  }
}

class StepSanitizer extends StdConverter<Step, Step> {

  private static final Logger log = LoggerFactory.getLogger(StepSanitizer.class);

  @Override
  public Step convert(Step obj) {
    isNotNull(obj.getType());
    isNotNull(obj.getValidationStep());

    return obj;
  }
}

class StepDeserializer extends StdDeserializer<Step> {

  private static final Logger log = LoggerFactory.getLogger(StepDeserializer.class);

  public StepDeserializer() {
    this(null);
  }

  public StepDeserializer(Class<?> vc) {
    super(vc);
  }

  @Override
  public Step deserialize(JsonParser jp, DeserializationContext ctxt)
  throws IOException {
    JsonNode node = jp.getCodec().readTree(jp);


    Step step = new Step();
    step.setType(node.get("type").textValue());



    if(Step.factory == null) {
      log.warn("Please set the static field com.sysunite.coinsweb.parser.config.pojo.StepDeserializer.factory to some instance!");
      throw new RuntimeException("Please set the static field com.sysunite.coinsweb.parser.config.pojo.StepDeserializer.factory to some instance!");
    }

    if(!Step.factory.exists(step.getType())) {
      throw new RuntimeException("This value was not found as validation object: "+step.getType());
    }

    Class<? extends ValidationStep> clazz = Step.factory.get(step.getType());
    ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
    ValidationStep validationStep = mapper.treeToValue(node, clazz);

    step.setValidationStep(validationStep);
    return step;
  }
}
