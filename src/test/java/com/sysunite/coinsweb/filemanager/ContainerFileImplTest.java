package com.sysunite.coinsweb.filemanager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.Test;

/**
 * @author bastbijl, Sysunite 2017
 */
public class ContainerFileImplTest {
  Logger log = LoggerFactory.getLogger(ContainerFileImplTest.class);

  @Test
  public void testPath() {
    ContainerFileImpl container_01 = new ContainerFileImpl(getClass().getClassLoader().getResource("otl-2.1/01_NetwerkRuimteVoorbeeld_OTL21.ccr").getFile());
    container_01.hasWrongSlashes();
    log.warn(""+container_01.getContentFiles().size());
    String contentFileName = container_01.getContentFiles().iterator().next();
    log.warn(contentFileName);
    log.warn(""+container_01.getContentFile(contentFileName));
  }
}
