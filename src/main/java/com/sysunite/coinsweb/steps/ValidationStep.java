package com.sysunite.coinsweb.steps;

import com.sysunite.coinsweb.filemanager.ContainerFile;
import com.sysunite.coinsweb.graphset.ContainerGraphSet;

/**
 * @author bastbijl, Sysunite 2017
 */
public interface ValidationStep {

  // Pojo part
  String getType();
  void checkConfig();
  void setParent(Object configFile);

  boolean getValid();
  boolean getFailed();  // a fail means the step is invalid and no other steps should be executed

  ValidationStep clone();

  // Logic part
  void execute(ContainerFile container, ContainerGraphSet graphSet);
}
