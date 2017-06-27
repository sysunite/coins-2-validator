package com.sysunite.coinsweb.parser.config.pojo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;


/**
 * @author bastbijl, Sysunite 2017
 */
@JsonInclude(Include.NON_NULL)
@JsonDeserialize(converter=StoreSanitizer.class)
public class Store {

  private static final Logger log = LoggerFactory.getLogger(Store.class);

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
