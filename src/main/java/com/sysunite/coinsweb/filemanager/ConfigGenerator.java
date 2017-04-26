package com.sysunite.coinsweb.filemanager;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.sysunite.coinsweb.config.*;
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

    ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
    try {

      ArrayList<Graph> graphs = new ArrayList();
      for(String contentFile : containerFile.getContentFiles()) {

        File file = containerFile.getContentFile(contentFile);

        Graph graph = new Graph();
        graph.setGraphname(namespaceForFile(file));
        graph.setType("container");
        graph.setContent("instances");
        graph.setPath(containerFile.getContentFilePath(contentFile).toString());
        graphs.add(graph);
      }
      for(String repositoryFile : containerFile.getRepositoryFiles()) {

        File file = containerFile.getRepositoryFile(repositoryFile);

        Graph graph = new Graph();
        graph.setGraphname(namespaceForFile(file));
        graph.setType("container");
        graph.setContent("library");
        graph.setPath(containerFile.getRepositoryFilePath(repositoryFile).toString());
        graphs.add(graph);
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

  public static String namespaceForFile(File file) {

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

    for(Resource context : model.contexts()) {
      log.warn("context: " + context);      // todo: this worksssss for nquad :D
    }

    Optional<Namespace> namespace = model.getNamespace("");
    if(namespace.isPresent()) {
      return namespace.get().getName();
    }
    return "http://a"; // todo: point to a default
  }
}
