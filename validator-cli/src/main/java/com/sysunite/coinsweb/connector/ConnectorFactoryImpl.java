package com.sysunite.coinsweb.connector;

import com.sysunite.coinsweb.connector.graphdb.GraphDB;
import com.sysunite.coinsweb.connector.inmem.InMemRdf4j;
import com.sysunite.coinsweb.parser.config.Store;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    register.put("graphdb", GraphDB.class);
    register.put("rdf4j-sail-memory", InMemRdf4j.class);
  }

  public boolean exists(String key) {
    return register.containsKey(key);
  }
  public Class<? extends Connector> get(String key) {
    return register.get(key);
  }

  public Connector build(Object config) {
    if(!(config instanceof Store)) {
      throw new RuntimeException("Config item for connection should be a Store instance.");
    }
    Class<? extends Connector> clazz = get(((Store)config).getType());
    try {
      return clazz.getConstructor(Store.class).newInstance(config);
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
