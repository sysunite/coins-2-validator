package com.sysunite.coinsweb.parser.config.pojo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.util.StdConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.sysunite.coinsweb.parser.Parser.isNotNull;

/**
 * @author bastbijl, Sysunite 2017
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonDeserialize(converter=EnvironmentSanitizer.class)
public class Environment extends ConfigPart {
  private static final Logger log = LoggerFactory.getLogger(Environment.class);

  private Store store;
  private String tempPath;

  private boolean cleanUp = false;
  private boolean createRepo = true;
  private boolean destroyRepo = false;
  private boolean useDisk = true;

  public Store getStore() {
    return store;
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
  public boolean getUseDisk() {
    return useDisk;
  }

  public void setStore(Store store) {
    this.store = store;
    this.store.setParent(this.getParent());
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
  public void setUseDisk(boolean useDisk) {
    this.useDisk = useDisk;
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

    return obj;
  }
}
