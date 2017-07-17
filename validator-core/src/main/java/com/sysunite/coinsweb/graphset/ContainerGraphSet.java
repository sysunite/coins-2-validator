package com.sysunite.coinsweb.graphset;

import com.sysunite.coinsweb.connector.Connector;
import com.sysunite.coinsweb.filemanager.ContainerFile;

import java.util.List;
import java.util.Map;

/**
 * A ContainerGraphSet is a collection of graphs (contexts) that is contained
 * in a .ccr file. These graphs are stored using a Connector.
 *
 * @author bastbijl, Sysunite 2017
 */
public interface ContainerGraphSet {


  List<Object> select(String query);


  void update(String query);

  List<String> getImports(GraphVar graphVar);
  boolean hasContext(GraphVar graphVar);

  void setContainerFile(ContainerFile container);
  void setContainerConfig(Object containerConfig);
  void setConfigFile(Object configFile);
  Map<GraphVar, String> contextMap();
  Map<String, Long> quadCount();

  void cleanup();


  void load();
  boolean requiresLoad();
  void setAllLoaded();

  String graphExists(GraphVar graphVar);

  Connector getConnector();
}
