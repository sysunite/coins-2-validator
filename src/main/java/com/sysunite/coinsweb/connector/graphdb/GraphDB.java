package com.sysunite.coinsweb.connector.graphdb;

import com.sysunite.coinsweb.config.Endpoint;
import com.sysunite.coinsweb.connector.Connector;
import org.apache.log4j.Logger;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.manager.RemoteRepositoryManager;
import org.eclipse.rdf4j.repository.manager.RepositoryManager;

/**
 * @author bastbijl, Sysunite 2017
 */
public class GraphDB implements Connector {

  private static final Logger log = Logger.getLogger(GraphDB.class);


  public GraphDB(Endpoint config) {

    log.info(config.getConfig().containsKey("custom"));
    log.info(config.getConfig().containsKey("uri"));
    log.info(config.getConfig().containsKey("user"));
    log.info(config.getConfig().containsKey("password"));

  }

  @Override
  public boolean connect() {
    return false;
  }

  @Override
  public boolean testConnection() {

    RepositoryManager repositoryManager = new RemoteRepositoryManager( "http://localhost:7200" );
    repositoryManager.initialize();

    Repository repo = repositoryManager.getRepository("otl21");
    try (RepositoryConnection con = repo.getConnection()) {



      String queryString = "select * where { \n" +
      "    graph <http://otl.rws.nl/> { ?s ?p ?o . }\n" +
      "} limit 10 ";
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

    return false;
  }
}
