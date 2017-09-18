package com.sysunite.coinsweb.connector;

import com.sysunite.coinsweb.graphset.GraphVar;
import com.sysunite.coinsweb.graphset.QueryFactory;
import com.sysunite.coinsweb.parser.config.pojo.Mapping;
import com.sysunite.coinsweb.parser.config.pojo.Source;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
import org.eclipse.rdf4j.query.*;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.rdfxml.util.RDFXMLPrettyWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Timestamp;
import java.util.*;
import java.util.function.Function;

import static com.sysunite.coinsweb.graphset.ContainerGraphSetFactory.fingerPrint;
import static com.sysunite.coinsweb.rdfutil.Utils.withoutHash;

/**
 * @author bastbijl, Sysunite 2017
 */
public abstract class Rdf4jConnector implements Connector {

  private static final Logger log = LoggerFactory.getLogger(Rdf4jConnector.class);
  protected Repository repository;
  protected boolean initialized = false;


  protected boolean cleanUp = false;
  protected boolean wipeOnClose = false;

  protected long max_update_limit = 10l;








  @Override
  public boolean testConnection() {
    try {
      if (!initialized) {
        init();
      }
      return repository.isWritable();
    } catch(RepositoryException e) {
      return false;
    }
  }

  @Override
  public List<Object> select(String queryString) {
    if(!initialized) {
      init();
    }

    try (RepositoryConnection con = repository.getConnection()) {

      log.trace(queryString.replace("\n", " "));

      TupleQuery tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
      tupleQuery.setIncludeInferred(false);

      List<Object> resultList;
      try (TupleQueryResult result = tupleQuery.evaluate()) {
        resultList = QueryResults.asList(result);
      }
      return resultList;
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      throw new RuntimeException("A problem with this select query (message: "+e.getLocalizedMessage()+")");
    }
  }

  @Override
  public List<Object> select(String queryString, long limit) {
    if(!initialized) {
      init();
    }

    try (RepositoryConnection con = repository.getConnection()) {

      log.trace(queryString.replace("\n", " "));

      TupleQuery tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
      tupleQuery.setIncludeInferred(false);

      List<Object> resultList = new ArrayList<>();
      try (TupleQueryResult result = tupleQuery.evaluate()) {
        int count = 0;
        while((count++<limit) && result.hasNext()) {
          resultList.add(result.next());
        }
      }
      return resultList;
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      throw new RuntimeException("A problem with this select query (message: "+e.getLocalizedMessage()+")");
    }
  }

  @Override
  public void update(String queryString) {
    if(!initialized) {
      init();
    }

    if(max_update_limit > 0l && queryString.contains("WHERE")) {
      queryString = queryString.replaceFirst("WHERE", "WHERE { SELECT * WHERE") ;
      queryString = queryString + " LIMIT " + max_update_limit + " }";
    }

    try (RepositoryConnection con = repository.getConnection()) {

      log.trace(queryString.replace("\n", " "));

//      con.begin(IsolationLevels.NONE);

      Update updateQuery = con.prepareUpdate(QueryLanguage.SPARQL, queryString);
      updateQuery.setIncludeInferred(false);
      updateQuery.execute();


    } catch (Exception e) {
      log.error(e.getMessage(), e);
      throw new RuntimeException("A problem with this update query");
    }
  }

  @Override
  public void sparqlCopy(String fromContext, String toContext) {
    update("COPY <"+fromContext+"> TO <"+toContext+">");
  }
  @Override
  public void sparqlAdd(String fromContext, String toContext) {
    update("ADD <"+fromContext+"> TO <"+toContext+">");
  }



//  @Override
//  public void booleanQuery(String queryString) {
//    if(!initialized) {
//      init();
//    }
//
//
//
//    try {
//      RepositoryConnection con = repository.getConnection();
//
//      log.trace(queryString.replace("\n", " "));
//
//      con.begin(IsolationLevels.NONE);
//
//      BooleanQuery booleanQuery = con.prepareBooleanQuery(QueryLanguage.SPARQL, queryString);
//      booleanQuery.setIncludeInferred(false);
//      booleanQuery.evaluate();
//
//
//
//      con.close();
//
//    } catch (Exception e) {
//      log.error(e.getMessage(), e);
//      throw new RuntimeException("A problem with this update query");
//    }
//  }

