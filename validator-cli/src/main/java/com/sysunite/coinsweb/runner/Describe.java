package com.sysunite.coinsweb.runner;

import com.sysunite.coinsweb.cli.CliOptions;
import com.sysunite.coinsweb.connector.Connector;
import com.sysunite.coinsweb.graphset.QueryFactory;
import com.sysunite.coinsweb.parser.config.factory.ConfigFactory;
import com.sysunite.coinsweb.parser.config.pojo.ConfigFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author bastbijl, Sysunite 2017
 */
public class Describe {

  private static final Logger log = LoggerFactory.getLogger(Describe.class);

  public static String run(List<File> containers, boolean fullConfig, boolean useAbsolutePaths) {

    if(containers.isEmpty()) {
      throw new RuntimeException("No container file found");
    }

    Path localizeTo = null;
    if(!useAbsolutePaths) {
      localizeTo = CliOptions.resolvePath("");
    }

    ConfigFile configFile = ConfigFactory.getDefaultConfig(containers, localizeTo);
    String yml;
    if(fullConfig) {
      yml = ConfigFactory.toYml(configFile);
    } else {
      yml = ConfigFactory.toYml(configFile.getRun().getContainers());
    }
    return yml;
  }

  public static String run(Connector connector) {

    Map<String, Set<String>> map = connector.listSigmaGraphs();
    String sigmaYml = ConfigFactory.toYml(map);

    String imports = "";
    for(String context : map.keySet()) {
      for(String inclusion : map.get(context)) {
        if(inclusion.startsWith(QueryFactory.VALIDATOR_HOST)) {
          imports += inclusion + " owl:imports\n";
          Map<String, String> importsMap = connector.getImports(inclusion);
          for(String importContext : importsMap.keySet()) {
            imports += "- "+importContext + "("+importsMap.get(importContext)+")\n";
          }
        }
      }
    }

    List<Object> list = connector.listPhiGraphs();
    String phiYml = ConfigFactory.toYml(list);

    return sigmaYml + "\n" + imports + "\n" + phiYml;
  }

  public static String run(Connector connector, String context) {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    Map<String, String> imports = connector.exportPhiGraph(context, baos);
    try {
      return baos.toString("UTF-8") + "\nYou also need:\n"+"".join("\n", imports.keySet());
    } catch (UnsupportedEncodingException e) {
      return "";
    }
  }
}
