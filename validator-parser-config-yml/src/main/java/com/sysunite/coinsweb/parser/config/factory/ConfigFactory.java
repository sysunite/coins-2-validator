package com.sysunite.coinsweb.parser.config.factory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature;
import com.sysunite.coinsweb.filemanager.ContainerFile;
import com.sysunite.coinsweb.filemanager.ContainerFileImpl;
import com.sysunite.coinsweb.parser.config.pojo.*;
import com.sysunite.coinsweb.rdfutil.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

/**
 * @author bastbijl, Sysunite 2017
 */
public class ConfigFactory {

  private static final Logger log = LoggerFactory.getLogger(ConfigFactory.class);

  public static String getDefaultConfigString(ConfigFile configFile) {

    ObjectMapper mapper = new ObjectMapper(
    new YAMLFactory()
    .enable(Feature.MINIMIZE_QUOTES)
    .disable(Feature.WRITE_DOC_START_MARKER)
    );

    // Todo: find a nicer way to do this
    String validationSnippet = "profile:\n" +
    "  type: file\n" +
    "  path: profile.lite-9.60.xml\n" +
    "maxResults: 0";

    try {
      String yml = mapper.writeValueAsString(configFile);
      String result = "";

      Scanner scanner = new Scanner(yml);

      while (scanner.hasNextLine()) {
        String line = scanner.nextLine();

        if (line.trim().startsWith("- type: ProfileValidation")) {
          result += line + System.lineSeparator();
          result += "    " + validationSnippet.trim().replace("\n", "\n    ") + "\n";
        } else {
          result += line + System.lineSeparator();
        }
      }
      scanner.close();
      return result;
    } catch (JsonProcessingException e) {
      log.error(e.getLocalizedMessage(), e);
    }
    throw new RuntimeException("Was not able to generate config.yml");
  }

  public static ConfigFile getDefaultConfig(ArrayList<ContainerFile> containersList) {
    Path localizeTo = containersList.get(0).toPath().getParent();
    return getDefaultConfig(containersList, localizeTo);
  }
  public static ConfigFile getDefaultConfig(ArrayList<ContainerFile> containersList, Path localizeTo) {
    ConfigFile configFile = new ConfigFile();

    configFile.setEnvironment(getDefaultEnvironment());
    configFile.setRun(getDefaultRun(containersList, localizeTo));

    return configFile;
  }

  public static Step[] getDefaultSteps() {
    ArrayList<Step> steps = new ArrayList();

    Step fileSystemValidation = new Step();
    fileSystemValidation.setType("FileSystemValidation");
    steps.add(fileSystemValidation);

    Step documentReferenceValidation = new Step();
    documentReferenceValidation.setType("DocumentReferenceValidation");
    steps.add(documentReferenceValidation);


//      Path profileLocation = localizeTo.resolve("profile.lite-9.60.xml");
//
//      Locator validationLocator = new Locator();
//      validationLocator.localizeTo(localizeTo);
//      validationLocator.setType("file");
//      validationLocator.setPath(profileLocation.toString());
//      ProfileValidation validation = new ProfileValidation();
//      validation.setConfigFile(configFile);
//      validation.setProfile(validationLocator);
//      String validationSnippet = mapper.writeValueAsString(validation);


    Step profileValidation = new Step();
    profileValidation.setType("ProfileValidation");
    steps.add(profileValidation);

    return steps.toArray(new Step[0]);
  }

  public static Environment getDefaultEnvironment() {

    Store store = new Store();
    store.setType("rdf4j-sail-memory");

    Environment environment = new Environment();
    environment.setStore(store);

    Mapping fullMapping = new Mapping();
    fullMapping.setVariable("FULL_UNION_GRAPH");
    fullMapping.setGraphname("http://full/union");

    Mapping instancesMapping = new Mapping();
    instancesMapping.setVariable("INSTANCE_UNION_GRAPH");
    instancesMapping.setGraphname("http://instances/union");

    Mapping libraryMapping = new Mapping();
    libraryMapping.setVariable("SCHEMA_UNION_GRAPH");
    libraryMapping.setGraphname("http://library/union");

    Mapping[] mappings = {fullMapping, instancesMapping, libraryMapping};

    environment.setGraphs(mappings);
    return environment;
  }

