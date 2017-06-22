package com.sysunite.coinsweb.connector;

import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.query.Update;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
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

    return false;
  }

  @Override
  public TupleQueryResult query(String queryString) {

    try (RepositoryConnection con = repository.getConnection()) {

      TupleQuery tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
      return tupleQuery.evaluate();
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }
    throw new RuntimeException("Was not able to build resultset for query: "+queryString);
  }

  @Override
  public void update(String queryString) {

    try (RepositoryConnection con = repository.getConnection()) {

      Update updateQuery = con.prepareUpdate(QueryLanguage.SPARQL, queryString);
      try {
        updateQuery.execute();
      } catch (Exception e) {
        log.error(e.getMessage(), e);
      }
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }
  }

  @Override
  public void cleanup() {
    try (RepositoryConnection con = repository.getConnection()) {
      con.clear();
    }
  }

  @Override
  public void uploadFile(File file, String[] contexts) {

    try (RepositoryConnection con = repository.getConnection()) {
      Optional<RDFFormat> format = Rio.getParserFormatForFileName(file.toString());
      if(!format.isPresent()) {
        throw new RuntimeException("Could not determine the type of file this is: " + file.getName());
      }
      con.add(file, null, format.get(), getContexts(contexts));

    } catch (IOException e) {
      log.error(e.getMessage(), e);
    }
  }

  @Override
  public void uploadFile(InputStream inputStream, String fileName, String baseUri, ArrayList<String> contexts) {

    try (RepositoryConnection con = repository.getConnection()) {
      Optional<RDFFormat> format = Rio.getParserFormatForFileName(fileName);
      if(!format.isPresent()) {
        throw new RuntimeException("Could not determine the type of file this is: " + fileName);
      }
      con.add(inputStream, baseUri, format.get(), getContexts(contexts.toArray(new String[0])));

    } catch (IOException e) {
      log.error(e.getMessage(), e);
    }
  }

  @Override
  public HashMap<String, Long> quadCount() {
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
    try (RepositoryConnection con = repository.getConnection()) {
      RepositoryResult<Resource> graphIterator = con.getContextIDs();
      while(graphIterator.hasNext()) {
        Resource graphName = graphIterator.next();
        if(graphName.toString().equals(context)) {
          return true;
        }
      }
    }
    return false;
  }
  private Resource[] getContexts(String[] contexts) {
    ValueFactory factory = repository.getValueFactory();
    Resource[] contextsIRI = new Resource[contexts.length];
    for(int i = 0; i < contexts.length; i++) {
      contextsIRI[i] = factory.createIRI(contexts[i]);
    }
    return contextsIRI;
  }

  public boolean requiresLoad() {
    return !initialized;
  }
  public void setAllLoaded() {
    initialized = true;
  }
}
