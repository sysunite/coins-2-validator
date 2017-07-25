package com.sysunite.coinsweb.connector;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Each graphSet needs a connector to connect to some graph database. A connector
 * gives access to the data of more than one graphSet.
 *
 * @author bastbijl, Sysunite 2017
 */
public interface Connector {

  void init();
  boolean testConnection();
  void cleanup(String[] contexts);
  void close();

  List<Object> query(String queryString);
  void update(String queryString);
  void sparqlCopy(String fromContext, String toContext);
  void sparqlAdd(String fromContext, String toContext);


  void uploadFile(File file, String[] contexts);
  void uploadFile(InputStream inputStream, String fileName, String baseUri, ArrayList<String> contexts);
  void storeGraphExists(String context, String originalContext);

  long quadCount(String context);
  List<String> getContexts();

  String graphExists(String context);

  void writeContextsToFile(String[] contexts, OutputStream outputStream);
}