  public static Report[] getDefaultReports(Path localizeTo) {

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

    return reports.toArray(new Report[0]);
  }

  public static Run getDefaultRun(ArrayList<ContainerFile> containersList, Path localizeTo) {


    ArrayList<Container> containers = new ArrayList();
    for(ContainerFile containerFile : containersList) {
      containers.add(describe(containerFile, localizeTo));
    }

    Run run = new Run();

    run.setContainers(containers.toArray(new Container[0]));
    run.setSteps(getDefaultSteps());
    run.setReports(getDefaultReports(localizeTo));

    return run;
  }

  public static Container describe(ContainerFile containerFile, Path localizeTo) {

    Locator locator = new Locator();
    locator.localizeTo(localizeTo);
    locator.setType("file");
    locator.setPath(containerFile.toString());

    ArrayList<Graph> graphs = graphsInContainer(containerFile);

    Container container = new Container();
    container.setType("container");
    container.setLocation(locator);
    container.setGraphs(graphs.toArray(new Graph[0]));

    return container;
  }










  public static ArrayList<Graph> graphsInContainer(ContainerFile containerFile) {
    ArrayList<Graph> graphs = new ArrayList();
    graphs.addAll(contentGraphsInContainer(containerFile));
    graphs.addAll(libraryGraphsInContainer(containerFile));
    return graphs;
  }
  public static ArrayList<Graph> contentGraphsInContainer(ContainerFile containerFile) {
    return contentGraphsInContainer(containerFile, new ArrayList<>(Arrays.asList("INSTANCE_UNION_GRAPH", "FULL_UNION_GRAPH")));
  }
  public static ArrayList<Graph> contentGraphsInContainer(ContainerFile containerFile, ArrayList<String> content) {
    ArrayList<Graph> graphs = new ArrayList();
    for(String contentFile : containerFile.getContentFiles()) {

      File file = containerFile.getContentFile(contentFile);
      try {
        for (String namespace : Utils.namespacesForFile(file)) {

          Source source = new Source();
          source.setType("container");
          source.setPath(containerFile.getContentFilePath(contentFile).toString());
          source.setGraphname(namespace);

          Graph graph = new Graph();
          graph.setSource(source);
          graph.setAs(content);
          graphs.add(graph);
        }
      } catch (RuntimeException e) {
        log.warn(e.getMessage());
      }
    }
    return graphs;
  }
  public static ArrayList<Graph> libraryGraphsInContainer(ContainerFile containerFile) {
    return libraryGraphsInContainer(containerFile, new ArrayList<>(Arrays.asList("SCHEMA_UNION_GRAPH", "FULL_UNION_GRAPH")));
  }
  public static ArrayList<Graph> libraryGraphsInContainer(ContainerFile containerFile, ArrayList<String> content) {
    ArrayList<Graph> graphs = new ArrayList();
    for(String repositoryFile : containerFile.getRepositoryFiles()) {

      File file = containerFile.getRepositoryFile(repositoryFile);
      try {
        for(String namespace : Utils.namespacesForFile(file)) {

          Source source = new Source();
          source.setType("container");
          source.setPath(containerFile.getRepositoryFilePath(repositoryFile).toString());
          source.setGraphname(namespace);

          Graph graph = new Graph();
          graph.setSource(source);
          graph.setAs(content);
          graphs.add(graph);
        }
      } catch (RuntimeException e) {
        log.warn(e.getMessage());
      }
    }
    return graphs;
  }

  public static void overrideContainers(ConfigFile configFile, int count, Path[] containerFiles) {
    if(configFile.getRun().getContainers().length != 1) {
      throw new RuntimeException("When overriding the container location from config file please configure precisely one container-to-override in the config.yml");
    }

    Container blueprint = configFile.getRun().getContainers()[0];
    Container[] overriddenContainerSet = new Container[count];
    for(int i = 0; i < count; i++) {
      overriddenContainerSet[i] = blueprint.clone();
      String location = containerFiles[i].toString();
      log.info("Use specified container config to load overriding container path: "+location);
      overriddenContainerSet[i].getLocation().setPath(location);
    }
    configFile.getRun().setContainers(overriddenContainerSet);
  }



