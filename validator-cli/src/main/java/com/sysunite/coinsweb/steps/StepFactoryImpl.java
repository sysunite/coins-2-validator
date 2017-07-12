package com.sysunite.coinsweb.steps;

import java.util.ArrayList;
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
    register.put("DocumentReferenceValidation", DocumentReferenceValidation.class);
    register.put("ProfileValidation", ProfileValidation.class);
    register.put("ContainerFileWriter", ContainerFileWriter.class);
  }

  public boolean exists(String key) {
    return register.containsKey(key);
  }
  public Class<? extends ValidationStep> get(String key) {
    return register.get(key);
  }

  public ValidationStep[] getDefaultSteps() {
    ArrayList<ValidationStep> steps = new ArrayList();

    FileSystemValidation fileSystemValidation = new FileSystemValidation();
    steps.add(fileSystemValidation);

    DocumentReferenceValidation profileValidation = new DocumentReferenceValidation();
    steps.add(profileValidation);

    ProfileValidation documentReferenceValidation = new ProfileValidation();
    steps.add(documentReferenceValidation);

    return steps.toArray(new ValidationStep[0]);
  }
}
