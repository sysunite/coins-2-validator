package com.sysunite.coinsweb.graphset;

import com.sysunite.coinsweb.connector.Connector;
import com.sysunite.coinsweb.filemanager.ContainerFile;
import com.sysunite.coinsweb.parser.config.Container;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.util.Models;
import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.helpers.StatementCollector;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;

/**
 * @author bastbijl, Sysunite 2017
 */
public class GraphSetFactory {
  public static ContainerGraphSet loadContainer(ContainerFile container, Connector connector, Container containerConfig) {
    return new ContainerGraphSet();
  }

  public static ArrayList<String> imports(File file) {

    ArrayList<String> namespaces = new ArrayList();

    Model model = load(file);

    for (Resource subject : model.filter(null, OWL.IMPORTS, null).subjects()) {
      for(IRI object : Models.getPropertyIRIs(model, subject, OWL.IMPORTS)) {
        namespaces.add(object.toString());
      }
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
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    throw new RuntimeException("Not able to load model from file");
  }
}
