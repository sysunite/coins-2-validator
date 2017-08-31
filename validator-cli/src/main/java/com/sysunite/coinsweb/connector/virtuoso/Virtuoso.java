package com.sysunite.coinsweb.connector.virtuoso;

import com.sysunite.coinsweb.connector.Rdf4jConnector;
import com.sysunite.coinsweb.connector.graphdb.GraphDB;
import com.sysunite.coinsweb.parser.config.pojo.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import virtuoso.rdf4j.driver.VirtuosoRepository;

/**
 * @author bastbijl, Sysunite 2017
 */
public class Virtuoso extends Rdf4jConnector {

  private static final Logger log = LoggerFactory.getLogger(GraphDB.class);

  public static final String REFERENCE = "virtuoso";

  private String url;
  private String user;
  private String password;

  boolean repoPerRun = false;
  boolean createRepo;



  public Virtuoso(Environment config) {

    if(config.getStore().getConfig() == null || !config.getStore().getConfig().containsKey("endpoint")) {
      throw new RuntimeException("No endpoint url specified");
    }
    url = config.getStore().getConfig().get("endpoint");

    if(config.getStore().getConfig() == null || !config.getStore().getConfig().containsKey("user")) {
      throw new RuntimeException("No endpoint user specified");
    }
    user = config.getStore().getConfig().get("user");

    if(config.getStore().getConfig() == null || !config.getStore().getConfig().containsKey("password")) {
      throw new RuntimeException("No endpoint password specified");
    }
    password = config.getStore().getConfig().get("password");




    cleanUp = config.getCleanUp();
    createRepo = config.getCreateRepo();
    wipeOnClose = config.getDestroyRepo();


    if(Environment.REPO_PER_RUN.equals(config.getLoadingStrategy())) {
      repoPerRun = true;
    }




  }



  public void init() {

    if(initialized) {
      return;
    }

    log.info("Initialize connector ("+REFERENCE+")");

    log.info("Going to connect to " + url);
    String fullUrl = "jdbc:virtuoso://" + url;
    repository = new VirtuosoRepository(fullUrl, user, password);
    repository.initialize();
    ((VirtuosoRepository)repository).setUseDefGraphForQueries(false); // todo
    initialized = true;
  }

  @Override
  public void update(String queryString) {
    super.update("DEFINE sql:log-enable 2 " + queryString);
  }

  @Override
  public void sparqlCopy(String fromContext, String toContext) {
    update(
    "DEFINE sql:log-enable 2 " +    // Autocommit mode, do not write transactions to log
    "COPY <"+fromContext+"> TO <"+toContext+">");
//    storeGraphExists(toContext, fromContext);
  }
  @Override
  public void sparqlAdd(String fromContext, String toContext) {
    update(
    "DEFINE sql:log-enable 2 " +    // Autocommit mode, do not write transactions to log
    "ADD <"+fromContext+"> TO <"+toContext+">");
  }



  @Override
  public void close() {
    if(!initialized) {
      return;
    }
    repository.shutDown();
    if(wipeOnClose) {
      // todo
    }
  }




//  private RepositoryConfig createRepositoryConfig(String repositoryId) throws IOException {
//
//    // see http://graphdb.ontotext.com/documentation/free/configuring-a-repository.html
//
//    String repoTurtle =
//    "@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>.\n" +
//    "@prefix rep: <http://www.openrdf.org/config/repository#>.\n" +
//    "@prefix sr: <http://www.openrdf.org/config/repository/sail#>.\n" +
//    "@prefix sail: <http://www.openrdf.org/config/sail#>.\n" +
//    "@prefix owlim: <http://www.ontotext.com/trree/owlim#>.\n" +
//    "[] a rep:Repository ;\n" +
//    "    rep:repositoryID \"" + repositoryId + "\" ;\n" +
//    "    rdfs:label \"GraphDB Free repository\" ;\n" +
//    "    rep:repositoryImpl [\n" +
//    "        rep:repositoryType \"graphdb:FreeSailRepository\" ;\n" +
//    "        sr:sailImpl [\n" +
//    "            sail:sailType \"graphdb:FreeSail\" ;\n" +
//    "            owlim:base-URL \"http://example.org/graphdb#\" ;\n" +
//    "            owlim:defaultNS \"\" ;\n" +
//    "            owlim:entity-index-size \"10000000\" ;\n" +
//    "            owlim:entity-id-size  \"32\" ;\n" +
//    "            owlim:imports \"\" ;\n" +
//    "            owlim:repository-type \"file-repository\" ;\n" +
//    "            owlim:ruleset \"empty\" ;\n" +
//    "            owlim:storage-folder \"storage\" ;\n" +
//    "            owlim:enable-context-index \"true\" ;\n" +
//    "            owlim:enablePredicateList \"true\" ;\n" +
//    "            owlim:in-memory-literal-properties \"true\" ;\n" +
//    "            owlim:enable-literal-index \"true\" ;\n" +
//    "            owlim:check-for-inconsistencies \"false\" ;\n" +
//    "            owlim:disable-sameAs  \"true\" ;\n" +
//    "            owlim:query-timeout  \"0\" ;\n" +
//    "            owlim:query-limit-results  \"0\" ;\n" +
//    "            owlim:throw-QueryEvaluationException-on-timeout \"false\" ;\n" +
//    "            owlim:read-only \"false\" ;\n" +
//    "            owlim:nonInterpretablePredicates \"" +
//    "http://www.w3.org/2000/01/rdf-schema#label;" +
//    "http://www.w3.org/1999/02/22-rdf-syntax-ns#type;" +
//    "http://www.ontotext.com/owlim/ces#gazetteerConfig;" +
//    "http://www.ontotext.com/owlim/ces#metadataConfig\" ;\n" +
//    "        ]\n" +
//    "    ].";
//
//    // see http://graphdb.ontotext.com/documentation/free/using-graphdb-with-the-rdf4j-api.html
//
//    TreeModel graph = new TreeModel();
//    InputStream config = new ByteArrayInputStream(repoTurtle.getBytes(StandardCharsets.UTF_8));
//    RDFParser rdfParser = Rio.createParser(RDFFormat.TURTLE);
//    rdfParser.setRDFHandler(new StatementCollector(graph));
//    rdfParser.parse(config, RepositoryConfigSchema.NAMESPACE);
//    config.close();
//    Resource repositoryNode = GraphUtil.getUniqueSubject(graph, RDF.TYPE, RepositoryConfigSchema.REPOSITORY);
//    return RepositoryConfig.create(graph, repositoryNode);
//  }
}

