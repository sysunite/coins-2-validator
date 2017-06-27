package com.sysunite.coinsweb.parser.config.pojo;

import com.fasterxml.jackson.databind.util.StdConverter;
import com.sysunite.coinsweb.connector.ConnectorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.sysunite.coinsweb.parser.Parser.isNotNull;


/**
 * @author bastbijl, Sysunite 2017
 */
public class StoreSanitizer extends StdConverter<Store, Store> {

  private static final Logger log = LoggerFactory.getLogger(StoreSanitizer.class);

  public static ConnectorFactory factory;

  @Override
  public Store convert(Store obj) {

    if(factory == null) {
      log.warn("Please set the static field com.sysunite.coinsweb.parser.config.pojo.StoreSanitizer.factory to some instance!");
      throw new RuntimeException("Please set the static field com.sysunite.coinsweb.parser.config.pojo.StoreSanitizer.factory to some instance!");
    }

    isNotNull(obj.getType());
    if(!"none".equals(obj.getType()) && !factory.exists(obj.getType())) {
      log.warn("This value was not found as connector type: " + obj.getType());
      throw new RuntimeException("This value was not found as connector type: "+obj.getType());
    }

    return obj;
  }
}