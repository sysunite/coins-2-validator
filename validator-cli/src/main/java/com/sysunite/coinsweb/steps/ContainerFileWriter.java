package com.sysunite.coinsweb.steps;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.sysunite.coinsweb.filemanager.ContainerFile;
import com.sysunite.coinsweb.graphset.ContainerGraphSet;
import com.sysunite.coinsweb.parser.config.pojo.ConfigFile;
import com.sysunite.coinsweb.parser.config.pojo.Locator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


/**
 * @author bastbijl, Sysunite 2017
 */
@JsonIgnoreProperties({"type"})
public class ContainerFileWriter implements ValidationStep {

  private static final Logger log = LoggerFactory.getLogger(ContainerFileWriter.class);

  private Locator location;
  public Locator getLocation() {
    return location;
  }
  public void setLocation(Locator location) {
    this.location = location;
  }

  @Override
  public Map<String, Object> execute(ContainerFile container, ContainerGraphSet graphSet) {

    File file;
    if(location.getParent() != null) {
      file = location.getParent().resolve(location.getPath()).toFile();
    } else {
      file =  new File(location.getPath());
    }
    try {
      file.createNewFile();
    } catch (IOException e) {
      e.printStackTrace();
    }

    Map<String, Object> reportItems = new HashMap();

    reportItems.put("valid",                true);

    return reportItems;
  }

  @JsonIgnore
  private ConfigFile configFile;
  @Override
  public void setParent(Object configFile) {
    this.configFile = (ConfigFile) configFile;
    this.location.setParent(this.getParent());
  }
  public ConfigFile getParent() {
    return this.configFile;
  }
}
