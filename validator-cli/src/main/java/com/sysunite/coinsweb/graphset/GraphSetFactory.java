package com.sysunite.coinsweb.graphset;

import com.sysunite.coinsweb.connector.ConnectorFactory;
import com.sysunite.coinsweb.connector.ConnectorFactoryImpl;
import com.sysunite.coinsweb.filemanager.ContainerFileImpl;
import com.sysunite.coinsweb.parser.config.Container;
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




  public static ContainerGraphSetImpl lazyLoad(ContainerFileImpl container, Store storeConfig, Container containerConfig) {
    if("none".equals(storeConfig.getType())) {
      return new ContainerGraphSetImpl();
    }
    ConnectorFactory factory = new ConnectorFactoryImpl();
    return new ContainerGraphSetImpl(factory.build(storeConfig));
  }

  public static ArrayList<String> imports(File file) {

    ArrayList<String> namespaces = new ArrayList();

    Model model = load(file);

    for (Value library : model.filter(null, OWL.IMPORTS, null).objects()) {
      namespaces.add(library.toString());
    }

    return namespaces;
  }

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

  public static Map<String, Long> diffNumTriples(Map<String, Long> oldValues, Map<String, Long> newValues) {
    HashMap<String, Long> result = new HashMap<>();

    Iterator<String> graphNameIterator = newValues.keySet().iterator();
    while(graphNameIterator.hasNext()) {
      String graphName = graphNameIterator.next();

      Long oldValue;
      if(oldValues.containsKey(graphName)) {
        oldValue = oldValues.get(graphName);
      } else {
        oldValue = 0l;
      }
      Long newValue = newValues.get(graphName);
      result.put(graphName, newValue - oldValue);
    }
    return result;
  }
}
