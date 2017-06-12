package com.sysunite.coinsweb.graphset;

import com.sysunite.coinsweb.filemanager.ContainerFile;
import com.sysunite.coinsweb.steps.ValidationStepResult;

import java.util.Map;

/**
 * @author bastbijl, Sysunite 2017
 */
public interface ContainerGraphSet {

  ValidationStepResult select(String query);
  Map<String, Long> numTriples();
  void update(String query, ValidationStepResult result);
  void setContainerFile(ContainerFile container);
  void setContainerConfig(Object containerConfig);
  void setConfigFile(Object configFile);

}
