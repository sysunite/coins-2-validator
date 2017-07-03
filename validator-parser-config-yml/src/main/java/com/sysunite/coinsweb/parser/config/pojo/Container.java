package com.sysunite.coinsweb.parser.config.pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.util.StdConverter;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.sysunite.coinsweb.parser.Parser.*;

/**
 * @author bastbijl, Sysunite 2017
 */
@JsonInclude(Include.NON_NULL)
@JsonDeserialize(converter=ContainerSanitizer.class)
public class Container extends ConfigPart {

  private static final Logger log = LoggerFactory.getLogger(Environment.class);

  public static final String CONTAINER = "container";
  public static final String VIRTUAL = "virtual";

  private String type;
  private Locator location;
  private Graph[] graphs = new Graph[0];

  @JsonIgnore
  private String code;

  public Container() {
    this.code = RandomStringUtils.random(8, true, true);
  }

  public String getType() {
    return type;
  }
  public Locator getLocation() {
    return location;
  }
  public Graph[] getGraphs() {
    return graphs;
  }

  @JsonIgnore
  public String getCode() {
    return code;
  }
  @JsonIgnore
  public boolean isVirtual() {
    return VIRTUAL.equals(this.type);
  }


  public void setType(String type) {
    validate(type, CONTAINER, VIRTUAL);
    this.type = type;
  }
  public void setLocation(Locator location) {
    this.location = location;
    this.location.setParent(this.getParent());
  }
  public void setGraphs(Graph[] graphs) {
    this.graphs = graphs;
    for(Graph graph : this.graphs) {
      graph.setParent(this.getParent());
    }
  }

  @Override
  public void setParent(ConfigFile parent) {
    super.setParent(parent);
    if(this.location != null) {
      this.location.setParent(this.getParent());
    }
    for(Graph graph : this.graphs) {
      graph.setParent(this.getParent());
    }
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

class ContainerSanitizer extends StdConverter<Container, Container> {

  private static final Logger log = LoggerFactory.getLogger(ContainerSanitizer.class);

  @Override
  public Container convert(Container obj) {
    if(Container.CONTAINER.equals(obj.getType())) {
      isNotNull(obj.getLocation());
    }
    if(Container.VIRTUAL.equals(obj.getType())) {
      isNull(obj.getLocation());

      // Not allowed to have sources of type container
      for(Graph graph : obj.getGraphs()) {
        if(Source.CONTAINER.equals(graph.getSource().getType())) {
          throw new RuntimeException("A source of type 'container' is not allowed for a virtual container");
        }
      }
    }
    isNotEmpty(obj.getGraphs());

    return obj;
  }
}
