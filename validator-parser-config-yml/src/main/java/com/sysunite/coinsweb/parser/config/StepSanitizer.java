package com.sysunite.coinsweb.parser.config;

import com.fasterxml.jackson.databind.util.StdConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.sysunite.coinsweb.parser.Parser.isNotNull;

/**
 * @author bastbijl, Sysunite 2017
 */
class StepSanitizer extends StdConverter<Step, Step> {

  private static final Logger log = LoggerFactory.getLogger(StepSanitizer.class);

  @Override
  public Step convert(Step obj) {
    isNotNull(obj.getType());
    isNotNull(obj.getValidationStep());

    return obj;
  }
}
