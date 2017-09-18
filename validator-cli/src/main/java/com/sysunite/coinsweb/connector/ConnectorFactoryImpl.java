package com.sysunite.coinsweb.connector;

import com.sysunite.coinsweb.connector.graphdb.GraphDB;
import com.sysunite.coinsweb.connector.inmem.InMemRdf4j;
import com.sysunite.coinsweb.connector.stardog.Stardog;
import com.sysunite.coinsweb.connector.virtuoso.Virtuoso;
import com.sysunite.coinsweb.parser.config.pojo.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author bastbijl, Sysunite 2017
 */
public class ConnectorFactoryImpl implements ConnectorFactory {

  private static final Logger log = LoggerFactory.getLogger(ConnectorFactoryImpl.class);

  private static final Map<String, Class<? extends Connector>> register;
  static
  {
    register = new HashMap();
    register.put(Stardog.REFERENCE, Stardog.class);
    register.put(GraphDB.REFERENCE, GraphDB.class);
    register.put(InMemRdf4j.REFERENCE, InMemRdf4j.class);
    register.put(Virtuoso.REFERENCE, Virtuoso.class);
  }

  public boolean exists(String key) {
    return register.containsKey(key);
  }
  public Class<? extends Connector> get(String key) {
    return register.get(key);
  }

  public Connector build(Object config) {
    if(!(config instanceof Environment)) {
      throw new RuntimeException("Config item for connection should be a Environment instance.");
    }
    try {
      String key = ((Environment)config).getStore().getType();
      log.info("Try to get connector with key: "+key);
      Class<? extends Connector> clazz = get(key);
      Constructor<? extends Connector> constructor = clazz.getConstructor(Environment.class);
      Connector instance = constructor.newInstance((Environment)config);
      return instance;
    } catch (InstantiationException e) {
      log.error(e.getMessage(), e);
    } catch (IllegalAccessException e) {
      log.error(e.getMessage(), e);
    } catch (InvocationTargetException e) {
      log.error(e.getMessage(), e);
    } catch (NoSuchMethodException e) {
      log.error(e.getMessage(), e);
    }
    throw new RuntimeException("Was not able to construct connector");
  }
}
