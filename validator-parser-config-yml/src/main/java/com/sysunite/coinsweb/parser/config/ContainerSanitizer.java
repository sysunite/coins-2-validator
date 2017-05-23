package com.sysunite.coinsweb.parser.config;

import com.fasterxml.jackson.databind.util.StdConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.sysunite.coinsweb.parser.Parser.isNotNull;
import static com.sysunite.coinsweb.parser.Parser.isNull;

/**
 * @author bastbijl, Sysunite 2017
 */
class ContainerSanitizer extends StdConverter<Container, Container> {

  private static final Logger log = LoggerFactory.getLogger(ContainerSanitizer.class);

  @Override
  public Container convert(Container obj) {
    if(obj.getType().equals("container")) {
      isNotNull(obj.getLocation());
    }
    if(obj.getType().equals("virtual")) {
      isNull(obj.getLocation());
    }
    return obj;
  }
}
