package com.sysunite.coinsweb.config;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.util.StdConverter;
import org.apache.log4j.Logger;

import java.util.Map;

import static com.sysunite.coinsweb.config.Parser.isNotNull;
import static com.sysunite.coinsweb.config.Parser.validate;

//import org.apache.jena.query.DatasetAccessor;
//import org.apache.jena.query.DatasetAccessorFactory;
//import org.apache.jena.riot.web.HttpOp;

/**
 * @author bastbijl, Sysunite 2017
 */
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
    validate(type, "rdf4j-sail-memory", "graphdb"); // , "virtuoso", "fuseki"
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


    return obj;
  }
}