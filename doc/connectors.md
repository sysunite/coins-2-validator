## Connectors

Two connectors are build in. The GraphDB connector and an in-mem connector. More connectors can be made by implementing the ```Connector``` interface.

The connector must return Rdf4j objects, so it is required the database being conntected has Rdf4j libraries.

The easiest way is to extend the Rdf4jConnector and read the GraphDB class as example.

### Connector Inferface
Function 

```java
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

  void uploadFile(InputStream inputStream, String fileName, String baseUri, ArrayList<String> contexts) throws ConnectorException;

  void storePhiGraphExists(Object source, String context, String fileName, String hash) throws ConnectorException;
  void storeSigmaGraphExists(String context, Set<String> inclusionSet) throws ConnectorException;
  void storeFinishedInferences(String compositionFingerPrint, Set<GraphVar> graphVars, Map<GraphVar, String> contextMap, String inferenceCode) throws ConnectorException;

  Map<String, String> exportPhiGraph(String contexts, OutputStream outputStream) throws ConnectorException;

  long quadCount(String context);
  List<String> getContexts();

  List<Object> listPhiGraphs() throws ConnectorException;
  Map<String, Set<String>> listPhiContextsPerHash() throws ConnectorException;

  Map<Set<String>, Set<String>> listSigmaGraphsWithIncludes() throws ConnectorException;
  Map<String, Set<String>> listInferenceCodePerSigmaGraph() throws ConnectorException;

  List<Object> listMappings() throws ConnectorException;

  void writeContextsToFile(List<String> contexts, OutputStream outputStream, Map<String, String> prefixMap, String mainContext);
  void writeContextsToFile(List<String> contexts, OutputStream outputStream, Map<String, String> prefixMap, String mainContext, Function filter);

  Map<String, String> getImports(String context) throws ConnectorException;
}
```

