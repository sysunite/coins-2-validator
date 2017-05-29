package com.sysunite.coinsweb.parser.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;

import static com.sysunite.coinsweb.parser.Parser.validate;

/**
 * @author bastbijl, Sysunite 2017
 */
@JsonInclude(Include.NON_NULL)
@JsonDeserialize(converter=LocatorSanitizer.class)
public class Locator {

  private static final Logger log = LoggerFactory.getLogger(Locator.class);

  public static final String FILE = "file";
  public static final String ONLINE = "online";

  private String type;
  private String path;
  private String uri;

  @JsonIgnore
  private Path localizeTo;

  public String getType() {
    return type;
  }
  public String getPath() {
    if(localizeTo == null) {
      return path;
    }
    return localizeTo.relativize(Paths.get(path)).toString();
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

  public void localizeTo(Path path) {
    this.localizeTo = path;
  }
}
