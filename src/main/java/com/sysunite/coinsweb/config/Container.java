package com.sysunite.coinsweb.config;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.util.StdConverter;
import org.apache.log4j.Logger;

import static com.sysunite.coinsweb.config.Parser.*;

/**
 * @author bastbijl, Sysunite 2017
 */
@JsonDeserialize(converter=ContainerSanitizer.class)
public class Container {

  private static Logger log = Logger.getLogger(Environment.class);

  private String type;
  private Model[] models;
  private Locator location;

  public String getType() {
    return type;
  }
  public Model[] getModels() {
    return models;
  }
  public Locator getLocation() {
    return location;
  }


  public void setType(String type) {
    validate(type, "container", "virtual");
    this.type = type;
  }
  public void setModels(Model[] models) {
    this.models = models;
  }
  public void setLocation(Locator location) {
    this.location = location;
  }

}

class ContainerSanitizer extends StdConverter<Container, Container> {

  private static Logger log = Logger.getLogger(ContainerSanitizer.class);

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
