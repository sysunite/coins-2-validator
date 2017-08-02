package com.sysunite.coinsweb.connector;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * Each graphSet needs a connector to connect to some graph database. A connector
 * gives access to the data of more than one graphSet.
 *
 * @author bastbijl, Sysunite 2017
 */
public interface Connector {

  void init();
  boolean testConnection();
  void cleanup(List<String> contexts);
  void close();

  List<Object> select(String queryString);
  void update(String queryString);
  void sparqlCopy(String fromContext, String toContext);
  void sparqlAdd(String fromContext, String toContext);
  void replaceResource(String context, String resource, String replace);


  void uploadFile(File file, List<String> contexts);
  void uploadFile(InputStream inputStream, String fileName, String baseUri, ArrayList<String> contexts);

  void storePhiGraphExists(Object source, String context, String fileName, String hash);
  void storeSigmaGraphExists(String context, Set<String> inclusionSet);

  Map<String, String> exportPhiGraph(String contexts, OutputStream outputStream);

  long quadCount(String context);
  List<String> getContexts();

//  String graphExists(String context);

  List<Object> listPhiGraphs();
  Map<String, Set<String>> listPhiSourceIdsPerHash();
  Map<String, Set<String>> listSigmaGraphs();
  Map<String, Set<String>> listInferencesPerSigmaGraph();

  void writeContextsToFile(List<String> contexts, OutputStream outputStream, Map<String, String> prefixMap, String mainContext);
  void writeContextsToFile(List<String> contexts, OutputStream outputStream, Map<String, String> prefixMap, String mainContext, Function filter);

  Map<String, String> getImports(String context);
}
