package com.sysunite.coinsweb.parser.config;

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
  }

  @Test
  public void test981Container() {
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