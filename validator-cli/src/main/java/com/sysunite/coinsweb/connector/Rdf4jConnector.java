package com.sysunite.coinsweb.connector;

import com.sysunite.coinsweb.graphset.QueryFactory;
import com.sysunite.coinsweb.rdfutil.Utils;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.query.*;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

/**
 * @author bastbijl, Sysunite 2017
 */
public abstract class Rdf4jConnector implements Connector {

  private static final Logger log = LoggerFactory.getLogger(Rdf4jConnector.class);
  protected Repository repository;
  protected boolean initialized = false;











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
  public TupleQueryResult query(String queryString) {
    if(!initialized) {
      init();
    }

    try (RepositoryConnection con = repository.getConnection()) {

      TupleQuery tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
      tupleQuery.setIncludeInferred(false);
      return tupleQuery.evaluate();
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
  public void cleanup() {
    if(!initialized) {
      init();
    }
    try (RepositoryConnection con = repository.getConnection()) {
      con.clear();
    }
  }

  @Override
  public void close() {
    if(!initialized) {
      return;
    }
  }

  @Override
  public void uploadFile(File file, String[] contexts) {
    if(!initialized) {
      init();
    }

    try (RepositoryConnection con = repository.getConnection()) {
      Optional<RDFFormat> format = Rio.getParserFormatForFileName(file.toString());
      if(!format.isPresent()) {
        throw new RuntimeException("Could not determine the type of file this is: " + file.getName());
      }
      con.add(file, null, format.get(), getContexts(contexts));
      for(String context : contexts) {
        storeGraphExists(context);
      }

    } catch (IOException e) {
      log.error(e.getMessage(), e);
    }
  }


  public String graphExists(String context) {


    if(context == null || context.isEmpty()) {
      return null;
    }

    String query =

    "PREFIX val: <"+ QueryFactory.VALIDATOR_NS+"> " +
    "SELECT ?creationDate " +
    "FROM NAMED <"+context+"> " +
    "WHERE { graph <"+context+"> { " +
    "  <"+context+"> val:uploaded ?creationDate . " +
    "}}";

    String creationDate = null;
    TupleQueryResult result = query(query);
    if (result.hasNext()) {
      BindingSet row = result.next();
      creationDate = row.getBinding("creationDate").getValue().stringValue();
    }
    return creationDate;
  }

  private void storeGraphExists(String context) {

    String timestamp = new Timestamp(System.currentTimeMillis()).toString();

    log.info("Store graphExists "+timestamp+" for "+context);

    String query =

    "PREFIX val: <"+QueryFactory.VALIDATOR_NS+"> " +
    "INSERT DATA { GRAPH <"+context+"> { <"+context+"> val:uploaded \""+timestamp+"\" . }}";

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
      con.add(inputStream, baseUri, format.get(), getContexts(contexts.toArray(new String[0])));
      for(String context : contexts) {
        storeGraphExists(context);
      }

    } catch (IOException e) {
      log.error(e.getMessage(), e);
    }
  }

  @Override
  public HashMap<String, Long> quadCount() {
    if(!initialized) {
      init();
    }
    HashMap<String, Long> result = new HashMap();
    try (RepositoryConnection con = repository.getConnection()) {
      RepositoryResult<Resource> graphIterator = con.getContextIDs();
      while(graphIterator.hasNext()) {
        Resource graphName = graphIterator.next();
        long count = con.size(graphName);
        result.put(graphName.toString(), count);
      }
    }
    return result;
  }




  public boolean containsContext(String context) {
    if(!initialized) {
      init();
    }
    try (RepositoryConnection con = repository.getConnection()) {
      RepositoryResult<Resource> graphIterator = con.getContextIDs();
      while(graphIterator.hasNext()) {
        Resource graphName = graphIterator.next();
        if(Utils.withoutHashOrSlash(graphName.toString()).equals(Utils.withoutHashOrSlash(context))) {
          return true;
        }
      }
    }
    return false;
  }
  private Resource[] getContexts(String[] contexts) {
    if(!initialized) {
      init();
    }
    ValueFactory factory = repository.getValueFactory();
    Resource[] contextsIRI = new Resource[contexts.length];
    for(int i = 0; i < contexts.length; i++) {
      contextsIRI[i] = factory.createIRI(contexts[i]);
    }
    return contextsIRI;
  }


}