  @Override
  public void replaceResource(String context, String resource, String replace) {

    context = withoutHash(context);
    resource = withoutHash(resource);
    replace = withoutHash(replace);

    String subjectQuery =
    "WITH <"+context+"> "+
    "DELETE { <"+resource+"> ?p ?o } "+
    "INSERT { <"+replace+"> ?p ?o } "+
    "WHERE { <"+resource+"> ?p ?o  } ";
    update(subjectQuery);

    String objectQuery =
    "PREFIX val: <"+QueryFactory.VALIDATOR_NS+"> " +
    "WITH <"+context+"> "+
    "DELETE { ?s ?p <"+resource+"> } "+
    "INSERT { ?s ?p <"+replace+"> } "+
    "WHERE { ?s ?p <"+resource+">   . FILTER (?p != val:sourceContext) } ";
    update(objectQuery);

     subjectQuery =
    "WITH <"+context+"> "+
    "DELETE { <"+resource+"#> ?p ?o } "+
    "INSERT { <"+replace+"> ?p ?o } "+
    "WHERE {  <"+resource+"#> ?p ?o  } ";
    update(subjectQuery);

     objectQuery =
    "PREFIX val: <"+QueryFactory.VALIDATOR_NS+"> " +
    "WITH <"+context+"> "+
    "DELETE { ?s ?p <"+resource+"#> } "+
    "INSERT { ?s ?p <"+replace+"> } "+
    "WHERE { ?s ?p <"+resource+"#>  . FILTER (?p != val:sourceContext) } ";
    update(objectQuery);
  }

  @Override
  public void cleanup(List<String> contexts) {
    if(!initialized) {
      init();
    }
    if(repository != null) {
      if(cleanUp) {
        try (RepositoryConnection con = repository.getConnection()) {
          con.clear(Rdf4jConnector.asResource(contexts));
        } catch (RepositoryException e) {
          log.error(e.getMessage(), e);
        }
      }
    }
  }

  @Override
  public void close() {
    if(!initialized) {
      return;
    }
  }

  @Override
  public void uploadFile(File file, List<String> contexts) {
    if(!initialized) {
      init();
    }

    try (RepositoryConnection con = repository.getConnection()) {
      RDFFormat format = Rdf4jUtil.interpretFormat(file.toString());
      if(format == null) {
        throw new RuntimeException("Could not determine the type of file this is: " + file.getName());
      }



//      con.begin(IsolationLevels.NONE);
      con.add(file, null, format, asResource(contexts));

    } catch (RepositoryException e) {
      log.error(e.getMessage(), e);
    } catch (IOException e) {
      log.error(e.getMessage(), e);
    }
  }


  @Override
  public void uploadFile(InputStream inputStream, String fileName, String baseUri, ArrayList<String> contexts) {
    if(!initialized) {
      init();
    }

    try (RepositoryConnection con = repository.getConnection()) {
      RDFFormat format = Rdf4jUtil.interpretFormat(fileName);
      if(format == null) {
        throw new RuntimeException("Could not determine the type of file this is: " + fileName);
      }


//      con.begin(IsolationLevels.NONE);
      con.add(inputStream, baseUri, format, asResource(contexts));

    } catch (RepositoryException e) {
      log.error(e.getMessage(), e); // todo, this means a problem
    } catch (IOException e) {
      log.error(e.getMessage(), e);
    }
  }


