package com.sysunite.coinsweb.parser.config.pojo;

import com.fasterxml.jackson.databind.util.StdConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.sysunite.coinsweb.parser.Parser.isResolvable;

/**
 * @author bastbijl, Sysunite 2017
 */
class LocatorSanitizer extends StdConverter<Locator, Locator> {

  private static final Logger log = LoggerFactory.getLogger(LocatorSanitizer.class);

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