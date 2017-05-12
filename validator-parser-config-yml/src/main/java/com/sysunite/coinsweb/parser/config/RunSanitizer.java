package com.sysunite.coinsweb.parser.config;

import com.fasterxml.jackson.databind.util.StdConverter;
import org.apache.log4j.Logger;

import static com.sysunite.coinsweb.parser.Parser.isNotEmpty;

/**
 * @author bastbijl, Sysunite 2017
 */
class RunSanitizer extends StdConverter<Run, Run> {

  private static final Logger log = Logger.getLogger(RunSanitizer.class);

  @Override
  public Run convert(Run obj) {

    isNotEmpty(obj.getContainers());
    isNotEmpty(obj.getSteps());
    isNotEmpty(obj.getReports());

    return obj;
  }
}