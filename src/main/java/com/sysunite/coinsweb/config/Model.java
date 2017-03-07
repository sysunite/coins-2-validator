package com.sysunite.coinsweb.config;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.util.StdConverter;
import org.apache.log4j.Logger;

import static com.sysunite.coinsweb.config.Parser.*;

/**
 * @author bastbijl, Sysunite 2017
 */
@JsonDeserialize(converter=ModelSanitizer.class)
public class Model {

  private static Logger log = Logger.getLogger(Model.class);

  private String graphname;
  private String content;
  private String type;
  private String uri;
  private String path;
  private Endpoint endpoint;


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
  public Endpoint getEndpoint() {
    return endpoint;
  }

  public void setGraphname(String graphname) {
    this.graphname = graphname;
  }

  public void setContent(String content) {
    validate(content, "instances", "library");
    this.content = content;
  }

  public void setType(String type) {
    validate(type, "file", "online", "container", "endpoint");
    this.type = type;
  }

  public void setUri(String uri) {
    this.uri = uri;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public void setEndpoint(Endpoint endpoint) {
    this.endpoint = endpoint;
  }
}


class ModelSanitizer extends StdConverter<Model, Model> {

  private static Logger log = Logger.getLogger(ModelSanitizer.class);

  @Override
  public Model convert(Model obj) {

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
    if(obj.getType().equals("endpoint")) {
      isNotNull(obj.getEndpoint());
    }
    return obj;
  }
}