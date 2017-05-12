package com.sysunite.coinsweb.steps;

import java.util.HashMap;
import java.util.Map;

/**
 * @author bastbijl, Sysunite 2017
 */
public class StepFactoryImpl implements StepFactory {

  private static final Map<String, Class<? extends ValidationStep>> register;
  static
  {
    register = new HashMap();
    register.put("FileSystemValidation", FileSystemValidation.class);
    register.put("ProfileValidation", ProfileValidation.class);
    register.put("DocumentReferenceValidation", DocumentReferenceValidation.class);
  }

  public boolean exists(String key) {
    return register.containsKey(key);
  }
  public Class<? extends ValidationStep> get(String key) {
    return register.get(key);
  }

  public ValidationStep getValidationStep() {
    return null;
  }
}
