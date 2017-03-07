package com.sysunite.coinsweb.config;

import com.fasterxml.jackson.databind.util.StdConverter;

import static com.sysunite.coinsweb.config.Parser.*;

/**
 * @author bastbijl, Sysunite 2017
 */
public class Model {

  private String graphname;
  private String content;
  private String type;
  private String uri;
  private String path;
  private Endpoint endpoint;


  public String getGraphname() {
    return graphname;
  }

  public void setGraphname(String graphname) {
    this.graphname = graphname;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    validate(content, "instances", "library");
    this.content = content;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    validate(type, "file", "online", "container", "endpoint");
    this.type = type;
  }

  public String getUri() {
    return uri;
  }

  public void setUri(String uri) {
    this.uri = uri;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public Endpoint getEndpoint() {
    return endpoint;
  }

  public void setEndpoint(Endpoint endpoint) {
    this.endpoint = endpoint;
  }
}


class ModelSanitizer extends StdConverter<Model, Model> {
  @Override
  public Model convert(Model obj) {
    if(obj.getType().equals("file")) {
      isFile(obj.getPath());
    }
    if(obj.getType().equals("online")) {
      isResolvable(obj.getUri());
    }
    if(obj.getType().equals("container")) {
      isFile(obj.getPath());
    }
    if(obj.getType().equals("endpoint")) {
      isNotNull(obj.getEndpoint());
    }
    return obj;
  }
}