  public List<Object> listPhiGraphs() {

    ArrayList<Object> list = new ArrayList<>();

    String query =

    "PREFIX val: <"+QueryFactory.VALIDATOR_NS+"> " +
    "SELECT ?context ?timestamp ?sourceId ?originalContext ?fileName ?hash " +
//    "FROM NAMED <"+context+"> " +
    "WHERE { graph ?context { " +
    "?context val:uploadedDate  ?timestamp . " +
    "?context val:sourceContext ?originalContext . " +
    "?context val:sourceFile    ?fileName . " +
    "?context val:sourceHash    ?hash . " +
    "}}";


    List<Object> result = select(query);
    for(Object rowObject :  result) {
      BindingSet row = (BindingSet) rowObject;

      String context         = row.getBinding("context").getValue().stringValue();
      String timestamp       = row.getBinding("timestamp").getValue().stringValue();
      String originalContext = row.getBinding("originalContext").getValue().stringValue();
      String fileName        = row.getBinding("fileName").getValue().stringValue();
      String hash            = row.getBinding("hash").getValue().stringValue();

      Source source = new Source();
      source.setGraphname(originalContext);
      source.setStoreContext(context);
      source.setHash(hash);
      source.setPath(fileName);
      list.add(source);
    }

    return list;
  }


  public Map<String, String> listFileNamePerPhiContext() {

    HashMap<String, String> map = new HashMap<>();

    String query =

    "PREFIX val: <"+QueryFactory.VALIDATOR_NS+"> " +
    "SELECT DISTINCT ?context ?fileName " +
    "WHERE { graph ?context { " +
    "?context val:sourceFile    ?fileName . " +
    "}}";


    List<Object> result = select(query);
    for(Object rowObject :  result) {
      BindingSet row = (BindingSet) rowObject;

      String context         = row.getBinding("context").getValue().stringValue();
      String fileName        = row.getBinding("fileName").getValue().stringValue();

      map.put(context, fileName);
    }

    return map;
  }


  public Map<String, Set<String>> listPhiContextsPerHash() {

    Map<String, Set<String>> list = new HashMap<>();

    String query =

    "PREFIX val: <"+QueryFactory.VALIDATOR_NS+"> " +
    "SELECT DISTINCT ?hash ?context " +
    "WHERE { graph ?context { " +
    "?context val:sourceHash ?hash . " +
    "}} ORDER BY ?hash";


    List<Object> result = select(query);

    String previousHash = "";
    HashSet<String> previousSet = null;
    for(Object rowObject :  result) {
      BindingSet row = (BindingSet) rowObject;

      String hash       = row.getBinding("hash").getValue().stringValue();
      String context    = row.getBinding("context").getValue().stringValue();

      if(!previousHash.equals(hash)) {
        if(previousSet != null) {
          list.put(previousHash, previousSet);
        }
        previousSet = new HashSet<>();
        previousHash =  hash;
      }
      previousSet.add(context);
    }
    if(previousSet != null) {
      list.put(previousHash, previousSet);
    }

    return list;
  }

