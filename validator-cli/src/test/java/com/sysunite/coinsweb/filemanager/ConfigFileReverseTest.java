package com.sysunite.coinsweb.filemanager;

import org.apache.log4j.Logger;
import org.junit.Test;


/**
 * @author bastbijl, Sysunite 2017
 */
public class ConfigFileReverseTest {

  Logger log = Logger.getLogger(ConfigFileReverseTest.class);

  @Test
  public void test() {


//    ContainerFileImpl containerFile = new ContainerFileImpl("/Users/bastiaanbijl/Documents/Sysunite/GitHub/Sysunite/coins-2-sdk/testsuite/target/test-classes/A2/sample.ccr");
//    ContainerFileImpl containerFile = new ContainerFileImpl(getClass().getClassLoader().getResource("otl-2.1/01_NetwerkRuimteVoorbeeld_OTL21.ccr").getFile());
    ContainerFileImpl containerFile = new ContainerFileImpl(getClass().getClassLoader().getResource("nquad.ccr").getFile());

    log.warn("\n"+ConfigGenerator.run(containerFile));
  }
}