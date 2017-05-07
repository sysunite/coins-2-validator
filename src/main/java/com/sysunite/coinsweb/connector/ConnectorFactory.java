package com.sysunite.coinsweb.connector;

import com.sysunite.coinsweb.connector.graphdb.GraphDB;
import com.sysunite.coinsweb.connector.inmem.InMemRdf4j;
import com.sysunite.coinsweb.parser.config.Store;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author bastbijl, Sysunite 2017
 */
public class ConnectorFactory {

  private static final Map<String, Class<? extends Connector>> register;
  static
  {
    register = new HashMap();
    register.put("graphdb", GraphDB.class);
    register.put("rdf4j-sail-memory", InMemRdf4j.class);
  }

  public static boolean exists(String key) {
    return register.containsKey(key);
  }
  public static Class<? extends Connector> get(String key) {
    return register.get(key);
  }
  public static Connector build(Store config) {
    Class<? extends Connector> clazz = get(config.getType());
    try {
      return clazz.getConstructor(Store.class).newInstance(config);
    } catch (InstantiationException e) {
      e.printStackTrace();
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    } catch (InvocationTargetException e) {
      e.printStackTrace();
    } catch (NoSuchMethodException e) {
      e.printStackTrace();
    }
    throw new RuntimeException("Was not able to construct connector");
  }
}
