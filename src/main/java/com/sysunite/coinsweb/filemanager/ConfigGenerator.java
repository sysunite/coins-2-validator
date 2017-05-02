package com.sysunite.coinsweb.filemanager;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature;
import com.sysunite.coinsweb.parser.config.*;
import org.apache.log4j.Logger;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
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
import java.util.Scanner;

/**
 * @author bastbijl, Sysunite 2017
 */
public class ConfigGenerator {

  private static final Logger log = Logger.getLogger(ConfigGenerator.class);

  public static String run(ContainerFile containerFile) {

    String result = "";

    ObjectMapper mapper = new ObjectMapper(new YAMLFactory().enable(Feature.MINIMIZE_QUOTES));
    try {

      ArrayList<Graph> graphs = new ArrayList();
      for(String contentFile : containerFile.getContentFiles()) {

        File file = containerFile.getContentFile(contentFile);
        for(String namespace : namespacesForFile(file)) {

          Graph graph = new Graph();
          graph.setGraphname(namespace);
          graph.setType("container");
          graph.setContent("instances");
          graph.setPath(containerFile.getContentFilePath(contentFile).toString());
          graphs.add(graph);
        }
      }
      for(String repositoryFile : containerFile.getRepositoryFiles()) {

        File file = containerFile.getRepositoryFile(repositoryFile);

        for(String namespace : namespacesForFile(file)) {

          Graph graph = new Graph();
          graph.setGraphname(namespace);
          graph.setType("container");
          graph.setContent("library");
          graph.setPath(containerFile.getRepositoryFilePath(repositoryFile).toString());
          graphs.add(graph);
        }
      }

      Locator locator = new Locator();
      locator.setType("file");
      locator.setPath(containerFile.toString());

      Container container = new Container();
      container.setType("container");
      container.setLocation(locator);
      container.setGraphs(graphs.toArray(new Graph[graphs.size()]));

      Run run = new Run();
      Container[] containers = {container};
      run.setContainers(containers);

      ConfigFile configFile = new ConfigFile();
      configFile.setRun(run);

      String yml = mapper.writeValueAsString(configFile);

      Scanner scanner = new Scanner(yml);

      boolean writing = false;

      while (scanner.hasNextLine()) {
        String line = scanner.nextLine();

        // Skip the first lines
        if(!writing) {
          if(line.startsWith("run:")) {
            writing = true;
          }
        }

        if(writing) {
          // Skip the last lines
          if(line.startsWith("  steps: null")) {
            break;
          }
          // Do the writing
          result += line + System.lineSeparator();
        }
      }
      scanner.close();

    } catch (Exception e) {
      log.error(e.getLocalizedMessage(), e);
    }

    return result;
  }

  public static ArrayList<String> namespacesForFile(File file) {

    ArrayList<String> namespaces = new ArrayList();

    Optional<RDFFormat> format = Rio.getParserFormatForFileName(file.toString());
    if(!format.isPresent()) {
      throw new RuntimeException("Not able to determine format of file: " + file.getName());
    }
    Model model = new LinkedHashModel();
    RDFParser rdfParser = Rio.createParser(format.get());
    rdfParser.setRDFHandler(new StatementCollector(model));

    try {
      rdfParser.parse(new FileInputStream(file), "http://backup");
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }

    // If there are contexts, use these
    for(Resource context : model.contexts()) {
      if(context != null) {
        namespaces.add(context.toString());
      }
    }

    // If no contexts, use the empty namespace
    if(namespaces.size() < 1) {
      Optional<Namespace> namespace = model.getNamespace("");
      if (namespace.isPresent()) {
        namespaces.add(namespace.get().getName());
      }
    }

    // If still no namespace
    if(namespaces.size() < 1) {
      throw new RuntimeException("No namespace found to represent this file.");
    }
    return namespaces;
  }
}
