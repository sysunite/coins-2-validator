package com.sysunite.coinsweb.steps;

import com.sysunite.coinsweb.filemanager.ContainerFile;
import com.sysunite.coinsweb.graphset.ContainerGraphSet;
import com.sysunite.coinsweb.parser.config.pojo.ConfigPart;
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
public class ContainerFileWriter extends ConfigPart implements ValidationStep {

  private static final Logger log = LoggerFactory.getLogger(ContainerFileWriter.class);


  private String type = "ContainerFileWriter";
  public String getType() {
    return type;
  }
  public void setType(String type) {
    this.type = type;
  }



  private Locator location;
  public Locator getLocation() {
    return location;
  }
  public void setLocation(Locator location) {
    this.location = location;
  }

  public void checkConfig() {
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



}
