package com.sysunite.coinsweb.cli;

import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author bastbijl, Sysunite 2017
 */
public class ApplicationTest {

  public static String configPath = "/Users/bastiaanbijl/Documents/Sysunite/GitHub/Sysunite/coins-2-validator/validator-parser-config-yml/src/test/resources/debug-config.yml";

  @Test
  public void debugRun() {
    String[] args = {"run", configPath};
    Application.main(args);
  }
  @Test
  public void debugRunPiping() throws IOException {
    InputStream stdin = System.in;
    try {
      System.setIn(new FileInputStream(new File(configPath)));
      String[] args = {};
      Application.main(args);
    } finally {
      System.setIn(stdin);
    }
    String[] args = {};
    Application.main(args);
  }
}
