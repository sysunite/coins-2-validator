package com.sysunite.coinsweb.parser.config;

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
import org.apache.log4j.Logger;

import java.io.IOException;

import static com.sysunite.coinsweb.parser.Parser.isNotNull;

/**
 * @author bastbijl, Sysunite 2017
 */
class StepDeserializer extends StdDeserializer<Step> {

  private static final Logger log = Logger.getLogger(StepDeserializer.class);

  public static StepFactory factory; // todo: handle that this keeps unset

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

    if(!factory.exists(step.getType())) {
      throw new RuntimeException("This value was not found as validation object: "+step.getType());
    }

    Class<? extends ValidationStep> clazz = factory.get(step.getType());
    ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
    ValidationStep validationStep = mapper.treeToValue(node, clazz);

    step.setValidationStep(validationStep);
    return step;
  }
}