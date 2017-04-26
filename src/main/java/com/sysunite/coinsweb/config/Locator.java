package com.sysunite.coinsweb.config;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.util.StdConverter;
import org.apache.log4j.Logger;

import static com.sysunite.coinsweb.config.Parser.isFile;
import static com.sysunite.coinsweb.config.Parser.isResolvable;
import static com.sysunite.coinsweb.config.Parser.validate;

/**
 * @author bastbijl, Sysunite 2017
 */
@JsonDeserialize(converter=LocatorSanitizer.class)
public class Locator {

  private static final Logger log = Logger.getLogger(Locator.class);

  private String type;
  private String path;
  private String uri;

  public String getType() {
    return type;
  }
  public String getPath() {
    return path;
  }
  public String getUri() {
    return uri;
  }

  public void setType(String type) {
    validate(type, "file", "online");
    this.type = type;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public void setUri(String uri) {
    this.uri = uri;
  }
}


class LocatorSanitizer extends StdConverter<Locator, Locator> {

  private static final Logger log = Logger.getLogger(LocatorSanitizer.class);

  @Override
  public Locator convert(Locator obj) {
    if(obj.getType().equals("file")) {
      isFile(obj.getPath());
    }
    if(obj.getType().equals("online")) {
      isResolvable(obj.getUri());
    }
    return obj;
  }
}