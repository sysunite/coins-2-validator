package com.sysunite.coinsweb.parser.config.pojo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.util.StdConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
  private String tempPath;

  private boolean cleanUp = false;
  private boolean createRepo = true;
  private boolean destroyRepo = false;

  public Store getStore() {
    return store;
  }
  public String getLoadingStrategy() {
    return loadingStrategy;
  }
  public String getTempPath() {
    return tempPath;
  }
  public boolean getCleanUp() {
    return cleanUp;
  }
  public boolean getCreateRepo() {
    return createRepo;
  }
  public boolean getDestroyRepo() {
    return destroyRepo;
  }

  public void setStore(Store store) {
    this.store = store;
    this.store.setParent(this.getParent());
  }
  public void setLoadingStrategy(String loadingStrategy) {

    validate(loadingStrategy, PERMANENT, HASH_IN_GRAPHNAME, REPO_PER_RUN);
    this.loadingStrategy = loadingStrategy;
  }
  public void setTempPath(String tempPath) {
    this.tempPath = tempPath;
  }
  public void setCleanUp(boolean cleanUp) {
    this.cleanUp = cleanUp;
  }
  public void setCreateRepo(boolean createRepo) {
    this.createRepo = createRepo;
  }
  public void setDestroyRepo(boolean destroyRepo) {
    this.destroyRepo = destroyRepo;
  }

  @Override
  public void setParent(Object parent) {
    super.setParent(parent);
    if(this.store != null) {
      this.store.setParent(this.getParent());
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
