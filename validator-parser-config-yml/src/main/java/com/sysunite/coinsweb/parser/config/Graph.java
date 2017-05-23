package com.sysunite.coinsweb.parser.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.sysunite.coinsweb.parser.Parser.*;

/**
 * @author bastbijl, Sysunite 2017
 */
@JsonInclude(Include.NON_NULL)
@JsonDeserialize(converter=GraphSanitizer.class)
public class Graph {

  private static final Logger log = LoggerFactory.getLogger(Graph.class);

  private String graphname;
  private String content;
  private String type;
  private String uri;
  private String path;
  private Store store;


  public String getGraphname() {
    return graphname;
  }
  public String getContent() {
    return content;
  }
  public String getType() {
    return type;
  }
  public String getUri() {
    return uri;
  }
  public String getPath() {
    return path;
  }
  public Store getStore() {
    return store;
  }

  public void setGraphname(String graphname) {
    this.graphname = graphname;
  }

  public void setContent(String content) {
    validate(content, "instances", "library");
    this.content = content;
  }

  public void setType(String type) {
    validate(type, "file", "online", "container", "store");
    this.type = type;
  }

  public void setUri(String uri) {
    this.uri = uri;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public void setEndpoint(Store store) {
    this.store = store;
  }
}
