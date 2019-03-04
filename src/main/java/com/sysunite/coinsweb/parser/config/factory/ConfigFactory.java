package com.sysunite.coinsweb.parser.config.factory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature;
import com.sysunite.coinsweb.parser.config.pojo.*;
import com.sysunite.coinsweb.steps.ValidationStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.util.*;

/**
 * @author bastbijl, Sysunite 2017
 */
public class ConfigFactory {
  private static final Logger log = LoggerFactory.getLogger(ConfigFactory.class);

  private static DescribeFactory describeFactory = null;

  public static void setDescribeFactory(DescribeFactory factory) {
    ConfigFactory.describeFactory = factory;
  }

  public static String toYml(Object configFile) {

    ObjectMapper mapper = new ObjectMapper(
    new YAMLFactory()
    .enable(Feature.MINIMIZE_QUOTES)
    .disable(Feature.WRITE_DOC_START_MARKER)
    );

    try {
      String yml = mapper.writeValueAsString(configFile);
      return yml;

    } catch (JsonProcessingException e) {
      log.error(e.getLocalizedMessage(), e);
    }
    throw new RuntimeException("Was not able to map object to yml");
  }

  public static ConfigFile getDefaultConfig(ArrayList<File> containersList) {
    Path localizeTo = containersList.get(0).toPath().getParent();
    return getDefaultConfig(containersList, localizeTo);
  }
  public static ConfigFile getDefaultConfig(List<File> containersList, Path localizeTo) {
    ConfigFile configFile = new ConfigFile(localizeTo);

    configFile.setEnvironment(getDefaultEnvironment());
    configFile.setRun(getDefaultRun(containersList));

    return configFile;
  }

  public static ArrayList<Mapping> getDefaultMapping(ArrayList<Graph> graphs) {

    Set<GraphVarImpl> varSet = new HashSet();
    ArrayList<Mapping> mappings = new ArrayList();
    for(Graph graph : graphs) {
      if(graph.getAs().size() != 1) {
        throw new RuntimeException("Can not generate mapping from graphs if for some graph more (or less) than one graphVar is set");
      }
      GraphVarImpl graphVar = graph.getAs().get(0);
      if(varSet.contains(graphVar)) {
        throw new RuntimeException("The graphVar \""+graphVar+"\" was already used, can not generate a mapping based on this graphs");
      }
      Mapping mapping = new Mapping();
      mapping.setVariable(graphVar);
      mapping.setGraphname(graph.getSource().getGraphname());
      mappings.add(mapping);
    }
    return mappings;
  }

  public static Container[] getDefaultContainers(List<File> containersList) {
    ArrayList<Container> containers = new ArrayList();
    for(File containerFile : containersList) {
      containers.add(getDefaultContainer(containerFile));
    }
    return containers.toArray(new Container[0]);
  }

  public static ValidationStep[] getDefaultSteps() {

    if(StepDeserializer.factory != null && StepDeserializer.factory.getDefaultSteps() != null) {
      return StepDeserializer.factory.getDefaultSteps();
    }

    // If the factory is not registered, generate using stubs
    ArrayList<ValidationStep> steps = new ArrayList();

    StepStub fileSystemValidation = new StepStub();
    fileSystemValidation.setType("FileSystemValidation");
    steps.add(fileSystemValidation);

    StepStub documentReferenceValidation = new StepStub();
    documentReferenceValidation.setType("DocumentReferenceValidation");
    steps.add(documentReferenceValidation);

    StepStub profileValidation = new StepStub();
    profileValidation.setType("ProfileValidation");
    steps.add(profileValidation);

    return steps.toArray(new StepStub[0]);
  }

  public static Environment getDefaultEnvironment() {

    Store store = new Store();
    store.setType("rdf4j-sail-memory");

    Environment environment = new Environment();
    environment.setStore(store);

    return environment;
  }

  public static Report[] getDefaultReports() {

    ArrayList<Report> reports = new ArrayList();

    Locator reportLocator = new Locator();
    reportLocator.setType("file");
    reportLocator.setPath("report.html");

    Report report = new Report();
    report.setType(Report.HTML);
    report.setLocation(reportLocator);
    reports.add(report);

    return reports.toArray(new Report[0]);
  }

