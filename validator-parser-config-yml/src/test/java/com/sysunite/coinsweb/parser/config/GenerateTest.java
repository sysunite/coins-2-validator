package com.sysunite.coinsweb.parser.config;

import com.sysunite.coinsweb.connector.Connector;
import com.sysunite.coinsweb.filemanager.ContainerFile;
import com.sysunite.coinsweb.filemanager.ContainerFileImpl;
import com.sysunite.coinsweb.parser.config.factory.ConfigFactory;
import com.sysunite.coinsweb.parser.config.pojo.ConfigFile;
import com.sysunite.coinsweb.parser.config.pojo.Step;
import com.sysunite.coinsweb.parser.config.pojo.Store;
import com.sysunite.coinsweb.steps.ValidationStep;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;


/**
 * @author bastbijl, Sysunite 2017
 */
public class GenerateTest {

  Logger log = LoggerFactory.getLogger(GenerateTest.class);

  @BeforeClass
  public static void before() {
    Store.factory = new ConnectorFactoryStub();
    Step.factory = new StepFactoryStub();
  }

  @Test
  public void testMinimalContainer() {

    ArrayList<ContainerFile> containers = new ArrayList();
    containers.add(new ContainerFileImpl(getClass().getClassLoader().getResource("some.ccr").getFile()));

    ConfigFile configFile = ConfigFactory.getDefaultConfig(containers);
    String yml = ConfigFactory.getDefaultConfigString(configFile);
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
    public Class<? extends ValidationStep> get(String key) {
      return null;
    }

    @Override
    public ValidationStep getValidationStep() {
      return null;
    }
  }
}