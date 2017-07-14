package com.sysunite.coinsweb.graphset;

import com.sysunite.coinsweb.filemanager.ContainerFile;

import java.util.List;
import java.util.Map;

/**
 * @author bastbijl, Sysunite 2017
 */
public interface ContainerGraphSet {

  @Deprecated // this is too specific for containergraphset
  boolean select(String query, Object formatTemplate, Object result);
  List<Object> select(String query);

  @Deprecated // this is too specific for containergraphset
  void update(String query, Object result);
  void update(String query);

  List<String> getImports(GraphVar graphVar);
  boolean hasContext(GraphVar graphVar);

  void setContainerFile(ContainerFile container);
  void setContainerConfig(Object containerConfig);
  void setConfigFile(Object configFile);
  Map<GraphVar, String> contextMap();
  Map<String, Long> quadCount();

  void cleanup();



  boolean requiresLoad();
  void setAllLoaded();

  String graphExists(GraphVar graphVar);
}
