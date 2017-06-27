package com.sysunite.coinsweb.cli;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.FileAppender;
import com.sysunite.coinsweb.connector.ConnectorFactoryImpl;
import com.sysunite.coinsweb.filemanager.ContainerFile;
import com.sysunite.coinsweb.filemanager.ContainerFileImpl;
import com.sysunite.coinsweb.graphset.ContainerGraphSet;
import com.sysunite.coinsweb.graphset.GraphSetFactory;
import com.sysunite.coinsweb.parser.config.factory.ConfigFactory;
import com.sysunite.coinsweb.parser.config.pojo.*;
import com.sysunite.coinsweb.report.ReportFactory;
import com.sysunite.coinsweb.steps.StepFactoryImpl;
import com.sysunite.coinsweb.steps.ValidationStep;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @author bastbijl, Sysunite 2017
 */
public class Application {

  private static final Logger log = LoggerFactory.getLogger(Application.class);


  private static CliOptions options;

  public static void main(String[] args) {

    options = new CliOptions(args);

    // Print header
    CliOptions.printHeader();

    // Asked for help
    if (options.printHelpOption()) {
      CliOptions.usage();
      System.exit(1);
      return;
    }

    if (options.writeLog()) {
      setLoggers("log.txt");
      log.info(")} COINS 2.0 validator - version " + CliOptions.getVersion());
    } else {
      setLoggers(null);
    }


    if(options.describeMode()) {
      try {
        System.out.println(describe());
        System.exit(0);
      } catch (RuntimeException e) {
        log.error(e.getMessage(), e);
      }
    }
    if(options.runMode()) {
      try {
        File configFile;
        if (!options.hasConfigFile()) {
          configFile = CliOptions.resolvePath("config-generated.yml").toFile();
          FileUtils.writeStringToFile(configFile, describe(), "UTF-8");
        } else {
          configFile = options.getConfigFile().toFile();
        }

        run(configFile);
      } catch (RuntimeException e) {
        log.error(e.getMessage(), e);
      } catch (IOException e) {
        log.error(e.getMessage(), e);
      }
    }
  }



  public static String describe() {

    ArrayList<ContainerFile> containers = new ArrayList();

    try {

      // Get containers
      for(int i = 0; i < options.hasContainerFile(); i++) {
        log.info("Try to read container file");
        ContainerFile containerFile = new ContainerFileImpl(options.getContainerFile(i).toFile().toString());
        log.info("Done reading container file");
        containers.add(containerFile);
      }

      if(containers.isEmpty()) {
        throw new RuntimeException();
      }


    } catch(RuntimeException e) {
      CliOptions.printOutput("(!) problem reading container file(s)\n");
      CliOptions.usage();
      System.exit(1);
      return null;
    }

    Path localizeTo = null;
    if(!options.absolutePaths()) {
      localizeTo = CliOptions.resolvePath("");
    }

    String yml = ConfigFactory.run(containers, localizeTo);
    return yml;
  }