  /**
   * List all sigma graphs
   * @return
   */
  public Set<String> listSigmaGraphs() {
    Set<String> set = new HashSet<>();

    String query =

    "PREFIX val: <"+QueryFactory.VALIDATOR_NS+"> " +
    "SELECT DISTINCT ?context ?inclusion " +
    "WHERE { graph ?context { " +
    "?context val:contains ?inclusion . " +
    "}} ORDER BY ?context";

    List<Object> result = select(query);


    for(Object rowObject : result) {
      BindingSet row = (BindingSet) rowObject;
      String context = row.getBinding("context").getValue().stringValue();

      if(!set.contains(context)) {
        set.add(context);
      }
    }

    return set;
  }
  public Map<Set<String>, Set<String>> listSigmaGraphsWithIncludes() {

    HashMap<String, Set<String>> list = new HashMap<>();

    String query =

    "PREFIX val: <"+QueryFactory.VALIDATOR_NS+"> " +
    "SELECT DISTINCT ?context ?inclusion " +
    "WHERE { graph ?context { " +
    "?context val:contains ?inclusion . " +
    "}} ORDER BY ?context";

    List<Object> result = select(query);

    String previousContext = "";
    HashSet<String> previousSet = new HashSet<>();
    for(Object rowObject : result) {
      BindingSet row = (BindingSet) rowObject;

      String context         = row.getBinding("context").getValue().stringValue();
      String inclusion       = row.getBinding("inclusion").getValue().stringValue();

      if(!previousContext.equals(context)) {

        // Skip start condition
        if(!previousContext.isEmpty()) {
          list.put(previousContext, previousSet);
        }
        previousSet = new HashSet<>();
        previousContext =  context;
      }
      previousSet.add(inclusion);
    }
    if(!previousSet.isEmpty()) {
      list.put(previousContext, previousSet);
    }


    HashMap<String, Set<String>> fingerPrintedContextList = new HashMap<>();
    HashMap<String, Set<String>> fingerPrintedInclusionsList = new HashMap<>();
    for(String context : list.keySet()) {

      Set<String> inclusions = list.get(context);
      String fingerPrint = fingerPrint(inclusions, "-");

      if(!fingerPrintedInclusionsList.containsKey(fingerPrint)) {
        fingerPrintedInclusionsList.put(fingerPrint, inclusions);
      }

      if(!fingerPrintedContextList.containsKey(fingerPrint)) {
        fingerPrintedContextList.put(fingerPrint, new HashSet<>());
      }
      Set<String> contexts = fingerPrintedContextList.get(fingerPrint);
      contexts.add(context);

    }

    HashMap<Set<String>, Set<String>> compressedList = new HashMap<>();
    for(String fingerPrint : fingerPrintedContextList.keySet()) {
      Set<String> contexts = fingerPrintedContextList.get(fingerPrint);
      Set<String> inclusions = fingerPrintedInclusionsList.get(fingerPrint);
      compressedList.put(contexts, inclusions);
    }

    return compressedList;
  }

  // The inferenceCode should have the "|" separator and have this format:
  // 1c73af7f95863c2f088053b1a1f5cc2d-57d50697b28694734038a545031e56eb-58fe7035dbea97e9d62619bf24a7fe73|COINS 2.0 Lite_0.9.85/0.9.85/coins container inference

  public Map<String, String> listSigmaGraphsByInferenceCode() {

    HashMap<String, String> set = new HashMap<>();

    String query =

    "PREFIX val: <"+QueryFactory.VALIDATOR_NS+"> " +
    "SELECT DISTINCT ?context ?inferenceCodeWithHashes " +
    "WHERE { graph ?context { " +
    "  ?context val:compositionFingerPrint ?hashes . " +
    "  ?context val:bundle ?inferenceCode . " +
    "  BIND ( concat(str(?hashes), \"|\", str(?inferenceCode)) as ?inferenceCodeWithHashes ) . " +
    "}} ORDER BY ?context";


    List<Object> result = select(query);

    for(Object rowObject :  result) {
      BindingSet row = (BindingSet) rowObject;

      String context = row.getBinding("context").getValue().stringValue();
      String inferenceCodeWithHashes = row.getBinding("inferenceCodeWithHashes").getValue().stringValue();
      set.put(inferenceCodeWithHashes, context);
      log.trace("Found fingerprint "+inferenceCodeWithHashes+" for "+context);
    }

    return set;
  }

  /**
   *
   * @param graphVars
   * @param inferenceCode
   */
  public void storeFinishedInferences(String hashes, Set<GraphVar> graphVars, Map<GraphVar, String> contextMap, String inferenceCode) {

    for(GraphVar graphVar : graphVars) {
      String context = contextMap.get(graphVar);
      log.info("Store for " + context);
      log.info(hashes + "|" + inferenceCode);

      String query =

      "PREFIX val: <" + QueryFactory.VALIDATOR_NS + "> " +
      "INSERT DATA { " +
      "  GRAPH <" + context + "> { " +
      "    <" + context + "> val:compositionFingerPrint \"" + hashes + "\" ." +
      "    <" + context + "> val:bundle \"" + inferenceCode + "\" . " +
      " }}";

      update(query);
    }
  }

