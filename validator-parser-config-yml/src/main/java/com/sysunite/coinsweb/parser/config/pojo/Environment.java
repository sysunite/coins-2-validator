package com.sysunite.coinsweb.parser.config.pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.util.StdConverter;
import com.sysunite.coinsweb.graphset.GraphVar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

import static com.sysunite.coinsweb.parser.Parser.isNotNull;
import static com.sysunite.coinsweb.parser.Parser.validate;

/**
 * @author bastbijl, Sysunite 2017
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonDeserialize(converter=EnvironmentSanitizer.class)
public class Environment extends ConfigPart {

  private static final Logger log = LoggerFactory.getLogger(Environment.class);

  public static final String PERMANENT = "permanent";
  public static final String HASH_IN_GRAPHNAME = "permanent-sorted-hashes-in-graphname";
  public static final String REPO_PER_RUN = "repo-per-run";

  private Store store;
  private String loadingStrategy;
  private Mapping[] graphs;
  private String tempPath;

  public Store getStore() {
    return store;
  }
  public String getLoadingStrategy() {
    return loadingStrategy;
  }
  public Mapping[] getGraphs() {
    return graphs;
  }
  @JsonIgnore
  public HashMap<GraphVar, String> getMapping() {
    HashMap<GraphVar, String> graphs = new HashMap();
    for(Mapping mapping : getGraphs()) {
      graphs.put(mapping.getVariable(), mapping.getGraphname());
    }
    return graphs;
  }
  public String getTempPath() {
    return tempPath;
  }

  public void setStore(Store store) {
    this.store = store;
    this.store.setParent(this.getParent());
  }
  public void setLoadingStrategy(String loadingStrategy) {

    validate(loadingStrategy, PERMANENT, HASH_IN_GRAPHNAME, REPO_PER_RUN);
    this.loadingStrategy = loadingStrategy;
  }
  public void setGraphs(Mapping[] graphs) {
    this.graphs = graphs;
    for(Mapping mapping : this.graphs) {
      mapping.setParent(this.getParent());
    }
  }
  public void setTempPath(String tempPath) {
    this.tempPath = tempPath;
  }

  @Override
  public void setParent(Object parent) {
    super.setParent(parent);
    if(this.store != null) {
      this.store.setParent(this.getParent());
    }
    for(Mapping mapping : this.graphs) {
      mapping.setParent(this.getParent());
    }
  }
}

class EnvironmentSanitizer extends StdConverter<Environment, Environment> {

  private static final Logger log = LoggerFactory.getLogger(EnvironmentSanitizer.class);

  @Override
  public Environment convert(Environment obj) {
    isNotNull(obj.getStore());
    isNotNull(obj.getLoadingStrategy());

    return obj;
  }
}
