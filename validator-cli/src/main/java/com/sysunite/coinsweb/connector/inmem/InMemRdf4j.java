package com.sysunite.coinsweb.connector.inmem;

import com.sysunite.coinsweb.connector.Connector;
import com.sysunite.coinsweb.parser.config.Store;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

/**
 * @author bastbijl, Sysunite 2017
 */
public class InMemRdf4j implements Connector {

  private static final Logger log = LoggerFactory.getLogger(InMemRdf4j.class);
  private Repository repository;

  private boolean initialized = false;




  public InMemRdf4j(Store config) {

//    log.info(config.getConfig().containsKey("custom"));
//    log.info(config.getConfig().containsKey("endpoint"));
//    log.info(config.getConfig().containsKey("user"));
//    log.info(config.getConfig().containsKey("password"));

    repository = new SailRepository(new MemoryStore());
    repository.initialize();



  }




  @Override
  public boolean testConnection() {

    return false;
  }

  @Override
  public boolean query(String queryString) {

    try (RepositoryConnection con = repository.getConnection()) {




      TupleQuery tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
      try (TupleQueryResult result = tupleQuery.evaluate()) {
        while (result.hasNext()) {  // iterate over the result
          BindingSet bindingSet = result.next();
          Value s = bindingSet.getValue("s");
          Value p = bindingSet.getValue("p");
          Value o = bindingSet.getValue("o");
          log.info( s+"-"+p+"->"+o);
        }
      } catch (Exception e) {
        log.error(e.getMessage(), e);
      }
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }
    return true;
  }

  @Override
  public boolean cleanup() {
    try (RepositoryConnection con = repository.getConnection()) {
      con.clear();
    }
    return false;
  }

  @Override
  public void uploadFile(File file, String[] contexts) {

    ValueFactory factory = repository.getValueFactory();
    Resource[] contextsIRI = new Resource[contexts.length];
    for(int i = 0; i < contexts.length; i++) {
      contextsIRI[i] = factory.createIRI(contexts[i]);
    }

    try (RepositoryConnection con = repository.getConnection()) {
      Optional<RDFFormat> format = Rio.getParserFormatForFileName(file.toString());
      if(!format.isPresent()) {
        throw new RuntimeException("Could not determine the type of file this is: " + file.getName());
      }
      con.add(file, null, format.get(), contextsIRI);

    } catch (IOException e) {
      log.error(e.getMessage(), e);
    }
  }

  @Override
  public void uploadFile(InputStream inputStream, String fileName, String baseUri, String[] contexts) {

    ValueFactory factory = repository.getValueFactory();
    Resource[] contextsIRI = new Resource[contexts.length];
    for(int i = 0; i < contexts.length; i++) {
      contextsIRI[i] = factory.createIRI(contexts[i]);
    }

    try (RepositoryConnection con = repository.getConnection()) {
      Optional<RDFFormat> format = Rio.getParserFormatForFileName(fileName);
      if(!format.isPresent()) {
        throw new RuntimeException("Could not determine the type of file this is: " + fileName);
      }
      con.add(inputStream, baseUri, format.get(), contextsIRI);

    } catch (IOException e) {
      log.error(e.getMessage(), e);
    }

  }

  public boolean requiresLoad() {
    return !initialized;
  }
  public void setAllLoaded() {
    initialized = true;
  }
}
