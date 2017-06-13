package com.sysunite.coinsweb.graphset;

import com.sysunite.coinsweb.connector.Connector;
import com.sysunite.coinsweb.filemanager.ContainerFile;
import com.sysunite.coinsweb.filemanager.FileFactory;
import com.sysunite.coinsweb.parser.config.ConfigFile;
import com.sysunite.coinsweb.parser.config.Container;
import com.sysunite.coinsweb.parser.config.Graph;
import com.sysunite.coinsweb.report.ReportFactory;
import com.sysunite.coinsweb.steps.ValidationStepResult;
import com.sysunite.coinsweb.steps.profile.ValidationQueryResult;
import freemarker.template.Template;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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
  private HashMap<String, String> graphs;



  public ContainerGraphSetImpl() {
    this.disabled = true;
  }

  public ContainerGraphSetImpl(Connector connector, HashMap<String, String> graphs) {
    this.disabled = false;
    this.connector = connector;
    this.graphs = graphs;
  }

  private void load() {

    if(disabled) {
      return;
    }

    log.info("Initialize connector");
    connector.init();

    for(Graph graph : containerConfig.getGraphs()) {

      String[] graphNames = new String[graph.getContent().size()];
      for(int i = 0; i < graph.getContent().size(); i++) {
        graphNames[i] = graphs.get(graph.getContent().get(i));
      }
      String fileName = graph.getPath();
      if(fileName == null) {
        fileName = graph.getUri();
      }

      log.info("Upload rdf file to connector: "+fileName);
      connector.uploadFile(FileFactory.toInputStream(graph, container, configFile), fileName, graph.getGraphname(), graphNames);
    }

    log.info("Finished initializing connector");
    this.connector.setAllLoaded();
  }

  public void select(String query, Object formatTemplate, ValidationStepResult validationStepResult) {

    if(connector.requiresLoad()) {
      load();
    }

    long start = new Date().getTime();

    ArrayList<String> formattedResults = new ArrayList<>();
    TupleQueryResult result = (TupleQueryResult) connector.query(query);

    if(!result.hasNext()) {
      log.info("No results, which is good");
    } else {

      while(result.hasNext()) {
        BindingSet row = result.next();
        formattedResults.add(ReportFactory.formatResult(row, (Template) formatTemplate));
      }
    }

    long executionTime = new Date().getTime() - start;
    ((ValidationQueryResult) validationStepResult).setExecutionTime(executionTime);
    ((ValidationQueryResult) validationStepResult).setExecutedQuery(query);
    ((ValidationQueryResult) validationStepResult).addFormattedResults(formattedResults);
  }

  public Map<String, Long> quadCount() {
    return connector.quadCount();
  }



  public void update(String query, ValidationStepResult validationStepResult) {

    if(connector.requiresLoad()) {
      load();
    }

    long start = new Date().getTime();

    connector.update(query);

    long executionTime = new Date().getTime() - start;
    ((ValidationQueryResult) validationStepResult).setExecutionTime(executionTime);
    ((ValidationQueryResult) validationStepResult).setExecutedQuery(query);
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
    connector.cleanup();
  }

}
