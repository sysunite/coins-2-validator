package com.sysunite.coinsweb.connector.graphdb;

import com.sysunite.coinsweb.connector.Rdf4jConnector;
import com.sysunite.coinsweb.parser.config.pojo.Environment;
import org.apache.commons.lang3.RandomStringUtils;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.impl.TreeModel;
import org.eclipse.rdf4j.model.util.GraphUtil;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.repository.config.RepositoryConfig;
import org.eclipse.rdf4j.repository.config.RepositoryConfigSchema;
import org.eclipse.rdf4j.repository.manager.RemoteRepositoryManager;
import org.eclipse.rdf4j.repository.manager.RepositoryManager;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.helpers.StatementCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * @author bastbijl, Sysunite 2017
 */
public class GraphDB extends Rdf4jConnector {

  private static final Logger log = LoggerFactory.getLogger(GraphDB.class);

  public static final String REFERENCE = "graphdb";

  private String url;
  RepositoryManager manager;
  String repositoryId;

  boolean repoPerRun = false;



  public GraphDB(Environment config) {

    if(config.getStore().getConfig() == null || !config.getStore().getConfig().containsKey("endpoint")) {
      throw new RuntimeException("No endpoint url specified");
    }
    url = config.getStore().getConfig().get("endpoint");


    cleanUp = config.getCleanUp();
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

    manager = new RemoteRepositoryManager(url);
    manager.initialize();

    try {

      if(repoPerRun) {
        repositoryId = "validator-generated-" + RandomStringUtils.random(8, true, true);
      } else {
        repositoryId = "validator-generated";
      }
      if(manager.hasRepositoryConfig(repositoryId)) {
        log.info("Found existing repository");
      } else {
        manager.addRepositoryConfig(createRepositoryConfig(repositoryId));
      }
    } catch (IOException e) {
    log.error(e.getMessage(), e);
    }
    repository = manager.getRepository(repositoryId);
    initialized = true;
  }





  @Override
  public void close() {
    if(!initialized) {
      return;
    }
    repository.shutDown();
    if(wipeOnClose) {
      if (manager != null && repositoryId != null) {
        manager.removeRepository(repositoryId);
      }
    }
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
    "            owlim:enable-context-index \"true\" ;\n" +
    "            owlim:enablePredicateList \"true\" ;\n" +
    "            owlim:in-memory-literal-properties \"true\" ;\n" +
    "            owlim:enable-literal-index \"true\" ;\n" +
    "            owlim:check-for-inconsistencies \"false\" ;\n" +
    "            owlim:disable-sameAs  \"true\" ;\n" +
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

    // see http://graphdb.ontotext.com/documentation/free/using-graphdb-with-the-rdf4j-api.html

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
