package com.sysunite.coinsweb.filemanager;

import org.apache.log4j.Logger;
import org.junit.Test;

/**
 * @author bastbijl, Sysunite 2017
 */
public class ContainerFileTest {

  Logger log = Logger.getLogger(ContainerFileTest.class);

  @Test
  public void testPath() {
    ContainerFile container_01 = new ContainerFile(getClass().getClassLoader().getResource("otl-2.1/01_NetwerkRuimteVoorbeeld_OTL21.ccr").getFile());
    container_01.scan();
    log.warn(container_01.getContentFiles().size());
    String contentFileName = container_01.getContentFiles().iterator().next();
    log.warn(contentFileName);
    log.warn(container_01.getContentFile(contentFileName));

  }

}