  public Map<String, Set<String>> listInferenceCodePerSigmaGraph() {

    HashMap<String, Set<String>> list = new HashMap<>();

    String query =

    "PREFIX val: <"+QueryFactory.VALIDATOR_NS+"> " +
    "SELECT DISTINCT ?context ?inferenceCode ?fingerPrint " +
    "WHERE { graph ?context { " +
    "  ?context val:bundle ?inferenceCode . " +
    "  ?context val:compositionFingerPrint ?fingerPrint . " +
    "}} ORDER BY ?context";


    List<Object> result = select(query);

    String previousContext = "";
    HashSet<String> previousSet = new HashSet<>();
    for(Object rowObject :  result) {
      BindingSet row = (BindingSet) rowObject;

      String context         = row.getBinding("context").getValue().stringValue();
      String inference       = row.getBinding("inferenceCode").getValue().stringValue();
      String fingerPrint     = row.getBinding("fingerPrint").getValue().stringValue();

      if(!previousContext.equals(context)) {

        // Skip start condition
        if(!previousContext.isEmpty()) {
          list.put(previousContext, previousSet);
        }
        previousSet = new HashSet<>();
        previousContext =  context;
      }
      previousSet.add(fingerPrint + "|" + inference);
    }
    if(!previousSet.isEmpty()) {
      list.put(previousContext, previousSet);
    }

    return list;
  }

  // If value is null in originalContextsWithHash, treat this key as the import context
  public List<String> findPhiGraphWithImports(String hash, Map<String, String> originalContextsWithHash) {


    ArrayList<String> keySet = new ArrayList<>();
    keySet.addAll(originalContextsWithHash.keySet());

    String query =

    "PREFIX val: <"+QueryFactory.VALIDATOR_NS+"> " +
    "PREFIX owl: <http://www.w3.org/2002/07/owl#> " +
    "SELECT DISTINCT ?context " +
    "WHERE { " +
    "  graph ?context { ";
    for(int i = 0; i < originalContextsWithHash.size(); i++ ) {
      if(originalContextsWithHash.get(keySet.get(i)) == null) {
        query += " ?context owl:imports <"+keySet.get(i)+"> . ";
      } else {
        query += " ?context owl:imports ?library_" + i + " . ";
      }
    }
    query +=
    "    ?context val:sourceHash    \""+hash+"\" . " +
    "  } ";

    for(int i = 0; i < originalContextsWithHash.size(); i++ ) {
      if (originalContextsWithHash.get(keySet.get(i)) != null) {
        query +=
        "  graph ?library_" + i + " { " +
        "    ?library val:sourceContext ?original . " +
        "    ?library val:sourceHash    \"" + originalContextsWithHash.get(keySet.get(i)) + "\" . " +
        "    FILTER(?original = <" + keySet.get(i) + "> || ?original = <" + keySet.get(i) + "#>) . " +
        "  } ";
      }
    }
    query +=
    "}";

    HashSet<String> contextsMap = new HashSet<>();
    List<Object> result = select(query);
    for (Object bindingSet : result) {

      String namespace = withoutHash(((BindingSet)bindingSet).getBinding("context").getValue().stringValue());
      contextsMap.add(namespace);
    }
    ArrayList<String> contexts = new ArrayList<>();
    contexts.addAll(contextsMap);
    return contexts;
  }

  public List<Object> listMappings() {
    HashMap<String, Mapping> resultMap = new HashMap<>();

    Map<Set<String>, Set<String>> sigmaGraphs = listSigmaGraphsWithIncludes();
    for(Set<String> sigmaUris : sigmaGraphs.keySet()) {
      for(String sigmaUri : sigmaUris) {
        Mapping mapping = new Mapping(null, sigmaUri);
        mapping.setInitialized();
        mapping.setInclusionSet(sigmaGraphs.get(sigmaUri));


        resultMap.put(sigmaUri, mapping);
      }
    }


    Map<String, Set<String>> inferenceMap = listInferenceCodePerSigmaGraph();
    for(String sigmaUri : inferenceMap.keySet()) {
      Mapping mapping = resultMap.get(sigmaUri);
      mapping.setBundleFingerPrints(inferenceMap.get(sigmaUri));
    }

    ArrayList<Object> result = new ArrayList<>();
    result.addAll(resultMap.values());
    return result;
  }

