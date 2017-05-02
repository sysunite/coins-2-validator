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
@JsonDeserialize(converter=ContainerSanitizer.class)
public class Container {

  private static final Logger log = Logger.getLogger(Environment.class);

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
  public void setGraphs(Graph[] graphs) {
    this.graphs = graphs;
  }
  public void setLocation(Locator location) {
    this.location = location;
  }

}

class ContainerSanitizer extends StdConverter<Container, Container> {

  private static final Logger log = Logger.getLogger(ContainerSanitizer.class);

  @Override
  public Container convert(Container obj) {
    if(obj.getType().equals("container")) {
      isNotNull(obj.getLocation());
    }
    if(obj.getType().equals("virtual")) {
      isNull(obj.getLocation());
    }
    return obj;
  }
}
