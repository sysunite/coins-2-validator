package com.sysunite.coinsweb.filemanager;

import com.sysunite.coinsweb.connector.ConnectorFactoryImpl;
import com.sysunite.coinsweb.parser.config.factory.ConfigFactory;
import com.sysunite.coinsweb.parser.config.pojo.ConfigFile;
import com.sysunite.coinsweb.parser.config.pojo.StepDeserializer;
import com.sysunite.coinsweb.parser.config.pojo.Store;
import com.sysunite.coinsweb.steps.StepFactoryImpl;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * @author bastbijl, Sysunite 2017
 */
public class ConfigFileTest {
  Logger log = LoggerFactory.getLogger(ConfigFileTest.class);

  @BeforeClass
  public static void before() {
    Store.factory = new ConnectorFactoryImpl();
    StepDeserializer.factory = new StepFactoryImpl();

    ConfigFactory.setDescribeFactory(new DescribeFactoryImpl());
  }

  @Test
  public void testMinimalContainer() {
    ConfigFile configFile = ConfigFile.parse(new File(getClass().getClassLoader().getResource("minimal-container.yml").getFile()));
    String yml = ConfigFactory.toYml(configFile);
    System.out.println(yml);

    DescribeFactoryImpl.expandGraphConfig(configFile);
    String ymlExpanded = ConfigFactory.toYml(configFile);
    System.out.println(ymlExpanded);
  }

  @Test
  public void testMinimalVirtual() {
    ConfigFile configFile = ConfigFile.parse(new File(getClass().getClassLoader().getResource("minimal-virtual.yml").getFile()));
    String yml = ConfigFactory.toYml(configFile);
    System.out.println(yml);

    DescribeFactoryImpl.expandGraphConfig(configFile);
    String ymlExpanded = ConfigFactory.toYml(configFile);
    System.out.println(ymlExpanded);
  }

  @Test
  public void testVirtualExpandingWildcards() {
    ConfigFile configFile = ConfigFile.parse(new File(getClass().getClassLoader().getResource("virtual-expanding-wildcards.yml").getFile()));
    String yml = ConfigFactory.toYml(configFile);
    System.out.println(yml);

    DescribeFactoryImpl.expandGraphConfig(configFile);
    String ymlExpanded = ConfigFactory.toYml(configFile);
    System.out.println(ymlExpanded);
  }
}