  public void storePhiGraphExists(Object sourceObject, String context, String fileName, String hash) {

    Source source = (Source) sourceObject;

    String timestamp = new Timestamp(System.currentTimeMillis()).toString();

    log.info("Store phi graph exists "+timestamp+" for "+context);

    String query =

    "PREFIX val: <"+QueryFactory.VALIDATOR_NS+"> " +
    "INSERT DATA { GRAPH <"+context+"> { " +
    "<"+context+"> val:uploadedDate  \""+timestamp+"\" . " +
    "<"+context+"> val:sourceContext <"+source.getGraphname()+"> . " +
    "<"+context+"> val:sourceFile    \""+fileName+"\" . " +
    "<"+context+"> val:sourceHash    \""+hash+"\" . " +
    "}}";

    update(query);
  }

  public void storeSigmaGraphExists(String context, Set<String> inclusionSet) {

    String timestamp = new Timestamp(System.currentTimeMillis()).toString();

    log.info("Store sigma graph exists "+timestamp+" for "+context);

    String query =

    "PREFIX val: <"+QueryFactory.VALIDATOR_NS+"> " +
    "INSERT DATA { GRAPH <"+context+"> { ";
    for(String inclusion : inclusionSet) {
      query += "<" + context + "> val:contains  <" + inclusion + "> . ";
    }
    query +=
    "}}";

    update(query);
  }

  @Override
  public long quadCount(String context) {
    if(!initialized) {
      init();
    }

    try (RepositoryConnection con = repository.getConnection()) {
      return con.size(asResource(context));
    } catch (RepositoryException e) {
      log.error(e.getMessage(), e);
    }
    return 0l;
  }



  @Override
  public List<String> getContexts() {
    if(!initialized) {
      init();
    }
    log.info("Connector contains these contexts (that might be used by GraphSets):");
    ArrayList<String> contexts = new ArrayList();
    try (RepositoryConnection con = repository.getConnection()) {
      RepositoryResult<Resource> graphIterator = con.getContextIDs();
      while(graphIterator.hasNext()) {
        Resource graphName = graphIterator.next();
        log.info("- " + graphName.toString());
        contexts.add(graphName.toString());
      }
    } catch (RepositoryException e) {
      log.error(e.getMessage(), e);
    }
    return contexts;
  }

  public static Resource asResource(String context) {
    ValueFactory factory = SimpleValueFactory.getInstance();
    return factory.createIRI(context);
  }
  public static Resource[] asResource(List<String> contexts) {
    ValueFactory factory = SimpleValueFactory.getInstance();
    Resource[] contextsIRI = new Resource[contexts.size()];
    for(int i = 0; i < contexts.size(); i++) {
      contextsIRI[i] = factory.createIRI(contexts.get(i));
    }
    return contextsIRI;
  }


  @Override
  public void writeContextsToFile(List<String> contexts, OutputStream outputStream, Map<String, String> prefixMap, String mainContext) {
    Function<Statement, Statement> filter = s->s;
    writeContextsToFile(contexts, outputStream, prefixMap, mainContext, filter);
  }
  @Override
  public void writeContextsToFile(List<String> contexts, OutputStream outputStream, Map<String, String> prefixMap, String mainContext, Function statementFilter) {

    Function<Statement, Statement> filter = (Function<Statement, Statement>) statementFilter;

    RDFXMLPrettyWriter writer = new RDFXMLPrettyWriter(outputStream);

    writer.handleNamespace("", mainContext);
    for(String prefix : prefixMap.keySet()) {
      writer.handleNamespace(prefix, prefixMap.get(prefix));
    }


    try (RepositoryConnection con = repository.getConnection();
         RepositoryResult<Statement> statements = con.getStatements(null, null, null, false, asResource(contexts))) {

      writer.startRDF();
      while (statements.hasNext()) {
        Statement statement = filter.apply(statements.next());
        if(statement != null) {
          writer.handleStatement(statement);
        }
      }
      writer.endRDF();
    } catch (RepositoryException e) {
      log.error(e.getMessage(), e);
    }
  }

