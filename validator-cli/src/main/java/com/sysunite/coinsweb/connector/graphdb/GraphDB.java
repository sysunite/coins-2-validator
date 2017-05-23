package com.sysunite.coinsweb.connector.graphdb;

import com.sysunite.coinsweb.connector.Connector;
import com.sysunite.coinsweb.parser.config.Store;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.TreeModel;
import org.eclipse.rdf4j.model.util.GraphUtil;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.eclipse.rdf4j.repository.config.RepositoryConfig;
import org.eclipse.rdf4j.repository.config.RepositoryConfigSchema;
import org.eclipse.rdf4j.repository.manager.RemoteRepositoryManager;
import org.eclipse.rdf4j.repository.manager.RepositoryManager;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.helpers.StatementCollector;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * @author bastbijl, Sysunite 2017
 */
public class GraphDB implements Connector {

  private static final Logger log = LoggerFactory.getLogger(GraphDB.class);
  private Repository repository;

  private String endpoint;


  public GraphDB(Store config) {

    log.info(""+config.getConfig().containsKey("custom"));
    log.info(""+config.getConfig().containsKey("endpoint"));
    log.info(""+config.getConfig().containsKey("user"));
    log.info(""+config.getConfig().containsKey("password"));




  }

  private void init() {
    RepositoryManager manager = new RemoteRepositoryManager( "http://localhost:7200" );
    manager.initialize();

//    String repositoryId = "otl21-generated";
//
//    try {
//      manager.addRepositoryConfig(createRepositoryConfig(repositoryId));
//    } catch (IOException e) {
//      e.printStackTrace();
//    }
//    repository = manager.getRepository(repositoryId);

    repository = manager.getRepository("otl21");
  }




  @Override
  public boolean testConnection() {
    init();
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

    ValueFactory factory = repository.getValueFactory();
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

  private RepositoryConfig createRepositoryConfig(String repositoryId) throws IOException {

    // see http://graphdb.ontotext.com/documentation/free/configuring-a-repository.html

    String repoTurtle =
    "@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>.\n" +
    "@prefix rep: <http://www.openrdf.org/config/repository#>.\n" +
    "@prefix sr: <http://www.openrdf.org/config/repository/sail#>.\n" +
    "@prefix sail: <http://www.openrdf.org/config/sail#>.\n" +
    "@prefix owlim: <http://www.ontotext.com/trree/owlim#>.\n" +
    "[] a rep:Repository ;\n" +
    "    rep:repositoryID \"" + repositoryId + "\" ;\n" +
    "    rdfs:label \"GraphDB Free repository\" ;\n" +
    "    rep:repositoryImpl [\n" +
    "        rep:repositoryType \"graphdb:FreeSailRepository\" ;\n" +
    "        sr:sailImpl [\n" +
    "            sail:sailType \"graphdb:FreeSail\" ;\n" +
    "            owlim:base-URL \"http://example.org/graphdb#\" ;\n" +
    "            owlim:defaultNS \"\" ;\n" +
    "            owlim:entity-index-size \"10000000\" ;\n" +
    "            owlim:entity-id-size  \"32\" ;\n" +
    "            owlim:imports \"\" ;\n" +
    "            owlim:repository-type \"file-repository\" ;\n" +
    "            owlim:ruleset \"empty\" ;\n" +
    "            owlim:storage-folder \"storage\" ;\n" +
    "            owlim:enable-context-index \"false\" ;\n" +
    "            owlim:enablePredicateList \"true\" ;\n" +
    "            owlim:in-memory-literal-properties \"true\" ;\n" +
    "            owlim:enable-literal-index \"true\" ;\n" +
    "            owlim:check-for-inconsistencies \"false\" ;\n" +
    "            owlim:disable-sameAs  \"false\" ;\n" +
    "            owlim:query-timeout  \"0\" ;\n" +
    "            owlim:query-limit-results  \"0\" ;\n" +
    "            owlim:throw-QueryEvaluationException-on-timeout \"false\" ;\n" +
    "            owlim:read-only \"false\" ;\n" +
    "            owlim:nonInterpretablePredicates \"" +
    "http://www.w3.org/2000/01/rdf-schema#label;" +
    "http://www.w3.org/1999/02/22-rdf-syntax-ns#type;" +
    "http://www.ontotext.com/owlim/ces#gazetteerConfig;" +
    "http://www.ontotext.com/owlim/ces#metadataConfig\" ;\n" +
    "        ]\n" +
    "    ].";

    TreeModel graph = new TreeModel();
    InputStream config = new ByteArrayInputStream(repoTurtle.getBytes(StandardCharsets.UTF_8));
    RDFParser rdfParser = Rio.createParser(RDFFormat.TURTLE);
    rdfParser.setRDFHandler(new StatementCollector(graph));
    rdfParser.parse(config, RepositoryConfigSchema.NAMESPACE);
    config.close();
    Resource repositoryNode = GraphUtil.getUniqueSubject(graph, RDF.TYPE, RepositoryConfigSchema.REPOSITORY);
    return RepositoryConfig.create(graph, repositoryNode);
  }
}
