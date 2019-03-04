package com.sysunite.coinsweb.parser.config.pojo;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.sysunite.coinsweb.steps.StepFactory;
import com.sysunite.coinsweb.steps.ValidationStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

/**
 * @author bastbijl, Sysunite 2017
 */
public class StepDeserializer extends StdDeserializer<ValidationStep[]> {
  private static final Logger log = LoggerFactory.getLogger(StepDeserializer.class);

  public static StepFactory factory;

  public StepDeserializer() {
    this(null);
  }

  public StepDeserializer(Class<?> vc) {
    super(vc);
  }

  @Override
  public ValidationStep[] deserialize(JsonParser jp, DeserializationContext ctxt) {

    ArrayList<ValidationStep> result = new ArrayList();
    String type = "unsupported";

    try {

      ObjectMapper mapper = (ObjectMapper) jp.getCodec();
      ArrayNode root = mapper.readTree(jp);

      if(StepDeserializer.factory == null) {
        log.warn("Please set the static field com.sysunite.coinsweb.parser.config.pojo.Step.factory to some instance!");
        throw new RuntimeException("Please set the static field com.sysunite.coinsweb.parser.config.pojo.Step.factory to some instance!");
      }

      for(JsonNode node : root) {

        type = node.get("type").textValue();

        if (!factory.exists(type)) {
          throw new RuntimeException("This value was not found as validation object: " + type);
        }

        Class<? extends ValidationStep> clazz = factory.get(type);

        ValidationStep validationStep = mapper.readValue(node.toString(), clazz);
//        validationStep.setParent();
        validationStep.checkConfig();
        result.add(validationStep);
      }
      return result.toArray(new ValidationStep[0]);
    } catch (Exception e) {
      log.error(e.getMessage());
      throw new RuntimeException("Was not able to parse the part of the config yml that configures the Step with type "+type + "\n" + e.getLocalizedMessage());
    }
  }
}
