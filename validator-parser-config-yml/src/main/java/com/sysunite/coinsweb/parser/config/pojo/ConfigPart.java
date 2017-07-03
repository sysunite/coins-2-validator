package com.sysunite.coinsweb.parser.config.pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @author bastbijl, Sysunite 2017
 */
public abstract class ConfigPart {

  @JsonIgnore
  private ConfigFile parent;


  public ConfigFile getParent() {
    return parent;
  }
  public void setParent(ConfigFile parent) {
    if(parent == null) {
      return;
    }
    if(this.parent != null) {
      if(!this.parent.equals(parent)) {
        throw new RuntimeException("This ConfigPart was already part of some other ConfigFile, please clone it");
      }
    }
    this.parent = parent;
  }
}