  /**
   * @param inputFile if null read from System.in
   */
  public static void run(File inputFile) {

    StoreSanitizer.factory = new ConnectorFactoryImpl();
    StepDeserializer.factory = new StepFactoryImpl();

    // Get config
    ConfigFile configFile;
    try {
      if (inputFile != null) {
        log.info("Try to read config.yml from file");
        configFile = ConfigFile.parse(inputFile);
        log.info("Done reading config.yml from file");
      } else {
        log.info("Try to read config.yml from pipe");
        configFile = ConfigFile.parse(System.in);
        log.info("Done reading config.yml from pipe");
      }
    } catch(Exception e) {
      log.error(e.getMessage(), e);
      CliOptions.printOutput("(!) problem reading config.yml\n");
      CliOptions.usage();
      System.exit(1);
      return;
    }

    if(options.hasContainerFile() > 0) {
      ConfigFactory.overrideContainers(configFile, options.hasContainerFile(), options.getContainerFiles());
    }



    Map<String, Object> reportItems = new HashMap();
    reportItems.put("runConfig", configFile);

    Map<String, Object> containers = new HashMap();
    reportItems.put("containers", containers);

    // For each container file execute steps
    for(Container containerConfig : configFile.getRun().getContainers()) {

      log.info("Validate "+containerConfig.getLocation().toString());

      ContainerFileImpl containerFile = ContainerFileImpl.parse(containerConfig.getLocation(), configFile);
      ContainerGraphSet graphSet = GraphSetFactory.lazyLoad(containerFile, containerConfig, configFile);

      Map<String, Object> containerItems = new HashMap();
      containerItems.put("file", containerFile);
      HashMap<String, String> availableNamespaces = new HashMap();
      for(String libraryFile : containerFile.getRepositoryFiles()) {
        availableNamespaces.put(libraryFile, String.join(", ", containerFile.getRepositoryFileNamespaces(libraryFile)));
      }
      containerItems.put("availableNamespaces", availableNamespaces);
      containerItems.put("stepNames", new ArrayList<String>());
      containerItems.put("steps", new HashMap<String, Boolean>());

      for (Step step : configFile.getRun().getSteps()) {

        ValidationStep validationStep = step.getValidationStep();

        log.info("Will now execute validator with type "+step.getType());

        Map<String, Object> items = validationStep.execute(containerFile, graphSet);
        if (!items.containsKey("valid")) {
          throw new RuntimeException("Validator " + step.getType() + " dit not return the field \"valid\"");
        }
        boolean valid = (boolean) items.remove("valid");
        ((ArrayList<String>) containerItems.get("stepNames")).add(step.getType());
        ((Map<String, Boolean>) containerItems.get("steps")).put(step.getType(), valid);
        containerItems.putAll(items);
      }

      // Close graphSet
      graphSet.close();

      // Postprocessing of reportItems
      boolean valid = true;
      Map<String, Boolean> steps = (Map<String, Boolean>) containerItems.get("steps");
      for (Boolean stepValid : steps.values()) {
        valid &= stepValid;
      }
      containerItems.put("valid", valid);

      containers.put(containerConfig.getCode(), containerItems);
    }


    log.info("Start generating report");

    // Generate the reports
    String xml = null;
    String html = null;
    for(Report report : configFile.getRun().getReports()) {
      String payload = null;
      if(Report.XML.equals(report.getType())) {
        if(xml == null) {
          xml = ReportFactory.buildXml(reportItems);
        }
        payload = xml;
      }
      if(Report.HTML.equals(report.getType())) {
        if(html == null) {
          html = ReportFactory.buildHtml(reportItems);
        }
        payload = html;
      }
      if(Report.DEBUG.equals(report.getType())) {
        if(html == null) {
          html = ReportFactory.buildDebug(reportItems);
        }
        payload = html;
      }

      if(Locator.FILE.equals(report.getLocation().getType()) && payload != null) {
        ReportFactory.saveReport(payload, configFile.resolve(report.getLocation().getPath()));
      }
      if(Locator.ONLINE.equals(report.getLocation().getType()) && payload != null) {
        ReportFactory.postReport(payload, report.getLocation().getUri());
      }
    }

    log.info("Finished successfully, quitting");
    System.exit(0);


  }

  private static void setLoggers(String filePath) {

    LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();

    ch.qos.logback.classic.Logger root = lc.getLogger(Logger.ROOT_LOGGER_NAME);
    root.detachAppender("console");

    if(filePath != null) {

      File file = CliOptions.resolvePath(filePath).toFile();
      if(file.exists()) {
        file.delete();
      }

      PatternLayoutEncoder ple = new PatternLayoutEncoder();
      ple.setPattern("%date{yyyy-MM-dd HH:mm:ss} %level [%file:%line] %msg%n");
      ple.setContext(lc);
      ple.start();

      FileAppender<ILoggingEvent> fileAppender = new FileAppender();
      fileAppender.setFile(filePath);
      fileAppender.setEncoder(ple);
      fileAppender.setContext(lc);
      fileAppender.start();
      fileAppender.setAppend(false);


      root.addAppender(fileAppender);
      root.setLevel(Level.INFO);
    }
  }


}
