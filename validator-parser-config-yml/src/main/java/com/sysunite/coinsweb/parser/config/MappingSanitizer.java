package com.sysunite.coinsweb.parser.config;

import com.fasterxml.jackson.databind.util.StdConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.sysunite.coinsweb.parser.Parser.*;

/**
 * @author bastbijl, Sysunite 2017
 */
class MappingSanitizer extends StdConverter<Mapping, Mapping> {

  private static final Logger log = LoggerFactory.getLogger(MappingSanitizer.class);

  @Override
  public Mapping convert(Mapping obj) {

    isNotNull(obj.getGraphname());
    isNotNull(obj.getContent());

    return obj;
  }
}