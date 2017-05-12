package com.sysunite.coinsweb.parser.config;

import com.fasterxml.jackson.databind.util.StdConverter;
import org.apache.log4j.Logger;

import static com.sysunite.coinsweb.parser.Parser.isNotNull;

/**
 * @author bastbijl, Sysunite 2017
 */
class StepSanitizer extends StdConverter<Step, Step> {

  private static final Logger log = Logger.getLogger(StepSanitizer.class);

  @Override
  public Step convert(Step obj) {
    isNotNull(obj.getType());
    isNotNull(obj.getValidationStep());

    return obj;
  }
}
