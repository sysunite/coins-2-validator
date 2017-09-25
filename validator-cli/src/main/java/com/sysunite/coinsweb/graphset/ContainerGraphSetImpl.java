package com.sysunite.coinsweb.graphset;

import com.sysunite.coinsweb.connector.Connector;
import com.sysunite.coinsweb.connector.ConnectorException;
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


  private ContainerFileImpl containerToLazyLoad = null;
  private Map<String, Set<GraphVar>> inferencePreference;

  private boolean disabled;
  private boolean loadingFailed = false;
  private Connector connector;
  private ComposePlan composePlan;

  private GraphVarImpl main;

  private List<Mapping> mappings;


  public ContainerGraphSetImpl(List<Mapping> mappings) {
    this.mappings = mappings;
    this.disabled = true;
  }

  public ContainerGraphSetImpl(List<Mapping> mappings, Connector connector) {
    this.mappings = mappings;
    this.connector = connector;
    this.disabled = false;
  }

  public Object getMain() {
    return main;
  }
  public void setMain(Object main) {
    this.main = (GraphVarImpl) main;
  }





  @Override
  public void lazyLoad(ContainerFile container, Map<String, Set<GraphVar>> inferencePreference) {
    this.containerToLazyLoad = (ContainerFileImpl)container;
    this.inferencePreference = inferencePreference;
  }

  // Execute postponed lazy load
  @Override
  public void load() {

    if(!requiresLoad()) {
      return;
    }

    // Essential step to get rid of the wildcards in the configFile
    log.info("Will now expand any wildcard usage in the config.yml section of this file");
    try {
      DescribeFactoryImpl.expandGraphConfig(containerToLazyLoad.getConfig(), containerToLazyLoad);
    } catch (RuntimeException e) {
      log.error(e.getMessage());
      setFailed();
      return;
    }

    if(disabled) {
      return;
    }

    log.info("Load stuff to connector");
    composePlan = ContainerGraphSetFactory.load(this, connector, containerToLazyLoad, inferencePreference);

    log.trace("Will use this compose plan: " + ReportFactory.buildJson(mappings));


    containerToLazyLoad.getConfig().setVariables(mappings);
    this.containerToLazyLoad = null;
  }

  public Connector getConnector() {
    return connector;
  }


  public List<Object> select(String query) throws ConnectorException {

    if(requiresLoad()) {
      load();
    }

    List<Object> result = connector.select(query);
    return result;
  }
  public List<Object> select(String query, long limit) throws ConnectorException {

    if(requiresLoad()) {
      load();
    }

    List<Object> result = connector.select(query, limit);
    return result;
  }










  public void setVariables(List<Mapping> mappings) {
    this.mappings = mappings;
  }
  public List<Mapping> getMappings() {
    return mappings;
  }

  public Map<GraphVar, String> contextMap() {
    if(requiresLoad()) {
      load();
    }

    HashMap<GraphVar, String> graphs = new HashMap();
    for(Mapping mapping : mappings) {
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
    Map<String, String> result = new HashMap<>();
    if(requiresLoad()) {
      load();
    }
    String context = contextMap().get(graphVar);
    try {
      result = connector.getImports(context);
    } catch (ConnectorException e) {
      log.error("Failed determining imports", e);
    }
    return result;
  }




  public boolean update(String query) {
    if(requiresLoad()) {
      load();
    }
    try {
      connector.update(query);
    } catch (ConnectorException e) {
      log.error("Update query failed", e);
      return false;
    }
    return true;
  }





  /**
   * Remove the contexts (graphs) from the connection that belong to this graphSet
   */
  @Override
  public void cleanup() {


    if(!requiresLoad()) {

//      HashSet<String> contexts = new HashSet<>();
//      log.info("Will wipe these graphs (if enabled) to remove GraphSet graphs from the Connector:");
//      for(Mapping mapping : mappings) {
//        if(mapping.getGraphname() != null) {
//          contexts.add(mapping.getGraphname());
//        }
//        for(String context : mapping.getInclusionSet()) {
//          if(context != null) {
//            contexts.add(context);
//          }
//        }
//      }
//      for(String context : contexts) {
//        log.info("- "+context);
//      }

      try {
        connector.wipe();
      } catch (ConnectorException e) {
        log.error("Cleanup failed");
      }
    }
  }



  public boolean requiresLoad() {
    return (containerToLazyLoad != null);
  }

  public void setFailed() {
    loadingFailed = true;
  }
  public boolean loadingFailed() {
    return loadingFailed;
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

    Map<String, Set<String>> hashToContext;
    try {
      hashToContext = connector.listPhiContextsPerHash();
    } catch (ConnectorException e) {
      log.error("Failed building composition fingerprint", e);
      return null;
    }
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
      contexts.addAll(relatedGraphs(context, mappings));
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