  public static void expandGraphConfig(ConfigFile configFile) {
    for(Container container : configFile.getRun().getContainers()) {
      ContainerFile containerFile = null;
      if(!container.isVirtual()) {
        containerFile = new ContainerFileImpl(FileFactory.toFile(container.getLocation()).getPath());
      }
      container.setGraphs(loadList(container.getGraphs(), containerFile).toArray(new Graph[0]));
    }
  }

  public static ArrayList<Graph> loadList(Graph[] originalGraphs, ContainerFile container) {

//    ArrayList<Graph> loadList = new ArrayList();

    Graph allContentFile = null;
    Graph allLibraryFile = null;

    // Explicit graphs
    ArrayList<String> explicitGraphs = new ArrayList();
    for(Graph graph : originalGraphs) {
      if(!graph.getSource().anyGraph()) {
        String graphName = graph.getSource().getGraphname();
        if(explicitGraphs.contains(graphName)) {
          throw new RuntimeException("The namespace "+graphName+ " is being mentioned more than once, this is not allowed");
        }
        explicitGraphs.add(graphName);
      }

      if(Source.CONTAINER.equals(graph.getSource().getType())) {

        // Keep track of fallback graph definitions
        if (graph.getSource().anyContentFile()) {
          if (allContentFile != null) {
            throw new RuntimeException("Only one graph with content file asterisk allowed");
          }
          allContentFile = graph;
        }
        if (graph.getSource().anyLibraryFile()) {
          if (allLibraryFile != null) {
            throw new RuntimeException("Only one graph with content file asterisk allowed");
          }
          allLibraryFile = graph;
        }
      }
    }

    // Implicit graphs
    ArrayList<Graph> loadList = new ArrayList();
    ArrayList<String> implicitGraphs = new ArrayList();

    if(allContentFile != null) {
      for(Graph graph : ConfigFactory.contentGraphsInContainer(container, allContentFile.getAs())) {
        String graphName = graph.getSource().getGraphname();
        if(!explicitGraphs.contains(graphName)) {
          log.info("Will load content file from wildcard definition");
          if(implicitGraphs.contains(graphName)) {
            throw new RuntimeException("Collision in implicit graphs names, this one can be found in more than one source: "+graphName);
          }
          implicitGraphs.add(graphName);
          loadList.add(graph);
        }
      }
    }

    if(allLibraryFile != null) {
      for(Graph graph : ConfigFactory.libraryGraphsInContainer(container, allLibraryFile.getAs())) {
        String graphName = graph.getSource().getGraphname();
        if(!explicitGraphs.contains(graphName)) {
          log.info("Will load library file from wildcard definition");
          if(implicitGraphs.contains(graphName)) {
            throw new RuntimeException("Collision in implicit graphs names, this one can be found in more than one source: "+graphName);
          }
          implicitGraphs.add(graphName);
          loadList.add(graph);
        }
      }
    }

    // If a graph points to a file or link online instead of a file in a container
    for(Graph originalGraph : originalGraphs) {
      if(originalGraph.getSource().anyGraph() &&
        (Source.FILE.equals(originalGraph.getSource().getType()) || Source.ONLINE.equals(originalGraph.getSource().getType()))) {

        File file = FileFactory.toFile(originalGraph.getSource().asLocator());
        for(String graphName : Utils.namespacesForFile(file)) {
          if(!explicitGraphs.contains(graphName)) {
            log.info("Will load graph from file because of wildcard graph definition");
            if(implicitGraphs.contains(graphName)) {
              throw new RuntimeException("Collision in implicit graphs names, this one can be found in more than one source: "+graphName);
            }
            implicitGraphs.add(graphName);

            Graph graph = originalGraph.clone();
            graph.getSource().setGraphname(graphName);
            loadList.add(graph);
          }
        }
      }
    }

    for(Graph graph : originalGraphs) {
      if(!graph.getSource().anyGraph()) {

        // Check if the file in the container is available
        if(Source.CONTAINER.equals(graph.getSource().getType())) {
          try {
            container.getFile(Paths.get(graph.getSource().getPath()));
          } catch(RuntimeException e) {
            throw e;
          }
        }

        log.info("Will load explicitly defined file");
        loadList.add(graph);
      }
    }
    return loadList;
  }
}
