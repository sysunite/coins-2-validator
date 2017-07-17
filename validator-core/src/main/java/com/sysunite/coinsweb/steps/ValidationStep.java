package com.sysunite.coinsweb.steps;

import com.sysunite.coinsweb.filemanager.ContainerFile;
import com.sysunite.coinsweb.graphset.ContainerGraphSet;

import java.util.Map;

/**
 * @author bastbijl, Sysunite 2017
 */
public interface ValidationStep {

  // Pojo part
  String getType();
  void checkConfig();
  void setParent(Object configFile);

  boolean getFailed();
  boolean getValid();

  // Logic part
  Map<String, Object> execute(ContainerFile container, ContainerGraphSet graphSet);
}
