package com.sysunite.coinsweb.parser.config.pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

import static com.sysunite.coinsweb.parser.Parser.validate;

/**
 * @author bastbijl, Sysunite 2017
 */
@JsonDeserialize(converter=EnvironmentSanitizer.class)
public class Environment {

  private static final Logger log = LoggerFactory.getLogger(Environment.class);

  public static final String HASH_IN_GRAPHNAME = "permanent-sorted-hashes-in-graphname";
  public static final String REPO_PER_RUN = "repo-per-run";

  private Store store;
  private String loadingStrategy;
  private Mapping[] graphs;

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
  public HashMap<String, String> getMapping() {
    HashMap<String, String> graphs = new HashMap();
    for(Mapping mapping : getGraphs()) {
      graphs.put(mapping.getContent(), mapping.getGraphname());
    }
    return graphs;
  }

  public void setStore(Store store) {
    this.store = store;
  }
  public void setLoadingStrategy(String loadingStrategy) {

    validate(loadingStrategy, HASH_IN_GRAPHNAME, REPO_PER_RUN);
    this.loadingStrategy = loadingStrategy;
  }
  public void setGraphs(Mapping[] graphs) {
    this.graphs = graphs;
  }
}