  // Writes the file to the outputStream and returns a Map of imports: fileUpload context -> original context
  @Override
  public Map<String, String> exportPhiGraph(String context, OutputStream outputStream) {

    String sourceContext = null;
    Resource contextResource = asResource(context);
    try (RepositoryConnection con = repository.getConnection();
         RepositoryResult<Statement> statements = con.getStatements(contextResource, (IRI)asResource(QueryFactory.VALIDATOR_NS+"sourceContext"), null, contextResource)) {
      if(!statements.hasNext()) {
        throw new RuntimeException("Source context could not be read from the connector");
      }
      sourceContext = statements.next().getObject().stringValue();
    } catch (RepositoryException e) {
      log.error(e.getMessage(), e);
    }

    Map<String, String> importsMap = getImports(context);
    List<String> uploadedFileContexts = new ArrayList<>();
    uploadedFileContexts.addAll(importsMap.keySet());
    final Map<String, String> contextMap = new HashMap<>();
    if(sourceContext != null) {
      contextMap.put(context, sourceContext);
    }

    Map<String, String> prefixMap = new HashMap<>();
    for(int i = 1; i <= uploadedFileContexts.size(); i++) {
      String uploadedFileContext = uploadedFileContexts.get(i-1);
      String originalContext = importsMap.get(uploadedFileContext);
      prefixMap.put("lib"+i, originalContext);
      contextMap.put(uploadedFileContext, originalContext);
    }
    prefixMap.put("cbim", "http://www.coinsweb.nl/cbim-2.0.rdf#");
    prefixMap.put(RDFS.PREFIX, RDFS.NAMESPACE);
    prefixMap.put(XMLSchema.PREFIX, XMLSchema.NAMESPACE);
    prefixMap.put(OWL.PREFIX, OWL.NAMESPACE);

    // Now add the main context itself
    Function<Statement, Statement> filter = statement -> {
      if(statement.getPredicate().getNamespace().equals(QueryFactory.VALIDATOR_NS)) {
        return null;
      }

      for(String uploadedFileContext : contextMap.keySet()) {
        String originalContext = contextMap.get(uploadedFileContext);
        if (statement.getSubject().equals(asResource(uploadedFileContext))) {
          ValueFactory factory = SimpleValueFactory.getInstance();
          statement = factory.createStatement(asResource(originalContext), statement.getPredicate(), statement.getObject());
        }
        if (statement.getObject().equals(asResource(uploadedFileContext))) {
          ValueFactory factory = SimpleValueFactory.getInstance();
          statement = factory.createStatement(statement.getSubject(), statement.getPredicate(), asResource(originalContext));
        }
      }
      return statement;
    };

    ArrayList<String> contexts = new ArrayList<>();
    contexts.add(context);

    writeContextsToFile(contexts, outputStream, prefixMap, sourceContext, filter);
    return importsMap;
  }

  // Get the contexts that are imported in this context, the value is the original uri
  public Map<String, String> getImports(String context) {

    log.info("Look for imports in context "+context);

    String query =

    "PREFIX val: <"+QueryFactory.VALIDATOR_NS+"> " +
    "PREFIX owl: <http://www.w3.org/2002/07/owl#> " +
    "SELECT ?library ?original " +
    "WHERE { " +
    "  graph <"+context+"> { " +
    "    ?s owl:imports ?library . " +
    "  } " +
    "  graph ?library { " +
    "    ?library val:sourceContext ?original . " +
    "  } " +
    "}";

    Map<String, String> namespaces = new HashMap<>();
    List<Object> result = select(query);
    for (Object bindingSet : result) {

      String namespace = ((BindingSet)bindingSet).getBinding("library").getValue().stringValue();
      String original = ((BindingSet)bindingSet).getBinding("original").getValue().stringValue();
      log.info("Found import: " + namespace + " ("+original+")");
      namespaces.put(namespace, original);
    }
    return namespaces;
  }
}
