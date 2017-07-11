package com.sysunite.coinsweb.parser.config.factory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature;
import com.sysunite.coinsweb.parser.config.pojo.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

/**
 * @author bastbijl, Sysunite 2017
 */
public class ConfigFactory {

  private static final Logger log = LoggerFactory.getLogger(ConfigFactory.class);

  private static DescribeFactory describeFactory = null;

  public static void setDescribeFactory(DescribeFactory factory) {
    ConfigFactory.describeFactory = factory;
  }



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

  public static ConfigFile getDefaultConfig(ArrayList<File> containersList) {
    Path localizeTo = containersList.get(0).toPath().getParent();
    return getDefaultConfig(containersList, localizeTo);
  }
  public static ConfigFile getDefaultConfig(List<File> containersList, Path localizeTo) {
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
    environment.setLoadingStrategy(Environment.HASH_IN_GRAPHNAME);

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


  public static Container getDefaultContainer(File containerFile, Path localizeTo) {

    Locator locator = new Locator();
    locator.localizeTo(localizeTo);
    locator.setType("file");
    locator.setPath(containerFile.toString());

    ArrayList<Graph> graphs;
    if(describeFactory != null) {
      graphs = describeFactory.graphsInContainer(containerFile);
    } else {
      graphs = new ArrayList();

      Source instanceGraphSource = new Source();
      instanceGraphSource.setType("container");
      instanceGraphSource.setPath("bim"+File.separator+"*");
      instanceGraphSource.setGraphname("\"*\"");

      Graph instanceGraphPattern = new Graph();
      instanceGraphPattern.setSource(instanceGraphSource);
      instanceGraphPattern.setAs(new ArrayList<>(Arrays.asList("INSTANCE_UNION_GRAPH", "FULL_UNION_GRAPH")));
      graphs.add(instanceGraphPattern);

      Source librarySource = new Source();
      librarySource.setType("container");
      librarySource.setPath("bim"+File.separator+"repository"+File.separator+"*");
      librarySource.setGraphname("\"*\"");

      Graph libraryGraphPattern = new Graph();
      libraryGraphPattern.setSource(librarySource);
      libraryGraphPattern.setAs(new ArrayList<>(Arrays.asList("SCHEMA_UNION_GRAPH", "FULL_UNION_GRAPH")));
      graphs.add(libraryGraphPattern);
    }

    Container container = new Container();
    container.setType("container");
    container.setLocation(locator);
    container.setGraphs(graphs.toArray(new Graph[0]));

    return container;
  }

  public static Run getDefaultRun(List<File> containersList, Path localizeTo) {


    ArrayList<Container> containers = new ArrayList();
    for(File containerFile : containersList) {
      containers.add(getDefaultContainer(containerFile, localizeTo));
    }

    Run run = new Run();

    run.setContainers(containers.toArray(new Container[0]));
    run.setSteps(getDefaultSteps());
    run.setReports(getDefaultReports(localizeTo));

    return run;
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

}
