package com.sysunite.coinsweb.parser.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.util.StdConverter;
import org.apache.log4j.Logger;

import static com.sysunite.coinsweb.parser.config.Parser.*;

/**
 * @author bastbijl, Sysunite 2017
 */
@JsonInclude(Include.NON_NULL)
@JsonDeserialize(converter=GraphSanitizer.class)
public class Graph {

  private static final Logger log = Logger.getLogger(Graph.class);

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


class GraphSanitizer extends StdConverter<Graph, Graph> {

  private static final Logger log = Logger.getLogger(GraphSanitizer.class);

  @Override
  public Graph convert(Graph obj) {

    isNotNull(obj.getGraphname());
    isNotNull(obj.getContent());
    isNotNull(obj.getType());

    if(obj.getType().equals("file")) {
      isFile(obj.getPath());
    }
    if(obj.getType().equals("online")) {
      isResolvable(obj.getUri());
    }
    if(obj.getType().equals("container")) {
      // a container should be referenced in the run section
    }
    if(obj.getType().equals("store")) {
      isNotNull(obj.getStore());
    }
    return obj;
  }
}