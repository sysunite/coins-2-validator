package com.sysunite.coinsweb.parser.config.pojo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.util.StdConverter;
import com.sysunite.coinsweb.connector.ConnectorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static com.sysunite.coinsweb.parser.Parser.isNotNull;

/**
 * @author bastbijl, Sysunite 2017
 */
@JsonInclude(Include.NON_NULL)
@JsonDeserialize(converter=StoreSanitizer.class)
public class Store extends ConfigPart {
  private static final Logger log = LoggerFactory.getLogger(Store.class);

  public static ConnectorFactory factory;

  private String type;

  private Map<String, String> config;

  public String getType() {
    return type;
  }
  public Map<String, String> getConfig() {
    return config;
  }

  public void setType(String type) {
    this.type = type;
  }

  public void setConfig(Map<String, String> config) {
    this.config = config;
  }
}

class StoreSanitizer extends StdConverter<Store, Store> {
  private static final Logger log = LoggerFactory.getLogger(StoreSanitizer.class);

  @Override
  public Store convert(Store obj) {

    if(Store.factory == null) {
      log.warn("Please set the static field com.sysunite.coinsweb.parser.config.pojo.Store.factory to some instance!");
      throw new RuntimeException("Please set the static field com.sysunite.coinsweb.parser.config.pojo.Store.factory to some instance!");
    }

    isNotNull(obj.getType());
    if(!"none".equals(obj.getType()) && !Store.factory.exists(obj.getType())) {
      log.warn("This value was not found as connector type: " + obj.getType());
      throw new RuntimeException("This value was not found as connector type: "+obj.getType());
    }

    return obj;
  }
}
