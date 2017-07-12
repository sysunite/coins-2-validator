package com.sysunite.coinsweb.parser.config.pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.util.StdConverter;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

import static com.sysunite.coinsweb.parser.Parser.*;

/**
 * @author bastbijl, Sysunite 2017
 */
@JsonInclude(Include.NON_NULL)
@JsonDeserialize(converter=SourceSanitizer.class)
public class Source extends ConfigPart {

  private static final Logger log = LoggerFactory.getLogger(Source.class);

  public static final String FILE = "file";
  public static final String ONLINE = "online";
  public static final String CONTAINER = "container";
//  public static final String STORE = "store";


  private String type;
  private String path;
  private String uri;
  private String graphname;
//  private Store store;


  public String getType() {
    return type;
  }
  public String getGraphname() {
    return graphname;
  }
  public String getPath() {
    return FilenameUtils.separatorsToSystem(path);
  }
  public String getUri() {
    return uri;
  }
//  public Store getStore() {
//    return store;
//  }

  public void setType(String type) {
    validate(type, FILE, ONLINE, CONTAINER);
    this.type = type;
  }
  public void setGraphname(String graphname) {
    this.graphname = graphname;
  }
  public void setPath(String path) {
    this.path = path;
  }
  public void setUri(String uri) {
    this.uri = uri;
  }

  @JsonIgnore
  public Locator asLocator() {
    if(!(FILE.equals(getType())||ONLINE.equals(getType()))) {
      throw new RuntimeException("Casting a Source to a Locator is only allowed for type 'file' and 'online'");
    }
    Locator clone = new Locator();
    clone.setType(getType());
    clone.setPath(getPath());
    clone.setUri(getUri());
    clone.setParent(getParent());
    return clone;
  }

  @JsonIgnore
  public boolean anyGraph() {
    boolean anyGraph = "*".equals(graphname);
    return anyGraph;
  }
  @JsonIgnore
  public boolean anyContentFile() {
    boolean anyContentFile = CONTAINER.equals(type) && getPath() != null && getPath().equals("bim" + File.separator + "*");
    if(anyContentFile && !"*".equals(graphname)) {
      throw new RuntimeException("Set graphname to \"*\" when using an asterisk in the container path, it was "+graphname);
    }
    return anyContentFile;
  }
  @JsonIgnore
  public boolean anyLibraryFile() {
    boolean anyLibraryFile = CONTAINER.equals(type) && getPath() != null && getPath().equals("bim" + File.separator + "repository" + File.separator + "*");
    if(anyLibraryFile && !"*".equals(graphname)) {
      throw new RuntimeException("Set graphname to \"*\" when using an asterisk in the container path, it was "+graphname);
    }
    return anyLibraryFile;
  }




//  public void setEndpoint(Store store) {
//    this.store = store;
//    this.store.setParent(this.getParent());
//  }


  @JsonIgnore
  public Source clone() {
    Source clone = new Source();
    clone.setType(this.type);
    clone.setGraphname(this.graphname);
    clone.setPath(this.path);
    clone.setUri(this.uri);
    clone.setParent(this.getParent());
    return clone;
  }

  @Override
  public void setParent(Object parent) {
    super.setParent(parent);
  }
}

class SourceSanitizer extends StdConverter<Source, Source> {

  private static final Logger log = LoggerFactory.getLogger(GraphSanitizer.class);

  @Override
  public Source convert(Source obj) {

    isNotNull(obj.getType());
    isNotNull(obj.getGraphname());

    if(Source.FILE.equals(obj.getType())) {
      if(obj.getPath() != null && obj.getPath().contains("*")) {
        throw new RuntimeException("Wildcards in path are only allowed for sources of type 'container'");
      }
//      isFile(obj.getPath());
    }
    if(Source.ONLINE.equals(obj.getType())) {
      isResolvable(obj.getUri());
    }
    if(Source.CONTAINER.equals(obj.getType())) {
      if(obj.getPath() != null && obj.getPath().contains("*")) {
        if(!obj.anyContentFile() && !obj.anyLibraryFile()) {
          throw new RuntimeException("Only wildcard allowed for bim or bim-repository folder");
        }
      }
    }
//    if(Graph.STORE.equals(obj.getType())) {
//      isNotNull(obj.getStore());
//    }

    return obj;
  }
}
