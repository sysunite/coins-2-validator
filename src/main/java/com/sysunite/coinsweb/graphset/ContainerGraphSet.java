package com.sysunite.coinsweb.graphset;

import com.sysunite.coinsweb.connector.Connector;
import com.sysunite.coinsweb.connector.ConnectorException;
import com.sysunite.coinsweb.filemanager.ContainerFile;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A ContainerGraphSet is a collection of graphs (contexts) that is contained
 * in a .ccr file. These graphs are stored using a Connector.
 *
 * @author bastbijl, Sysunite 2017
 */
public interface ContainerGraphSet {

  Connector getConnector();

  List<Object> select(String query) throws ConnectorException;
  List<Object> select(String query, long limit) throws ConnectorException;
  boolean update(String query);

  Map<String, String> getImports(GraphVar graphVar);
  boolean hasContext(GraphVar graphVar);

  void lazyLoad(ContainerFile container, Map<String, Set<GraphVar>> inferencePreference);
  Map<GraphVar, String> contextMap();
  Map<GraphVar, Long> quadCount();

  void cleanup();

  void load();
  boolean requiresLoad();
  void setFailed();
  boolean loadingFailed();

  Object getMain();
  void setMain(Object graphVar);

  void pushUpdatesToCompose();

  String getCompositionFingerPrint(Set<GraphVar> graphVars);
}
