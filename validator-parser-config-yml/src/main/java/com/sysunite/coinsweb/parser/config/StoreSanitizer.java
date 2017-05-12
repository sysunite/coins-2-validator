package com.sysunite.coinsweb.parser.config;

import com.fasterxml.jackson.databind.util.StdConverter;
import com.sysunite.coinsweb.connector.ConnectorFactory;
import org.apache.log4j.Logger;

import static com.sysunite.coinsweb.parser.Parser.isNotNull;


/**
 * @author bastbijl, Sysunite 2017
 */
class StoreSanitizer extends StdConverter<Store, Store> {

  private static final Logger log = Logger.getLogger(StoreSanitizer.class);

  public static ConnectorFactory factory; // todo: handle that this keeps unset

  @Override
  public Store convert(Store obj) {

    isNotNull(obj.getType());
    if(!"none".equals(obj.getType()) && factory.exists(obj.getType())) {
      throw new RuntimeException("This value was not found as connector type: "+obj.getType());
    }

    return obj;
  }
}