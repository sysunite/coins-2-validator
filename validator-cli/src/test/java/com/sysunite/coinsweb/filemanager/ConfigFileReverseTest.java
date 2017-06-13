package com.sysunite.coinsweb.filemanager;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;


/**
 * @author bastbijl, Sysunite 2017
 */
public class ConfigFileReverseTest {

  Logger log = LoggerFactory.getLogger(ConfigFileReverseTest.class);

  @Test
  public void test() {

    Path projectPath = Paths.get(getClass().getClassLoader().getResource("mix.ccr").getPath()).getParent().getParent().getParent();



//    ContainerFileImpl containerFile = new ContainerFileImpl("/Users/bastiaanbijl/Documents/Sysunite/GitHub/Sysunite/coins-2-sdk/testsuite/target/test-classes/A2/sample.ccr");
//    ContainerFileImpl containerFile = new ContainerFileImpl(getClass().getClassLoader().getResource("otl-2.1/01_NetwerkRuimteVoorbeeld_OTL21.ccr").getFile());
    ContainerFileImpl containerFile = new ContainerFileImpl(getClass().getClassLoader().getResource("mix.ccr").getFile());

    log.warn("\n"+ConfigGenerator.run(containerFile, projectPath));
  }
}