package com.sysunite.coinsweb.graphset;

import com.sysunite.coinsweb.connector.Connector;
import com.sysunite.coinsweb.filemanager.ContainerFile;
import com.sysunite.coinsweb.filemanager.ContainerFileImpl;
import com.sysunite.coinsweb.filemanager.DescribeFactoryImpl;
import com.sysunite.coinsweb.parser.config.pojo.ConfigFile;
import com.sysunite.coinsweb.parser.config.pojo.Container;
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
  private ConfigFile configFile;
  private ComposePlan composePlan;

  private GraphVarImpl main;

  private Container containerConfig;


  public ContainerGraphSetImpl(Container containerConfig) {
    this.containerConfig = containerConfig;
    this.disabled = true;
  }

  public ContainerGraphSetImpl(Container containerConfig, Connector connector) {
    this.containerConfig = containerConfig;
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
    composePlan = ContainerGraphSetFactory.load(connector, lazyLoad, inferencePreference);

    log.info("Tried compose plan: " + ReportFactory.buildJson(composePlan));
    List<Mapping> updatedVarMap = composePlan.getVarMap();
    lazyLoad.getConfig().setVariables(updatedVarMap);

    this.lazyLoad = null;

    if(composePlan.isFailed()) {
      disabled = true;
      throw new RuntimeException("Something went wrong in loading the GraphSet");
    }

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


  public Map<GraphVar, String> contextMap() {
    if(requiresLoad()) {
      load();
    }
    return containerConfig.getVariablesContextMap();
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



  @Override
  public void setConfigFile(Object configFile) {
    if(configFile instanceof ConfigFile) {
      this.configFile = (ConfigFile) configFile;
      return;
    }
    throw new RuntimeException("The object is not an instance of a ConfigFile ");
  }

  /**
   * Remove the contexts (graphs) from the connection that belong to this graphSet
   */
  @Override
  public void cleanup() {


    if(!requiresLoad()) {

      ArrayList<String> contexts = new ArrayList<>();
      log.info("Will wipe these graphs (if enabled) to remove GraphSet graphs from the Connector:");
      for(String context : containerConfig.getVariablesContextMap().values()) {
        log.info("- "+context);
        contexts.add(context);
      }

      connector.cleanup(contexts);
    }
  }

//  @Override
//  public String graphExists(GraphVar graphVar) {
//    if(requiresLoad()) {
//      load();
//    }
//    String context = contextMap().get(graphVar);
//    return connector.graphExists(context);
//  }

  public boolean requiresLoad() {
    return (lazyLoad != null);
  }

//  public void writeContextToFile(List<String> contexts, OutputStream outputStream) {
//    connector.writeContextsToFile(contexts, outputStream);
//  }
//  public void writeContextToFile(List<String> contexts, OutputStream outputStream, Function filter) {
//    connector.writeContextsToFile(contexts, outputStream, filter);
//  }

  @Override
  public void pushUpdatesToCompose() {
    ContainerGraphSetFactory.executeCompose(composePlan, connector, false);
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

    Map<String, Set<String>> hashToContext = connector.listPhiSourceIdsPerHash();
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
      contexts.addAll(relatedGraphs(context, composePlan.getVarMap()));
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
