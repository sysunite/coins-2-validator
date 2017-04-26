package com.sysunite.coinsweb.connector.inmem;

import com.sysunite.coinsweb.config.Store;
import com.sysunite.coinsweb.connector.Connector;
import org.apache.log4j.Logger;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.sail.memory.MemoryStore;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

/**
 * @author bastbijl, Sysunite 2017
 */
public class InMemRdf4j implements Connector {

  private static final Logger log = Logger.getLogger(InMemRdf4j.class);
  private Repository repository;




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
        e.printStackTrace();
      }
    } catch (Exception e) {
      e.printStackTrace();
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
  public boolean uploadFile(File file, String namespace) {

//    ValueFactory factory = repository.getValueFactory();
    try (RepositoryConnection con = repository.getConnection()) {
      Optional<RDFFormat> format = Rio.getParserFormatForFileName(file.toString());
      if(!format.isPresent()) {
        throw new RuntimeException("Could not determine the type of file this is: " + file.getName());
      }
      con.add(file, null, format.get());

      log.warn("listing context ids:");
      RepositoryResult<Resource> contexts = con.getContextIDs();
      while(contexts.hasNext()) {
        log.warn(contexts.next().stringValue());
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return false;
  }

}
