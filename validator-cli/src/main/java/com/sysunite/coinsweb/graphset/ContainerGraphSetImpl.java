package com.sysunite.coinsweb.graphset;

import com.sysunite.coinsweb.connector.Connector;
import com.sysunite.coinsweb.filemanager.ContainerFile;
import com.sysunite.coinsweb.filemanager.ContainerFileImpl;
import com.sysunite.coinsweb.filemanager.DescribeFactoryImpl;
import com.sysunite.coinsweb.parser.config.pojo.GraphVarImpl;
import com.sysunite.coinsweb.parser.config.pojo.Mapping;
import com.sysunite.coinsweb.report.ReportFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static com.sysunite.coinsweb.graphset.ContainerGraphSetFactory.fingerPrint;

/**
 * @author bastbijl, Sysunite 2017
 */
public class ContainerGraphSetImpl implements ContainerGraphSet {

  private static final Logger log = LoggerFactory.getLogger(ContainerGraphSetImpl.class);


  private ContainerFileImpl lazyLoad = null;
  private Map<String, Set<GraphVar>> inferencePreference;

  private boolean disabled;
  private Connector connector;
  private ComposePlan composePlan;

  private GraphVarImpl main;

  private List<Mapping> variables;


  public ContainerGraphSetImpl(List<Mapping> variables) {
    this.variables = variables;
    this.disabled = true;
  }

  public ContainerGraphSetImpl(List<Mapping> variables, Connector connector) {
    this.variables = variables;
    this.connector = connector;
    this.disabled = false;
  }

  public Object getMain() {
    return main;
  }
  public void setMain(Object main) {
    this.main = (GraphVarImpl) main;
  }

  // Execute postponed lazy load
  public void load() {

    if(!requiresLoad()) {
      return;
    }

    // Essential step to get rid of the wildcards in the configFile
    log.info("Will now expand any wildcard usage in the config.yml section of this file");
    DescribeFactoryImpl.expandGraphConfig(lazyLoad.getConfig(), lazyLoad);

    if(disabled) {
      return;
    }

    log.info("Load stuff to connector");
    composePlan = ContainerGraphSetFactory.load(this, connector, lazyLoad, inferencePreference);

    log.info("Tried compose plan: " + ReportFactory.buildJson(variables));


    lazyLoad.getConfig().setVariables(variables);
    this.lazyLoad = null;
  }

  public Connector getConnector() {
    return connector;
  }

  /**
   * returns true if it least one result was found
   */
  public List<Object> select(String query) {

    if(requiresLoad()) {
      load();
    }

    List<Object> result = connector.select(query);
    return result;
  }
  public List<Object> select(String query, long limit) {

    if(requiresLoad()) {
      load();
    }

    List<Object> result = connector.select(query, limit);
    return result;
  }










  public void setVariables(List<Mapping> variables) {
    this.variables = variables;
  }
  public List<Mapping> getVariables() {
    return variables;
  }

  public Map<GraphVar, String> contextMap() {
    if(requiresLoad()) {
      load();
    }

    HashMap<GraphVar, String> graphs = new HashMap();
    for(Mapping mapping : variables) {
      graphs.put(mapping.getVariable(), mapping.getGraphname());
    }
    return graphs;
  }

  public boolean hasContext(GraphVar graphVar) {
    return contextMap().containsKey(graphVar);
  }

  public Map<GraphVar, Long> quadCount() {
    if(requiresLoad()) {
      load();
    }
    HashMap<GraphVar, Long> map = new HashMap();
    for(GraphVar graphVar : contextMap().keySet()) {
      map.put(graphVar, connector.quadCount(contextMap().get(graphVar)));
    }
    return map;
  }

  public Map<String, String> getImports(GraphVar graphVar) {
    if(requiresLoad()) {
      load();
    }
    String context = contextMap().get(graphVar);
    return connector.getImports(context);
  }




  public void update(String query) {
    if(requiresLoad()) {
      load();
    }
    connector.update(query);
  }

  @Override
  public void lazyLoad(ContainerFile container, Map<String, Set<GraphVar>> inferencePreference) {
    this.lazyLoad = (ContainerFileImpl)container;
    this.inferencePreference = inferencePreference;
  }





  /**
   * Remove the contexts (graphs) from the connection that belong to this graphSet
   */
  @Override
  public void cleanup() {


    if(!requiresLoad()) {

      ArrayList<String> contexts = new ArrayList<>();
      log.info("Will wipe these graphs (if enabled) to remove GraphSet graphs from the Connector:");
      for(String context : contextMap().values()) {
        log.info("- "+context);
        contexts.add(context);
      }

      connector.cleanup(contexts);
    }
  }



  public boolean requiresLoad() {
    return (lazyLoad != null);
  }



  @Override
  public void pushUpdatesToCompose() {
    ContainerGraphSetFactory.executeCompose(this, composePlan, connector, true);
  }

  private static Set<String> relatedGraphs(String context, List<Mapping> mappings) {

    HashSet<String> contexts = new HashSet<>();
    contexts.add(context);
    for(Mapping mapping : mappings) {
      if(context.equals(mapping.getGraphname())) {
        for(String included : mapping.getInclusionSet()) {
          contexts.addAll(relatedGraphs(included, mappings));
        }
      }
    }
    return contexts;
  }

  @Override
  public String getCompositionFingerPrint(Set<GraphVar> graphVars) {

    Map<String, Set<String>> hashToContext = connector.listPhiContextsPerHash();
    Map<String, String> contextToHash = new HashMap<>();
    for(String hash : hashToContext.keySet()) {
      Set<String> contexts = hashToContext.get(hash);
      for(String context : contexts) {
        contextToHash.put(context, hash);
      }
    }

    HashSet<String> contexts = new HashSet<>();
    for(GraphVar graphVar : graphVars) {
      String context = contextMap().get(graphVar);
      contexts.addAll(relatedGraphs(context, variables));
    }

    Set<String> hashes = new HashSet<>();
    for(String context : contexts) {
      if(contextToHash.containsKey(context)) {
        hashes.add(contextToHash.get(context));
      }
    }

    return fingerPrint(hashes, "-");
  }
}
