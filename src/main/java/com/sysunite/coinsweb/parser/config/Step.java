package com.sysunite.coinsweb.parser.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
import org.apache.log4j.Logger;

import java.io.IOException;

import static com.sysunite.coinsweb.parser.config.Parser.isNotNull;

/**
 * @author bastbijl, Sysunite 2017
 */
@JsonDeserialize(using=StepDeserializer.class, converter=StepSanitizer.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Step {

  private static final Logger log = Logger.getLogger(Step.class);

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

class StepSanitizer extends StdConverter<Step, Step> {

  private static final Logger log = Logger.getLogger(StepSanitizer.class);

  @Override
  public Step convert(Step obj) {
    isNotNull(obj.getType());
    isNotNull(obj.getValidationStep());

    return obj;
  }
}

class StepDeserializer extends StdDeserializer<Step> {

  private static final Logger log = Logger.getLogger(StepDeserializer.class);

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

    if(!StepFactory.exists(step.getType())) {
      throw new RuntimeException("This value was not found as validation object: "+step.getType());
    }

    Class<? extends ValidationStep> clazz = StepFactory.get(step.getType());
    ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
    ValidationStep validationStep = mapper.treeToValue(node, clazz);

    step.setValidationStep(validationStep);
    return step;
  }
}