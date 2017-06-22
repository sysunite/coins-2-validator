package com.sysunite.coinsweb.rdfutil;

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
import java.util.ArrayList;
import java.util.Optional;

/**
 * @author bastbijl, Sysunite 2017
 */
public class Utils {

  private static final Logger log = LoggerFactory.getLogger(Utils.class);

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

  public static String withoutHash(String input) {
    while(input.endsWith("#")) {
      input = input.substring(0, input.length()-1);
    }
    return input;
  }
}
