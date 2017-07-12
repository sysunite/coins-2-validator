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
  Object select(String query);

  @Deprecated // this is too specific for containergraphset
  void update(String query, Object result);

  List<String> getImports(String graphVar);
  boolean hasContext(String graphVar);

  void setContainerFile(ContainerFile container);
  void setContainerConfig(Object containerConfig);
  void setConfigFile(Object configFile);
  Map<String, String> contextMap();
  Map<String, Long> quadCount();

  void close();

}
