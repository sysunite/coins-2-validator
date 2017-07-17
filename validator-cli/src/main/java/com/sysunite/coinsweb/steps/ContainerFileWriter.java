package com.sysunite.coinsweb.steps;

import com.fasterxml.jackson.annotation.JsonInclude;
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
@JsonInclude(JsonInclude.Include.NON_NULL) // todo wrong this line?
public class ContainerFileWriter extends ConfigPart implements ValidationStep {

  private static final Logger log = LoggerFactory.getLogger(ContainerFileWriter.class);


  // Configuration items
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


  // Result items
  private boolean failed = true;
  public boolean getFailed() {
    return failed;
  }
  public void setFailed(boolean failed) {
    this.failed = failed;
  }

  private boolean valid = false;
  public boolean getValid() {
    return valid;
  }
  public void setValid(boolean valid) {
    this.valid = valid;
  }

  public void checkConfig() {
  }

  @Override
  public Map<String, Object> execute(ContainerFile container, ContainerGraphSet graphSet) {

    try {
      File file;
      if (location.getParent() != null) {
        file = location.getParent().resolve(location.getPath()).toFile();
      } else {
        file = new File(location.getPath());
      }
      try {
        file.createNewFile();
      } catch (IOException e) {
        e.printStackTrace();
      }

      this.valid = true;

    } catch (RuntimeException e) {
      log.warn("Executing failed validationStep of type "+getType());
      log.warn(e.getMessage());
      this.failed = true;
    }

    // Prepare data to transfer to the template
    Map<String, Object> reportItems = new HashMap();

    reportItems.put("failed",     getFailed());
    reportItems.put("valid",      getValid());


    return reportItems;
  }



}
