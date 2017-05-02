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


//    ContainerFile containerFile = new ContainerFile("/Users/bastiaanbijl/Documents/Sysunite/GitHub/Sysunite/coins-2-sdk/testsuite/target/test-classes/A2/sample.ccr");
//    ContainerFile containerFile = new ContainerFile(getClass().getClassLoader().getResource("otl-2.1/01_NetwerkRuimteVoorbeeld_OTL21.ccr").getFile());
    ContainerFile containerFile = new ContainerFile(getClass().getClassLoader().getResource("nquad.ccr").getFile());

    log.warn("\n"+ConfigGenerator.run(containerFile));
  }
}