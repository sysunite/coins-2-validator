package com.sysunite.coinsweb.filemanager;

import com.sysunite.coinsweb.connector.Connector;
import com.sysunite.coinsweb.parser.config.factory.ConfigFactory;
import com.sysunite.coinsweb.parser.config.pojo.ConfigFile;
import com.sysunite.coinsweb.parser.config.pojo.StepDeserializer;
import com.sysunite.coinsweb.parser.config.pojo.Store;
import com.sysunite.coinsweb.steps.ValidationStep;
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
    Store.factory = new ConnectorFactoryStub();
    StepDeserializer.factory = new StepFactoryStub();

    ConfigFactory.setDescribeFactory(new DescribeFactoryImpl());
  }

  @Test
  public void testMinimalContainer() {

    ConfigFile configFile = ConfigFile.parse(new File(getClass().getClassLoader().getResource("config/minimal-container.yml").getFile()));
    String yml = ConfigFactory.toYml(configFile);
    System.out.println(yml);

    DescribeFactoryImpl.expandGraphConfig(configFile);
    String ymlExpanded = ConfigFactory.toYml(configFile);
    System.out.println(ymlExpanded);
  }

  @Test
  public void testMinimalVirtual() {

    ConfigFile configFile = ConfigFile.parse(new File(getClass().getClassLoader().getResource("config/minimal-virtual.yml").getFile()));
    String yml = ConfigFactory.toYml(configFile);
    System.out.println(yml);

    DescribeFactoryImpl.expandGraphConfig(configFile);
    String ymlExpanded = ConfigFactory.toYml(configFile);
    System.out.println(ymlExpanded);
  }

  @Test
  public void testVirtualExpandingWildcards() {

    ConfigFile configFile = ConfigFile.parse(new File(getClass().getClassLoader().getResource("config/virtual-expanding-wildcards.yml").getFile()));
    String yml = ConfigFactory.toYml(configFile);
    System.out.println(yml);

    DescribeFactoryImpl.expandGraphConfig(configFile);
    String ymlExpanded = ConfigFactory.toYml(configFile);
    System.out.println(ymlExpanded);
  }

  private static class ConnectorFactoryStub implements com.sysunite.coinsweb.connector.ConnectorFactory {
    @Override
    public boolean exists(String key) {
      return true;
    }

    @Override
    public Class<? extends Connector> get(String key) {
      return null;
    }

    @Override
    public Connector build(Object config) {
      return null;
    }
  }

  private static class StepFactoryStub implements com.sysunite.coinsweb.steps.StepFactory {
    @Override
    public boolean exists(String key) {
      return true;
    }

    @Override
    public ValidationStep[] getDefaultSteps() {
      return null;
    }

    @Override
    public Class<? extends ValidationStep> get(String key) {
      return null;
    }
  }
}