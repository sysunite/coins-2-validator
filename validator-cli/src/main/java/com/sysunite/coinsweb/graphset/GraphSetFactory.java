package com.sysunite.coinsweb.graphset;

import com.sysunite.coinsweb.connector.Connector;
import com.sysunite.coinsweb.connector.ConnectorFactory;
import com.sysunite.coinsweb.connector.ConnectorFactoryImpl;
import com.sysunite.coinsweb.filemanager.ContainerFileImpl;
import com.sysunite.coinsweb.parser.config.ConfigFile;
import com.sysunite.coinsweb.parser.config.Container;
import com.sysunite.coinsweb.parser.config.Mapping;
import com.sysunite.coinsweb.parser.config.Store;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.helpers.StatementCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

/**
 * @author bastbijl, Sysunite 2017
 */
public class GraphSetFactory {

  private static final Logger log = LoggerFactory.getLogger(GraphSetFactory.class);

  public static ContainerGraphSet lazyLoad(ContainerFileImpl container, Container containerConfig, ConfigFile configFile) {
    Store storeConfig = configFile.getEnvironment().getStore();
    if("none".equals(storeConfig.getType())) {
      return new ContainerGraphSetImpl();
    }

    HashMap<String, String> graphs = new HashMap();
    for(Mapping mapping : configFile.getEnvironment().getGraphs()) {
      graphs.put(mapping.getContent(), mapping.getGraphname());
    }

    log.info("Construct graphset and lazy load connector");
    ConnectorFactory factory = new ConnectorFactoryImpl();
    Connector connector = factory.build(storeConfig);
    ContainerGraphSet graphSet = new ContainerGraphSetImpl(connector, graphs);
    graphSet.setContainerFile(container);
    graphSet.setContainerConfig(containerConfig);
    graphSet.setConfigFile(configFile);
    return graphSet;
  }

  public static ArrayList<String> imports(File file) {

    ArrayList<String> namespaces = new ArrayList();

    Model model = load(file);

    for (Value library : model.filter(null, OWL.IMPORTS, null).objects()) {
      namespaces.add(library.toString());
    }

    return namespaces;
  }

  @Deprecated
  public static Model load(File file) {

    Optional<RDFFormat> format = Rio.getParserFormatForFileName(file.toString());
    if(!format.isPresent()) {
      throw new RuntimeException("Not able to determine format of file: " + file.getName());
    }
    Model model = new LinkedHashModel();
    RDFParser rdfParser = Rio.createParser(format.get());
    rdfParser.setRDFHandler(new StatementCollector(model));

    try {
      rdfParser.parse(new FileInputStream(file), "http://backup");
      return model;
    } catch (FileNotFoundException e) {
      log.error(e.getMessage(), e);
    } catch (IOException e) {
      log.error(e.getMessage(), e);
    }
    throw new RuntimeException("Not able to load model from file");
  }
}
