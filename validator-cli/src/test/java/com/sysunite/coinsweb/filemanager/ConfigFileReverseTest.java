package com.sysunite.coinsweb.filemanager;

import com.sysunite.coinsweb.parser.config.factory.ConfigFactory;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;


/**
 * @author bastbijl, Sysunite 2017
 */
public class ConfigFileReverseTest {

  Logger log = LoggerFactory.getLogger(ConfigFileReverseTest.class);

  @Test
  public void test() {

    Path projectPath = Paths.get(getClass().getClassLoader().getResource("mix.ccr").getPath()).getParent().getParent().getParent();


    ArrayList<ContainerFile> containers = new ArrayList();
//    containers.add(new ContainerFileImpl("/Users/bastiaanbijl/Documents/Sysunite/GitHub/Sysunite/coins-2-sdk/testsuite/target/test-classes/A2/sample.ccr"));
    containers.add(new ContainerFileImpl(getClass().getClassLoader().getResource("otl-2.1/01_NetwerkRuimteVoorbeeld_OTL21.ccr").getFile()));
    containers.add(new ContainerFileImpl(getClass().getClassLoader().getResource("mix.ccr").getFile()));


    log.warn("\n"+ ConfigFactory.describe(containers, projectPath));
  }
}