package com.sysunite.coinsweb.graphset;

import com.sysunite.coinsweb.connector.Connector;
import com.sysunite.coinsweb.filemanager.ContainerFile;
import com.sysunite.coinsweb.parser.config.pojo.ConfigFile;
import com.sysunite.coinsweb.parser.config.pojo.Container;
import com.sysunite.coinsweb.report.ReportFactory;
import com.sysunite.coinsweb.steps.profile.QueryResult;
import com.sysunite.coinsweb.steps.profile.ValidationQueryResult;
import freemarker.template.Template;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author bastbijl, Sysunite 2017
 */
public class ContainerGraphSetImpl implements ContainerGraphSet {

  private static final Logger log = LoggerFactory.getLogger(ContainerGraphSetImpl.class);



  private boolean disabled;
  private Connector connector;
  private ContainerFile container;
  private Container containerConfig;
  private ConfigFile configFile;
  private HashMap<String, String> contextMap;



  public ContainerGraphSetImpl() {
    this.disabled = true;
  }

  public ContainerGraphSetImpl(Connector connector) {
    this.disabled = false;
    this.connector = connector;
  }

  private void load() {

    if(disabled) {
      return;
    }

    log.info("Initialize connector");
    connector.init();

    contextMap = GraphSetFactory.load(containerConfig.getGraphs(), connector, container, configFile);

    log.info("Finished initializing connector");
    this.connector.setAllLoaded();
  }

  /**
   * returns true if it least one result was found
   */
  public TupleQueryResult select(String query) {

    if(connector.requiresLoad()) {
      load();
    }

    TupleQueryResult result = (TupleQueryResult) connector.query(query);
    return result;
  }
  public boolean select(String query, Object formatTemplate, Object validationStepResult) {

    boolean resultsFound = false;

    if(connector.requiresLoad()) {
      load();
    }

    long start = new Date().getTime();

    ArrayList<String> formattedResults = new ArrayList<>();
    TupleQueryResult result = (TupleQueryResult) connector.query(query);

    if(!result.hasNext()) {
      log.info("No results, which is good");
    } else {
      log.info("Results were found, this is bad");
      resultsFound = true;

      if(formatTemplate != null) {
        while (result.hasNext()) {
          BindingSet row = result.next();
          formattedResults.add(ReportFactory.formatResult(row, (Template) formatTemplate));
        }
      }
    }

    long executionTime = new Date().getTime() - start;
    if(validationStepResult != null) {
      ((QueryResult) validationStepResult).setExecutionTime(executionTime);
      ((QueryResult) validationStepResult).setExecutedQuery(query);
      ((ValidationQueryResult) validationStepResult).addFormattedResults(formattedResults);
    }
    return resultsFound;
  }

  public Map<String, String> contextMap() {
    if(connector.requiresLoad()) {
      load();
    }
    return contextMap;
  }

  public boolean hasContext(String graphVar) {
    return contextMap().containsKey(graphVar);
  }

  public Map<String, Long> quadCount() {
    if(connector.requiresLoad()) {
      load();
    }
    return connector.quadCount();
  }

  public List<String> getImports(String graphVar) {

    String context = contextMap().get(graphVar);

    log.info("Look for imports in context "+context);

    String query =

      "PREFIX owl: <http://www.w3.org/2002/07/owl#> " +
      "SELECT ?library " +
      "FROM NAMED <"+context+"> " +
      "WHERE { graph ?g { " +
      "  ?s owl:imports ?library . " +
      "}}";

    List<String> namespaces = new ArrayList<>();
    TupleQueryResult result = select(query);
    while (result.hasNext()) {
      BindingSet row = result.next();
      String namespace = row.getBinding("library").getValue().stringValue();
      log.info("Found import: "+namespace);
      namespaces.add(namespace);
    }
    return namespaces;
  }



  public void update(String query, Object validationStepResult) {

    if(connector.requiresLoad()) {
      load();
    }

    long start = new Date().getTime();

    connector.update(query);

    long executionTime = new Date().getTime() - start;
    ((QueryResult) validationStepResult).setExecutionTime(executionTime);
    ((QueryResult) validationStepResult).setExecutedQuery(query);
  }

  public void update(String query) {
    if(connector.requiresLoad()) {
      load();
    }
    connector.update(query);
  }

  @Override
  public void setContainerFile(ContainerFile container) {
    this.container = container;
  }

  @Override
  public void setContainerConfig(Object config) {
    if(config instanceof Container) {
      this.containerConfig = (Container) config;
      return;
    }
    throw new RuntimeException("The object is not an instance of a Container configuration");
  }

  @Override
  public void setConfigFile(Object configFile) {
    if(configFile instanceof ConfigFile) {
      this.configFile = (ConfigFile) configFile;
      return;
    }
    throw new RuntimeException("The object is not an instance of a ConfigFile ");
  }

  @Override
  public void close() {
    if(!connector.requiresLoad()) {
      connector.cleanup();
    }
  }

  @Override
  public String graphExists(String graphVar) {

    String context = contextMap().get(graphVar);
    if(!connector.requiresLoad()) {
      load();
    }
    return connector.graphExists(context);
  }
}
