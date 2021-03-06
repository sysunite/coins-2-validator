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
  public static final String STORE = "store";

  @JsonIgnore
  private static int fileNameCount = 0;

  private String type;
  private String path;
  private String uri;
  private String graphname;
  private String storeContext;
  private GraphVarImpl graph;
  private String hash;

  public String getType() {
    return type;
  }
  public String getPath() {
    if(path == null) {
      return null;
    }
    if(CONTAINER.equals(getType())) {
      return FilenameUtils.separatorsToUnix(path);
    } else {
      String cleanPath = FilenameUtils.separatorsToSystem(path);
      if(parent != null) {
        return parent.relativize(cleanPath).toString();
      }
      return cleanPath;
    }
  }
  public String getUri() {
    return uri;
  }
  public String getGraphname() {
    return graphname;
  }
  public String getStoreContext() {
    return storeContext;
  }
  public GraphVarImpl getGraph() {
    return graph;
  }
  public String getHash() {
    return this.hash;
  }
  public String getDefaultFileName() {
    if(path != null) {
      return new File(path).getName();
    } else {
      return "file."+(fileNameCount++)+".rdf";
    }
  }

  public void setType(String type) {
    validate(type, FILE, ONLINE, CONTAINER, STORE);
    this.type = type;
  }
  public void setPath(String path) {
    this.path = path;
  }
  public void setUri(String uri) {
    this.uri = uri;
  }
  public void setGraphname(String graphname) {
    this.graphname = graphname;
  }
  public void setStoreContext(String storeContext) {
    this.storeContext = storeContext;
  }
  public void setGraph(GraphVarImpl graph) {
    this.graph = graph;
  }
  public void setHash(String hash) {
    this.hash = hash;
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

//  @JsonIgnore
//  public boolean anyGraph() {
//    return "*".equals(graphname);
//  }
  @JsonIgnore
  public boolean isContentFile() {
    return !isLibraryFile() && CONTAINER.equals(type) && getPath() != null && getPath().startsWith("bim/");
  }
  @JsonIgnore
  public boolean isLibraryFile() {
    return CONTAINER.equals(type) && getPath() != null && getPath().startsWith("bim/repository/");
  }

  @JsonIgnore
  public Source clone() {
    Source clone = new Source();
    clone.setType(this.type);
    clone.setPath(this.path);
    clone.setUri(this.uri);
    clone.setGraphname(this.graphname);
    clone.setStoreContext(this.storeContext);
    clone.setGraph(this.getGraph());
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

    if(Source.FILE.equals(obj.getType())) {
      isNotNull(obj.getPath());
      isNotNull(obj.getGraphname());

      isNull(obj.getUri());
      isNull(obj.getGraph());

      if(obj.getPath().contains("*")) {
        throw new RuntimeException("Wildcards in path are only allowed for sources of type 'container'");
      }
    }
    if(Source.ONLINE.equals(obj.getType())) {
      isResolvable(obj.getUri());
      isNotNull(obj.getGraphname());

      isNull(obj.getPath());
      isNull(obj.getGraph());
    }
    if(Source.CONTAINER.equals(obj.getType())) {
      isNotNull(obj.getPath());
      isNotNull(obj.getGraphname());

      isNull(obj.getUri());
      isNull(obj.getGraph());

      if(obj.getPath().contains("*")) {
        if(!obj.isContentFile() && !obj.isLibraryFile()) {
          throw new RuntimeException("Only wildcard allowed for bim or bim-repository folder");
        }
      }
    }
    if(Source.STORE.equals(obj.getType())) {
      isNotNull(obj.getGraph());

      isNull(obj.getPath());
      isNull(obj.getUri());
      isNull(obj.getGraphname());
    }

    return obj;
  }
}
