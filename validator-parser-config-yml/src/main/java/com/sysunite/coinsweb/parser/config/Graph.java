package com.sysunite.coinsweb.parser.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;

import static com.sysunite.coinsweb.parser.Parser.validate;

/**
 * @author bastbijl, Sysunite 2017
 */
@JsonInclude(Include.NON_NULL)
@JsonDeserialize(converter=GraphSanitizer.class)
public class Graph {

  private static final Logger log = LoggerFactory.getLogger(Graph.class);

  public static final String FILE = "file";
  public static final String ONLINE = "online";
  public static final String CONTAINER = "container";
  public static final String STORE = "store";

  private String graphname;
  private ArrayList<String> content;
  private String type;
  private String uri;
  private String path;
  private Store store;


  public String getGraphname() {
    return graphname;
  }
  public ArrayList<String> getContent() {
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

  @JsonIgnore
  public boolean anyGraph() {
    boolean anyGraph = "*".equals(graphname);
    if(anyGraph && !(anyContentFile() || anyLibraryFile())) {
      throw new RuntimeException("Not allowed to use asterisk as graphname wildcard other than using a container selector with wildcard");
    }
    return anyGraph;
  }

  @JsonIgnore
  public boolean anyContentFile() {
    boolean anyContentFile = CONTAINER.equals(type) && path != null && path.equals("bim" + File.separator + "*");
    if(anyContentFile && !"*".equals(graphname)) {
      throw new RuntimeException("Set graphname to \"*\" when using an asterisk in the container path, it was "+graphname);
    }
    return anyContentFile;
  }

  @JsonIgnore
  public boolean anyLibraryFile() {
    boolean anyLibraryFile = CONTAINER.equals(type) && path != null && path.equals("bim" + File.separator + "repository" + File.separator + "*");
    if(anyLibraryFile && !"*".equals(graphname)) {
      throw new RuntimeException("Set graphname to \"*\" when using an asterisk in the container path, it was "+graphname);
    }
    return anyLibraryFile;
  }

  public void setContent(ArrayList<String> content) {
//    validate(content, "instances", "library");
    this.content = content;
  }

  public void setType(String type) {
    validate(type, FILE, ONLINE, CONTAINER, STORE);
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


  @JsonIgnore
  public Graph clone() {
    Graph clone = new Graph();
    clone.setGraphname(this.graphname);
    clone.setContent((ArrayList<String>)this.content.clone());
    clone.setType(this.type);
    clone.setUri(this.uri);
    clone.setPath(this.path);
    clone.setEndpoint(this.store);
    return clone;
  }
}
