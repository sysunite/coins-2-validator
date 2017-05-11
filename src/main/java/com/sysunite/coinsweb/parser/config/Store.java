package com.sysunite.coinsweb.parser.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.util.StdConverter;
import com.sysunite.coinsweb.connector.ConnectorFactory;
import org.apache.log4j.Logger;

import java.util.Map;

import static com.sysunite.coinsweb.parser.config.Parser.isNotNull;


/**
 * @author bastbijl, Sysunite 2017
 */
@JsonInclude(Include.NON_NULL)
@JsonDeserialize(converter=StoreSanitizer.class)
public class Store {

  private static final Logger log = Logger.getLogger(Store.class);

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

  private static final Logger log = Logger.getLogger(StoreSanitizer.class);

  @Override
  public Store convert(Store obj) {

    isNotNull(obj.getType());
    if(!"none".equals(obj.getType()) && ConnectorFactory.exists(obj.getType())) {
      throw new RuntimeException("This value was not found as connector type: "+obj.getType());
    }

    return obj;
  }
}