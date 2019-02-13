package com.sysunite.coinsweb.filemanager;

import com.sysunite.coinsweb.parser.config.factory.ConfigFactory;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
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

    ArrayList<File> containers = new ArrayList();
    containers.add(new File(getClass().getClassLoader().getResource("otl-2.1/01_NetwerkRuimteVoorbeeld_OTL21.ccr").getFile()));
    containers.add(new File(getClass().getClassLoader().getResource("mix.ccr").getFile()));

    log.warn("\n"+ ConfigFactory.getDefaultConfig(containers, projectPath));
  }
}