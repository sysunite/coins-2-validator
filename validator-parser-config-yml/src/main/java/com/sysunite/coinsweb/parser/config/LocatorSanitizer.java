package com.sysunite.coinsweb.parser.config;

import com.fasterxml.jackson.databind.util.StdConverter;
import org.apache.log4j.Logger;

import static com.sysunite.coinsweb.parser.Parser.isResolvable;

/**
 * @author bastbijl, Sysunite 2017
 */
class LocatorSanitizer extends StdConverter<Locator, Locator> {

  private static final Logger log = Logger.getLogger(LocatorSanitizer.class);

  @Override
  public Locator convert(Locator obj) {
    if(obj.getType().equals("file")) {
//      isFile(obj.getPath());
    }
    if(obj.getType().equals("online")) {
      isResolvable(obj.getUri());
    }
    return obj;
  }
}