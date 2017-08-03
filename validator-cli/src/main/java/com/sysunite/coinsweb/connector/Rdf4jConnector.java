package com.sysunite.coinsweb.connector;

import com.sysunite.coinsweb.graphset.QueryFactory;
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
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.rdfxml.util.RDFXMLPrettyWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.sql.Timestamp;
import java.util.*;
import java.util.function.Function;

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

      TupleQuery tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
      tupleQuery.setIncludeInferred(false);

      List<Object> resultList;
      try (TupleQueryResult result = tupleQuery.evaluate()) {
        resultList = QueryResults.asList(result);
      }
      return resultList;
    } catch (Exception e) {
      log.error("A problem with this select query (message: "+e.getLocalizedMessage()+"):\n"+queryString);
    }
    throw new RuntimeException("Was not able to build resultset for query: "+queryString);
  }

  @Override
  public void update(String queryString) {
    if(!initialized) {
      init();
    }

    try (RepositoryConnection con = repository.getConnection()) {

      Update updateQuery = con.prepareUpdate(QueryLanguage.SPARQL, queryString);
      updateQuery.setIncludeInferred(false);


      try {
        updateQuery.execute();
      } catch (Exception e) {
        log.error("A problem with this update query (message: "+e.getLocalizedMessage()+"):\n"+queryString);
      }
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }
  }

  @Override
  public void sparqlCopy(String fromContext, String toContext) {
    update("COPY <"+fromContext+"> TO <"+toContext+">");
//    storeGraphExists(toContext, fromContext);
  }
  @Override
  public void sparqlAdd(String fromContext, String toContext) {
    update("ADD <"+fromContext+"> TO <"+toContext+">");
  }

  @Override
  public void replaceResource(String context, String resource, String replace) {

    context = withoutHash(context);
    resource = withoutHash(resource);
    replace = withoutHash(replace);

    update("WITH <"+context+"> "+
    "DELETE { <"+resource+"> ?p ?o } "+
    "INSERT { <"+replace+"> ?p ?o } "+
    "WHERE { <"+resource+"> ?p ?o } ");

    update("WITH <"+context+"> "+
    "DELETE { ?s ?p <"+resource+"> } "+
    "INSERT { ?s ?p <"+replace+"> } "+
    "WHERE { ?s ?p <"+resource+"> } ");
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
      Optional<RDFFormat> format = Rio.getParserFormatForFileName(file.toString());
      if(!format.isPresent()) {
        throw new RuntimeException("Could not determine the type of file this is: " + file.getName());
      }
      con.add(file, null, format.get(), asResource(contexts));

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
    "?context val:sourceId      ?sourceId . " +
    "?context val:sourceContext ?originalContext . " +
    "?context val:sourceFile    ?fileName . " +
    "?context val:sourceHash    ?hash . " +
    "}}";


    List<Object> result = select(query);
    for(Object rowObject :  result) {
      BindingSet row = (BindingSet) rowObject;

      String context         = row.getBinding("context").getValue().stringValue();
      String sourceId        = row.getBinding("sourceId").getValue().stringValue();
      String timestamp       = row.getBinding("timestamp").getValue().stringValue();
      String originalContext = row.getBinding("originalContext").getValue().stringValue();
      String fileName        = row.getBinding("fileName").getValue().stringValue();
      String hash            = row.getBinding("hash").getValue().stringValue();

      Source source = new Source();
      source.setId(sourceId);
      source.setGraphname(originalContext);
      source.setHash(hash);
      source.setPath(fileName);
      list.add(source);
    }

    return list;
  }


  public Map<String, Set<String>> listPhiSourceIdsPerHash() {

    Map<String, Set<String>> list = new HashMap<>();

    String query =

    "PREFIX val: <"+QueryFactory.VALIDATOR_NS+"> " +
    "SELECT DISTINCT ?hash ?sourceId " +
    "WHERE { graph ?context { " +
    "?context val:sourceHash ?hash . " +
    "?context val:sourceId   ?sourceId . " +
    "}} ORDER BY ?hash";


    List<Object> result = select(query);

    String previousHash = "";
    HashSet<String> previousSet = new HashSet<>();
    for(Object rowObject :  result) {
      BindingSet row = (BindingSet) rowObject;

      String hash       = row.getBinding("hash").getValue().stringValue();
      String sourceId   = row.getBinding("sourceId").getValue().stringValue();

      if(!previousHash.equals(hash)) {
        list.put(previousHash, previousSet);
        previousSet = new HashSet<>();
        previousHash =  hash;
      }
      previousSet.add(sourceId);
    }
    if(!previousSet.isEmpty()) {
      list.put(previousHash, previousSet);
    }

    return list;
  }


  public Map<String, Set<String>> listSigmaGraphs() {

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

    return list;
  }

  public Map<String, Set<String>> listInferencesPerSigmaGraph() {

    HashMap<String, Set<String>> list = new HashMap<>();

    String query =

    "PREFIX val: <"+QueryFactory.VALIDATOR_NS+"> " +
    "SELECT DISTINCT ?context ?inference " +
    "WHERE { graph ?context { " +
    "?context val:inference ?inference . " +
    "}} ORDER BY ?context";

    List<Object> result = select(query);

    String previousContext = "";
    HashSet<String> previousSet = new HashSet<>();
    for(Object rowObject :  result) {
      BindingSet row = (BindingSet) rowObject;

      String context         = row.getBinding("context").getValue().stringValue();
      String inference       = row.getBinding("inference").getValue().stringValue();

      if(!previousContext.equals(context)) {
        list.put(previousContext, previousSet);
        previousSet = new HashSet<>();
        previousContext =  context;
      }
      previousSet.add(inference);
    }
    if(!previousSet.isEmpty()) {
      list.put(previousContext, previousSet);
    }

    return list;
  }

  public void storePhiGraphExists(Object sourceObject, String context, String fileName, String hash) {

    Source source = (Source) sourceObject;

    String timestamp = new Timestamp(System.currentTimeMillis()).toString();

    log.info("Store phi graph exists "+timestamp+" for "+context);

    String query =

    "PREFIX val: <"+QueryFactory.VALIDATOR_NS+"> " +
    "INSERT DATA { GRAPH <"+context+"> { " +
    "<"+context+"> val:uploadedDate  \""+timestamp+"\" . " +
    "<"+context+"> val:sourceId      \""+source.getId()+"\". " +
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

    log.info("Store sigma graph exists "+timestamp+" for "+context+":\n"+query);

    update(query);
  }


  @Override
  public void uploadFile(InputStream inputStream, String fileName, String baseUri, ArrayList<String> contexts) {
    if(!initialized) {
      init();
    }

    try (RepositoryConnection con = repository.getConnection()) {
      Optional<RDFFormat> format = Rio.getParserFormatForFileName(fileName);
      if(!format.isPresent()) {
        throw new RuntimeException("Could not determine the type of file this is: " + fileName);
      }
      con.add(inputStream, baseUri, format.get(), asResource(contexts));


    } catch (IOException e) {
      log.error(e.getMessage(), e);
    }
  }

  @Override
  public long quadCount(String context) {
    if(!initialized) {
      init();
    }

    try (RepositoryConnection con = repository.getConnection()) {
      return con.size(asResource(context));
    }
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
    }
  }

  // Writes the file to the outputStream and returns a Map of imports: fileUpload context -> original context
  @Override
  public Map<String, String> exportPhiGraph(String context, OutputStream outputStream) {

    String sourceContext;
    Resource contextResource = asResource(context);
    try (RepositoryConnection con = repository.getConnection();
         RepositoryResult<Statement> statements = con.getStatements(contextResource, (IRI)asResource(QueryFactory.VALIDATOR_NS+"sourceContext"), null, contextResource)) {
      if(!statements.hasNext()) {
        throw new RuntimeException("Source context could not be read from the connector");
      }
      sourceContext = statements.next().getObject().stringValue();
    }

    Map<String, String> importsMap = getImports(context);
    List<String> uploadedFileContexts = new ArrayList<>();
    uploadedFileContexts.addAll(importsMap.keySet());
    final Map<String, String> contextMap = new HashMap<>();
    contextMap.put(context, sourceContext);

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
      log.info("Found import: " + namespace + "("+original+")");
      namespaces.put(namespace, original);
    }
    return namespaces;
  }
}
