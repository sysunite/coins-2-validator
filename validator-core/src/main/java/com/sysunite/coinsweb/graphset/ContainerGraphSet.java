package com.sysunite.coinsweb.graphset;

import com.sysunite.coinsweb.steps.ValidationStepResult;

import java.util.Map;

/**
 * @author bastbijl, Sysunite 2017
 */
public interface ContainerGraphSet {

  ValidationStepResult select(Object obj);
  Map<String, Long> numTriples();
  void insert(Object obj, ValidationStepResult result);

}
