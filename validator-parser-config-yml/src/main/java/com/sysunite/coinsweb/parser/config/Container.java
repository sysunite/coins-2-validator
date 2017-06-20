package com.sysunite.coinsweb.parser.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
@JsonDeserialize(converter=ContainerSanitizer.class)
public class Container {

  private static final Logger log = LoggerFactory.getLogger(Environment.class);

  private String type;
  private Locator location;
  private Graph[] graphs;

  public String getType() {
    return type;
  }
  public Locator getLocation() {
    return location;
  }
  public Graph[] getGraphs() {
    return graphs;
  }


  public void setType(String type) {
    validate(type, "container", "virtual");
    this.type = type;
  }
  public void setLocation(Locator location) {
    this.location = location;
  }
  public void setGraphs(Graph[] graphs) {
    this.graphs = graphs;
  }


  @JsonIgnore
  public Container clone() {
    Container clone = new Container();
    clone.setType(this.type);
    clone.setLocation(this.location.clone());
    Graph[] graphs = new Graph[this.graphs.length];
    for(int i = 0; i < this.graphs.length; i++) {
      graphs[i] = this.graphs[i].clone();
    }
    clone.setGraphs(graphs);
    return clone;
  }
}
