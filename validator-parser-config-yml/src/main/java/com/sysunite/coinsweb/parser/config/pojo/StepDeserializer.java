package com.sysunite.coinsweb.parser.config.pojo;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.sysunite.coinsweb.steps.StepFactory;
import com.sysunite.coinsweb.steps.ValidationStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @author bastbijl, Sysunite 2017
 */
public class StepDeserializer extends StdDeserializer<Step> {

  private static final Logger log = LoggerFactory.getLogger(StepDeserializer.class);

  public static StepFactory factory;

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



    if(factory == null) {
      log.warn("Please set the static field com.sysunite.coinsweb.parser.config.pojo.StepDeserializer.factory to some instance!");
      throw new RuntimeException("Please set the static field com.sysunite.coinsweb.parser.config.pojo.StepDeserializer.factory to some instance!");
    }

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