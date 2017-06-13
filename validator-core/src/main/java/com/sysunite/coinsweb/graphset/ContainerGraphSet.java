package com.sysunite.coinsweb.graphset;

import com.sysunite.coinsweb.filemanager.ContainerFile;
import com.sysunite.coinsweb.steps.ValidationStepResult;

import java.util.Map;

/**
 * @author bastbijl, Sysunite 2017
 */
public interface ContainerGraphSet {

  void select(String query, Object formatTemplate, ValidationStepResult result);
  void update(String query, ValidationStepResult result);
  void setContainerFile(ContainerFile container);
  void setContainerConfig(Object containerConfig);
  void setConfigFile(Object configFile);
  Map<String, Long> quadCount();

}
