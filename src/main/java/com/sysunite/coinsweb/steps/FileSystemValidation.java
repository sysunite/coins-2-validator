package com.sysunite.coinsweb.steps;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.sysunite.coinsweb.filemanager.ContainerFile;
import com.sysunite.coinsweb.graphset.ContainerGraphSet;

import java.util.Map;

/**
 * @author bastbijl, Sysunite 2017
 */
@JsonIgnoreProperties({"type"})
public class FileSystemValidation implements ValidationStep {

  @Override
  public Map<String, Object> execute(ContainerFile container, ContainerGraphSet graphSet) {
    return null;
  }
}
