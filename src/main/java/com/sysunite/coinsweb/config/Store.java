package com.sysunite.coinsweb.config;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.util.StdConverter;
import org.apache.log4j.Logger;

import static com.sysunite.coinsweb.config.Parser.*;

/**
 * @author bastbijl, Sysunite 2017
 */
@JsonDeserialize(converter=StoreSanitizer.class)
public class Store {

  private static Logger log = Logger.getLogger(Store.class);

  private String type;
  private Endpoint endpoint;

  public String getType() {
    return type;
  }
  public Endpoint getEndpoint() {
    return endpoint;
  }

  public void setType(String type) {
    validate(type, "inmem", "endpoint");
    this.type = type;
  }

  public void setEndpoint(Endpoint endpoint) {
    this.endpoint = endpoint;
  }
}

class StoreSanitizer extends StdConverter<Store, Store> {

  private static Logger log = Logger.getLogger(StoreSanitizer.class);

  @Override
  public Store convert(Store obj) {

    isNotNull(obj.getType());

    if(obj.getType().equals("endpoint")) {
      isNotNull(obj.getEndpoint());
    }
    return obj;
  }
}