  public static Container getDefaultContainer(File containerFile) {

    Locator locator = new Locator();
    locator.setType("file");
    locator.setPath(containerFile.toString());

    Mapping fullMapping = new Mapping();
    fullMapping.setVariable(new GraphVarImpl("FULL_UNION_GRAPH"));
    fullMapping.setGraphname("http://full/union");

    Mapping instancesMapping = new Mapping();
    instancesMapping.setVariable(new GraphVarImpl("INSTANCE_UNION_GRAPH"));
    instancesMapping.setGraphname("http://instances/union");

    Mapping libraryMapping = new Mapping();
    libraryMapping.setVariable(new GraphVarImpl("SCHEMA_UNION_GRAPH"));
    libraryMapping.setGraphname("http://library/union");

    ArrayList<Mapping> variables = new ArrayList<>();
    variables.add(fullMapping);
    variables.add(instancesMapping);
    variables.add(libraryMapping);

    ArrayList<Graph> graphs;
    if(describeFactory != null) {
      ArrayList<GraphVarImpl> dataGraphs = new ArrayList<>(Arrays.asList(new GraphVarImpl("INSTANCE_UNION_GRAPH"), new GraphVarImpl("FULL_UNION_GRAPH")));
      ArrayList<GraphVarImpl> schemaGraphs = new ArrayList<>(Arrays.asList(new GraphVarImpl("SCHEMA_UNION_GRAPH"), new GraphVarImpl("FULL_UNION_GRAPH")));
      graphs = describeFactory.graphsInContainerFile(containerFile, dataGraphs, schemaGraphs);
    } else {
      graphs = new ArrayList();

      Source instanceGraphSource = new Source();
      instanceGraphSource.setType("container");
      instanceGraphSource.setPath("bim/*");
      instanceGraphSource.setGraphname("*");

      Graph instanceGraphPattern = new Graph();
      instanceGraphPattern.setSource(instanceGraphSource);
      ArrayList<GraphVarImpl> dataList = new ArrayList();
      dataList.add(new GraphVarImpl("INSTANCE_UNION_GRAPH"));
      dataList.add(new GraphVarImpl("FULL_UNION_GRAPH"));
      instanceGraphPattern.setAs(dataList);
      graphs.add(instanceGraphPattern);

      Source librarySource = new Source();
      librarySource.setType("container");
      librarySource.setPath("bim/repository/*");
      librarySource.setGraphname("*");

      Graph libraryGraphPattern = new Graph();
      libraryGraphPattern.setSource(librarySource);
      ArrayList<GraphVarImpl> schemaList = new ArrayList();
      schemaList.add(new GraphVarImpl("SCHEMA_UNION_GRAPH"));
      schemaList.add(new GraphVarImpl("FULL_UNION_GRAPH"));
      libraryGraphPattern.setAs(schemaList);
      graphs.add(libraryGraphPattern);
    }

    Container container = new Container();
//    container.setParent(new ConfigFile());
    container.setType("container");
    container.setLocation(locator);
    container.setVariables(variables);
    container.setGraphs(graphs);

    return container;
  }

  public static Run getDefaultRun(List<File> containersList) {
    Run run = new Run();

    run.setContainers(getDefaultContainers(containersList));
    run.setSteps(getDefaultSteps());
    run.setReports(getDefaultReports());

    return run;
  }

  public static void overrideContainers(ConfigFile configFile, int count, Path[] containerFiles) {
    if(configFile.getRun().getContainers().length != 1) {
      throw new RuntimeException("When overriding the container location from config file please configure precisely one container-to-override in the config.yml");
    }

    Container blueprint = configFile.getRun().getContainers()[0];
    Container[] overriddenContainerSet = new Container[count];
    log.info("Use specified container config to load overriding container paths:");
    for(int i = 0; i < count; i++) {
      overriddenContainerSet[i] = blueprint.clone();
      String location = containerFiles[i].toString();
      log.info(location);
      overriddenContainerSet[i].getLocation().setPath(location);
    }
    configFile.getRun().setContainers(overriddenContainerSet);
  }

}
