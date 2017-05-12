package com.sysunite.coinsweb.steps;

import com.sysunite.coinsweb.steps.ValidationStep;

/**
 * @author bastbijl, Sysunite 2017
 */
public interface StepFactory {

  boolean exists(String key);
  Class<? extends ValidationStep> get(String key);

  ValidationStep getValidationStep();
}
