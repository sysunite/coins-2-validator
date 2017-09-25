package com.sysunite.coinsweb.connector;

import com.sysunite.coinsweb.graphset.GraphVar;

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
  void wipe() throws ConnectorException;
  void cleanup(List<String> contexts) throws ConnectorException;
  void close();

  List<Object> select(String queryString) throws ConnectorException;
  List<Object> select(String queryString, long limit) throws ConnectorException;
  void update(String queryString) throws ConnectorException;
  void sparqlCopy(String fromContext, String toContext) throws ConnectorException;
  void sparqlAdd(String fromContext, String toContext) throws ConnectorException;
  void replaceResource(String context, String resource, String replace) throws ConnectorException;


  void uploadFile(File file, List<String> contexts) throws ConnectorException;
  void uploadFile(InputStream inputStream, String fileName, String baseUri, ArrayList<String> contexts) throws ConnectorException;

  void storePhiGraphExists(Object source, String context, String fileName, String hash) throws ConnectorException;
  void storeSigmaGraphExists(String context, Set<String> inclusionSet) throws ConnectorException;
  void storeFinishedInferences(String compositionFingerPrint, Set<GraphVar> graphVars, Map<GraphVar, String> contextMap, String inferenceCode) throws ConnectorException;

  Map<String, String> exportPhiGraph(String contexts, OutputStream outputStream) throws ConnectorException;

  long quadCount(String context);
  List<String> getContexts();

//  String graphExists(String context);

  List<Object> listPhiGraphs() throws ConnectorException;
  Map<String, Set<String>> listPhiContextsPerHash() throws ConnectorException;
  Map<String, String> listFileNamePerPhiContext() throws ConnectorException;
  Set<String> listSigmaGraphs() throws ConnectorException;
  Map<Set<String>, Set<String>> listSigmaGraphsWithIncludes() throws ConnectorException;
  Map<String, Set<String>> listInferenceCodePerSigmaGraph() throws ConnectorException;
  Map<String, String> listSigmaGraphsByInferenceCode() throws ConnectorException;
  List<Object> listMappings() throws ConnectorException;

  List<String> findPhiGraphWithImports(String hash, Map<String, String> originalContextsWithHash) throws ConnectorException;

  void writeContextsToFile(List<String> contexts, OutputStream outputStream, Map<String, String> prefixMap, String mainContext);
  void writeContextsToFile(List<String> contexts, OutputStream outputStream, Map<String, String> prefixMap, String mainContext, Function filter);

  Map<String, String> getImports(String context) throws ConnectorException;
}
