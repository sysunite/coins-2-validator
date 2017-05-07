package com.sysunite.coinsweb.steps;

import java.util.HashMap;
import java.util.Map;

/**
 * @author bastbijl, Sysunite 2017
 */
public class StepFactory {

  private static final Map<String, Class<? extends ValidationStep>> register;
  static
  {
    register = new HashMap();
    register.put("FileSystemValidation", FileSystemValidation.class);
    register.put("ProfileValidation", ProfileValidation.class);
    register.put("DocumentReferenceValidation", DocumentReferenceValidation.class);
  }

  public static boolean exists(String key) {
    return register.containsKey(key);
  }
  public static Class<? extends ValidationStep> get(String key) {
    return register.get(key);
  }
}
