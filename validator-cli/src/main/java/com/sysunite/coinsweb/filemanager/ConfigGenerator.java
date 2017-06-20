package com.sysunite.coinsweb.filemanager;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature;
import com.sysunite.coinsweb.parser.config.*;
import com.sysunite.coinsweb.steps.ProfileValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

import static com.sysunite.coinsweb.filemanager.ContainerFileImpl.namespacesForFile;

/**
 * @author bastbijl, Sysunite 2017
 */
public class ConfigGenerator {

  private static final Logger log = LoggerFactory.getLogger(ConfigGenerator.class);

  public static String run(ArrayList<ContainerFile> containersList) {
    if(containersList.isEmpty()) {
      throw new RuntimeException("");
    }

    Path localizeTo = containersList.get(0).toPath().getParent();
    return run(containersList, localizeTo);
  }
  public static String run(ArrayList<ContainerFile> containersList, Path localizeTo) {

    ObjectMapper mapper = new ObjectMapper(
      new YAMLFactory()
      .enable(Feature.MINIMIZE_QUOTES)
      .disable(Feature.WRITE_DOC_START_MARKER)
    );

    try {


      ConfigFile configFile = new ConfigFile();

      // Environment
      Store store = new Store();
      store.setType("rdf4j-sail-memory");

      Environment environment = new Environment();
      environment.setStore(store);


      Mapping fullMapping = new Mapping();
      fullMapping.setContent("full");
      fullMapping.setGraphname("http://full/union");

      Mapping instancesMapping = new Mapping();
      instancesMapping.setContent("instances");
      instancesMapping.setGraphname("http://instances/union");

      Mapping libraryMapping = new Mapping();
      libraryMapping.setContent("library");
      libraryMapping.setGraphname("http://library/union");

      Mapping[] mappings = {fullMapping, instancesMapping, libraryMapping};

      environment.setGraphs(mappings);


      // Run
      ArrayList<Container> containers = new ArrayList();
      for(ContainerFile containerFile : containersList) {

        // - Container
        Locator locator = new Locator();
        locator.localizeTo(localizeTo);
        locator.setType("file");
        locator.setPath(containerFile.toString());

        ArrayList<Graph> graphs = graphsInContainer(containerFile);

        Container container = new Container();
        container.setType("container");
        container.setLocation(locator);
        container.setGraphs(graphs.toArray(new Graph[0]));

        containers.add(container);
      }

      // - Steps
      ArrayList<Step> steps = new ArrayList();

      Step fileSystemValidation = new Step();
      fileSystemValidation.setType("FileSystemValidation");
      steps.add(fileSystemValidation);


      Path profileLocation = localizeTo.resolve("profile.lite-9.60.xml");

      Locator validationLocator = new Locator();
      validationLocator.localizeTo(localizeTo);
      validationLocator.setType("file");
      validationLocator.setPath(profileLocation.toString());
      ProfileValidation validation = new ProfileValidation();
      validation.setConfigFile(configFile);
      validation.setProfile(validationLocator);
      String validationSnippet = mapper.writeValueAsString(validation);

      Step profileValidation = new Step();
      profileValidation.setType("ProfileValidation");
      steps.add(profileValidation);

      Step documentReferenceValidation = new Step();
      documentReferenceValidation.setType("DocumentReferenceValidation");
      steps.add(documentReferenceValidation);

      // - Reports
      ArrayList<Report> reports = new ArrayList();

      Path reportLocation = localizeTo.resolve("report.xml");

      Locator reportLocator = new Locator();
      reportLocator.localizeTo(localizeTo);
      reportLocator.setType("file");
      reportLocator.setPath(reportLocation.toString());

      Report report = new Report();
      report.setType("debug");
      report.setLocation(reportLocator);
      reports.add(report);

      Run run = new Run();

      run.setContainers(containers.toArray(new Container[0]));
      run.setSteps(steps.toArray(new Step[0]));
      run.setReports(reports.toArray(new Report[0]));

      // Put it together
      configFile.setEnvironment(environment);
      configFile.setRun(run);

      String yml =  mapper.writeValueAsString(configFile);
      String result = "";

      Scanner scanner = new Scanner(yml);

      while (scanner.hasNextLine()) {
        String line = scanner.nextLine();

        if(line.trim().startsWith("- type: ProfileValidation")) {
          result += line + System.lineSeparator();
          result += "    "+validationSnippet.trim().replace("\n", "\n    ")+"\n";
        } else {
          result += line + System.lineSeparator();
        }
      }
      scanner.close();
      return result;

    } catch (Exception e) {
      log.error(e.getLocalizedMessage(), e);
    }

    throw new RuntimeException("Was not able to generate config.yml");
  }

  public static ArrayList<Graph> graphsInContainer(ContainerFile containerFile) {
    ArrayList<Graph> graphs = new ArrayList();
    graphs.addAll(contentGraphsInContainer(containerFile));
    graphs.addAll(libraryGraphsInContainer(containerFile));
    return graphs;
  }
  public static ArrayList<Graph> contentGraphsInContainer(ContainerFile containerFile) {
    return contentGraphsInContainer(containerFile, new ArrayList<>(Arrays.asList("instances", "full")));
  }
  public static ArrayList<Graph> contentGraphsInContainer(ContainerFile containerFile, ArrayList<String> content) {
    ArrayList<Graph> graphs = new ArrayList();
    for(String contentFile : containerFile.getContentFiles()) {

      File file = containerFile.getContentFile(contentFile);
      try {
        for (String namespace : namespacesForFile(file)) {

          Graph graph = new Graph();
          graph.setGraphname(namespace);
          graph.setType("container");
          graph.setContent(content);
          graph.setPath(containerFile.getContentFilePath(contentFile).toString());
          graphs.add(graph);
        }
      } catch (RuntimeException e) {
        log.warn(e.getMessage());
      }
    }
    return graphs;
  }
  public static ArrayList<Graph> libraryGraphsInContainer(ContainerFile containerFile) {
    return libraryGraphsInContainer(containerFile, new ArrayList<>(Arrays.asList("library", "full")));
  }
  public static ArrayList<Graph> libraryGraphsInContainer(ContainerFile containerFile, ArrayList<String> content) {
    ArrayList<Graph> graphs = new ArrayList();
    for(String repositoryFile : containerFile.getRepositoryFiles()) {

      File file = containerFile.getRepositoryFile(repositoryFile);
      try {
        for(String namespace : namespacesForFile(file)) {

          Graph graph = new Graph();
          graph.setGraphname(namespace);
          graph.setType("container");
          graph.setContent(content);
          graph.setPath(containerFile.getRepositoryFilePath(repositoryFile).toString());
          graphs.add(graph);
        }
      } catch (RuntimeException e) {
        log.warn(e.getMessage());
      }
    }
    return graphs;
  }
}
