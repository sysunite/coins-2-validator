package com.sysunite.coinsweb.runner;

import com.sysunite.coinsweb.cli.CliOptions;
import com.sysunite.coinsweb.parser.config.factory.ConfigFactory;
import com.sysunite.coinsweb.parser.config.pojo.ConfigFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

/**
 * @author bastbijl, Sysunite 2017
 */
public class Describe {

  private static final Logger log = LoggerFactory.getLogger(Describe.class);

  public static String run(boolean useAbsolutePaths, List<File> containers) {

    if(containers.isEmpty()) {
      throw new RuntimeException("No container file found");
    }

    Path localizeTo = null;
    if(!useAbsolutePaths) {
      localizeTo = CliOptions.resolvePath("");
    }

    ConfigFile configFile = ConfigFactory.getDefaultConfig(containers, localizeTo);
    String yml = ConfigFactory.getDefaultConfigString(configFile);
    return yml;
  }
}
