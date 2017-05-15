package com.sysunite.coinsweb.parser.profile;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

/**
 * @author bastbijl, Sysunite 2017
 */
public class Graph {

  @JsonInclude(Include.NON_NULL)
  @JacksonXmlProperty(localName = "code")
  private String code;

  @JacksonXmlProperty(localName = "uri")
  private String uri;


  @JsonInclude(Include.NON_NULL)
  @JacksonXmlProperty(localName = "description")
  private String description;


  public String getCode() {
    return code;
  }
  public void setCode(String code) {
    this.code = code;
  }

  public String getUri() {
    return uri;
  }
  public void setUri(String uri) {
    this.uri = uri;
  }

  public String getDescription() {
    return description;
  }
  public void setDescription(String description) {
    this.description = description;
  }
}
