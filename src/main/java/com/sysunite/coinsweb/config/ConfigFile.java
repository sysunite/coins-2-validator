package com.sysunite.coinsweb.config;

/**
 * @author bastbijl, Sysunite 2017
 */
public class ConfigFile {
  private Environment environment;
  private Run run;

  public Run getRun() {
    return run;
  }

  public void setRun(Run run) {
    this.run = run;
  }

  public Environment getEnvironment() {
    return environment;
  }

  public void setEnvironment(Environment environment) {
    this.environment = environment;
  }
}
