package com.sysunite.coinsweb.parser.config;

import com.fasterxml.jackson.databind.util.StdConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.sysunite.coinsweb.parser.Parser.isNotEmpty;

/**
 * @author bastbijl, Sysunite 2017
 */
class RunSanitizer extends StdConverter<Run, Run> {

  private static final Logger log = LoggerFactory.getLogger(RunSanitizer.class);

  @Override
  public Run convert(Run obj) {

    isNotEmpty(obj.getContainers());
    isNotEmpty(obj.getSteps());
    isNotEmpty(obj.getReports());

    return obj;
  }
}