package com.sysunite.coinsweb.graphset;

import com.sysunite.coinsweb.connector.Connector;
import com.sysunite.coinsweb.filemanager.ContainerFile;
import com.sysunite.coinsweb.filemanager.DescribeFactoryImpl;
import com.sysunite.coinsweb.parser.config.pojo.ConfigFile;
import com.sysunite.coinsweb.parser.config.pojo.Container;
import com.sysunite.coinsweb.parser.config.pojo.Graph;
import org.eclipse.rdf4j.query.BindingSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author bastbijl, Sysunite 2017
 */
public class ContainerGraphSetImpl implements ContainerGraphSet {

  private static final Logger log = LoggerFactory.getLogger(ContainerGraphSetImpl.class);


  protected boolean initialized = false;
  private boolean disabled;
  private Connector connector;
  private ContainerFile container;
  private Container containerConfig;
  private ConfigFile configFile;
  private HashMap<GraphVar, String> contextMap;

  private Graph main;


  public ContainerGraphSetImpl() {
    this.disabled = true;
  }

  public ContainerGraphSetImpl(Connector connector) {
    this.disabled = false;
    this.connector = connector;
  }

  public Object getMain() {
    return main;
  }
  public void setMain(Object main) {
    this.main = (Graph) main;
  }

  // Execute postponed lazy load
  public void load() {

    // Essential step to get rid of the wildcards in the configFile
    log.info("Will now expand any wildcard usage in the config.yml section of this file");
    DescribeFactoryImpl.expandGraphConfig(containerConfig);

    // Now test if
    if(!GraphSetFactory.testCompose(containerConfig.getGraphs(), containerConfig.getVariablesMap())) {
      throw new RuntimeException("The ContainerGraphSet can not be loaded because the graph description composition plan contains an error");
    }

    if(disabled) {
      return;
    }

    log.info("Load stuff to connector");
    contextMap = GraphSetFactory.load(containerConfig, connector, container, configFile);

    this.setAllLoaded();
  }

  /**
   * returns true if it least one result was found
   */
  public List<Object> select(String query) {

    if(requiresLoad()) {
      load();
    }

    List<Object> result = connector.query(query);
    return result;
  }


  public Map<GraphVar, String> contextMap() {
    if(requiresLoad()) {
      load();
    }
    return contextMap;
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

  public List<String> getImports(GraphVar graphVar) {
    if(requiresLoad()) {
      load();
    }

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
    List<Object> result = select(query);
    for (Object bindingSet : result) {

      String namespace = ((BindingSet)bindingSet).getBinding("library").getValue().stringValue();
      log.info("Found import: "+namespace);
      namespaces.add(namespace);
    }
    return namespaces;
  }




  public void update(String query) {
    if(requiresLoad()) {
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

  /**
   * Remove the contexts (graphs) from the connection that belong to this graphSet
   */
  @Override
  public void cleanup() {


    if(!requiresLoad()) {

      ArrayList<String> contexts = new ArrayList<>();
      log.info("Will wipe these graphs (if enabled) to remove GraphSet graphs from the Connector:");
      for(String context : contextMap.values()) {
        log.info("- "+context);
        contexts.add(context);
      }

      connector.cleanup(contexts.toArray(new String[0]));
    }
  }

  @Override
  public String graphExists(GraphVar graphVar) {
    if(requiresLoad()) {
      load();
    }
    String context = contextMap().get(graphVar);
    return connector.graphExists(context);
  }

  public boolean requiresLoad() {
    return !initialized;
  }
  public void setAllLoaded() {
    initialized = true;
  }

  public void writeContextToFile(String[] contexts, OutputStream outputStream) {
    connector.writeContextsToFile(contexts, outputStream);
  }
}
