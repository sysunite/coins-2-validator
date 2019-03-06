package com.sysunite.coinsweb.parser.config;

import application.run.HostFiles;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.sysunite.coinsweb.connector.Connector;
import com.sysunite.coinsweb.connector.ConnectorFactoryImpl;
import com.sysunite.coinsweb.connector.graphdb.GraphDB;
import com.sysunite.coinsweb.parser.config.factory.ConfigFactory;
import com.sysunite.coinsweb.parser.config.pojo.ConfigFile;
import com.sysunite.coinsweb.parser.config.pojo.StepDeserializer;
import com.sysunite.coinsweb.parser.config.pojo.Store;
import com.sysunite.coinsweb.report.ReportFactory;
import com.sysunite.coinsweb.steps.StepFactoryImpl;
import com.sysunite.coinsweb.steps.ValidationStep;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * @author bastbijl, Sysunite 2017
 */
public class ConfigFileTest extends HostFiles {
  Logger log = LoggerFactory.getLogger(ConfigFileTest.class);

  @BeforeClass
  public static void before() {
    Store.factory = new ConnectorFactoryImpl();
    StepDeserializer.factory = new StepFactoryImpl();
  }

  @Test
  public void testContainer() {
    ConfigFile configFile = ConfigFile.parse(new File(getClass().getClassLoader().getResource("general-9.85.yml").getFile()));
    String yml = ConfigFactory.toYml(configFile);
    System.out.println(yml);
  }

  @Test
  public void testMinimalContainer() {
    ConfigFile configFile = ConfigFile.parse(new File(getClass().getClassLoader().getResource("minimal-container.yml").getFile()));
    String yml = ConfigFactory.toYml(configFile);
    System.out.println(yml);
  }

  @Test
  public void testMinimalVirtual() {
    ConfigFile configFile = ConfigFile.parse(new File(getClass().getClassLoader().getResource("minimal-virtual.yml").getFile()));
    String yml = ConfigFactory.toYml(configFile);
    System.out.println(yml);
  }

  @Test
  public void testVirtualExpandingWildcards() {
    ConfigFile configFile = ConfigFile.parse(new File(getClass().getClassLoader().getResource("virtual-expanding-wildcards.yml").getFile()));
    String yml = ConfigFactory.toYml(configFile);
    System.out.println(yml);
  }
}
