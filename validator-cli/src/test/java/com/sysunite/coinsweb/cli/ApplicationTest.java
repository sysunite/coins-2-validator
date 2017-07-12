package com.sysunite.coinsweb.cli;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * @author bastbijl, Sysunite 2017
 */
public class ApplicationTest {

  Logger log = LoggerFactory.getLogger(ApplicationTest.class);



  @Test
  public void runConfigFull() {
    File file = new File(getClass().getClassLoader().getResource("config/minimal-container.yml").getFile());
    System.setProperty("user.dir", file.getParent());
    String[] args = {"run", file.getPath(), "-l"};
    Application.main(args);
  }

  @Test
  public void parseAndFormatProfileXml() {
    File file = new File(getClass().getClassLoader().getResource("profile.lite-9.82.xml").getFile());
    System.setProperty("user.dir", file.getParent());
    String[] args = {"describe", file.getPath(), "-l"};
    Application.main(args);
  }

//  @Test
//  public void debugRunPiping() throws IOException {
//    InputStream stdin = System.in;
//    try {
//      System.setIn(new FileInputStream(new File(configPath)));
//      String[] args = {};
//      Application.main(args);
//    } finally {
//      System.setIn(stdin);
//    }
//    String[] args = {};
//    Application.main(args);
//  }
}
