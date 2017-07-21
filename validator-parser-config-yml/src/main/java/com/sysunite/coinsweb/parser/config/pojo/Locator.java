package com.sysunite.coinsweb.parser.config.pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.util.StdConverter;
import com.sysunite.coinsweb.parser.config.factory.FileFactory;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.sysunite.coinsweb.parser.Parser.isResolvable;
import static com.sysunite.coinsweb.parser.Parser.validate;

/**
 * @author bastbijl, Sysunite 2017
 */
@JsonInclude(Include.NON_NULL)
@JsonDeserialize(converter=LocatorSanitizer.class)
public class Locator extends ConfigPart {

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
    String cleanPath = FilenameUtils.separatorsToSystem(path);
    if(localizeTo == null) {
      return cleanPath;
    }
    return localizeTo.relativize(Paths.get(cleanPath)).toString();
  }
  public String getUri() {
    return uri;
  }

  public void setType(String type) {
    validate(type, FILE, ONLINE);
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

  @JsonIgnore
  public String toString() {
    if(path != null) {
      return path;
    }
    if(uri != null) {
      return uri;
    }
    return null;
  }

  @JsonIgnore
  public boolean fileExists() {
    File file = FileFactory.toFile(this);
    return file.exists() && file.isFile();
  }

  @JsonIgnore
  public Locator clone() {
    Locator clone = new Locator();
    clone.setType(this.type);
    clone.setPath(this.path);
    clone.setUri(this.uri);
    clone.localizeTo(this.localizeTo);
    clone.setParent(this.getParent());
    return clone;
  }
}

class LocatorSanitizer extends StdConverter<Locator, Locator> {

  private static final Logger log = LoggerFactory.getLogger(LocatorSanitizer.class);

  @Override
  public Locator convert(Locator obj) {
    if(obj.getType().equals("file")) {
//      isFile(obj.getPath());
    }
    if(obj.getType().equals("online")) {
      isResolvable(obj.getUri());
    }
    return obj;
  }
}
