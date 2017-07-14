package com.sysunite.coinsweb.runner;

import com.sysunite.coinsweb.connector.Connector;
import com.sysunite.coinsweb.connector.ConnectorFactory;
import com.sysunite.coinsweb.connector.ConnectorFactoryImpl;
import com.sysunite.coinsweb.filemanager.ContainerFile;
import com.sysunite.coinsweb.filemanager.ContainerFileImpl;
import com.sysunite.coinsweb.filemanager.DescribeFactoryImpl;
import com.sysunite.coinsweb.filemanager.VirtualContainerFileImpl;
import com.sysunite.coinsweb.graphset.ContainerGraphSet;
import com.sysunite.coinsweb.graphset.GraphSetFactory;
import com.sysunite.coinsweb.parser.config.factory.FileFactory;
import com.sysunite.coinsweb.parser.config.pojo.*;
import com.sysunite.coinsweb.report.ReportFactory;
import com.sysunite.coinsweb.steps.ValidationStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @author bastbijl, Sysunite 2017
 */
public class Validation {

  private static final Logger log = LoggerFactory.getLogger(Validation.class);


  public static void run(ConfigFile configFile) {

    log.info("Construct the connector");
    ConnectorFactory factory = new ConnectorFactoryImpl();
    Connector connector = factory.build(configFile.getEnvironment());

    if(!connector.testConnection()) {
      throw new RuntimeException("Failed to connect to the store");
    }


    log.info("Entered first phase of run, \uD83D\uDD0E iterate over containers");



    Map<String, Object> reportItems = new HashMap();
    reportItems.put("runConfig", configFile);

    Map<String, Object> containers = new HashMap();
    reportItems.put("containers", containers);

    // For each container file execute steps
    for(Container containerConfig : configFile.getRun().getContainers()) {

      // Init containerFile
      ContainerFile containerFile;
      if(containerConfig.isVirtual()) {
        log.info("\uD83D\uDCCF Validate a virtual file");
        containerFile = new VirtualContainerFileImpl();
      } else {
        String containerFilePath = containerConfig.getLocation().toString();
        log.info("\uD83D\uDCD0 Validate "+containerFilePath);

        // Check basic sanity of file by scanning it
        try {
          containerFile = new ContainerFileImpl(FileFactory.toFile(containerConfig.getLocation()).getPath());
          containerFile.scan();
        } catch (RuntimeException e) {
          Map<String, Object> containerItems = new HashMap();
          containerItems.put("fileNotFound", true);
          containerItems.put("alledgedName", containerFilePath);
          containerItems.put("valid", false);
          containers.put(containerConfig.getCode(), containerItems);
          log.info("Skipping normal path because container file could not be found, up to the next");
          continue;
        }
      }

      // Report if namespace collision in container prevents the expanding
      try {
        // Essential step to get rid of the wildcards in the configFile
        log.info("Will now expand any wildcard usage in the config.yml section of this file");
        DescribeFactoryImpl.expandGraphConfig(containerConfig);

      } catch (RuntimeException e) {
        Map<String, Object> containerItems = new HashMap();
        containerItems.put("fileNotFound", false);
        containerItems.put("file", containerFile);
        containerItems.put("namespaceConflict", true);
        containerItems.put("availableNamespaces", availableNamespaces(containerFile));
        containerItems.put("stepNames", new ArrayList<String>());
        containerItems.put("steps", new HashMap<String, Boolean>());
        containerItems.put("stepsFailed", new HashMap<String, Boolean>());
        containerItems.put("valid", false);
        containers.put(containerConfig.getCode(), containerItems);
        log.info("Skipping normal path because container file could not be found, up to the next");
        continue;
      }


      // Init graphSet
      ContainerGraphSet graphSet = GraphSetFactory.lazyLoad(containerFile, containerConfig, connector);

      // Fill dataMap for report
      Map<String, Object> containerItems = new HashMap();
      containerItems.put("fileNotFound", false);
      containerItems.put("file", containerFile);
      containerItems.put("namespaceConflict", false);
      containerItems.put("availableNamespaces", availableNamespaces(containerFile));
      containerItems.put("stepNames", new ArrayList<String>());
      containerItems.put("steps", new HashMap<String, Boolean>());
      containerItems.put("stepsFailed", new HashMap<String, Boolean>());

      // Execute the steps
      for (ValidationStep step : configFile.getRun().getSteps()) {

        log.info("\uD83D\uDD2C Will now execute validationStep of type "+step.getType());

        try {
          Map<String, Object> items = step.execute(containerFile, graphSet);
          if (!items.containsKey("valid")) {
            throw new RuntimeException("Validator " + step.getType() + " dit not return the field \"valid\"");
          }
          boolean valid = (boolean) items.remove("valid");
          ((ArrayList<String>) containerItems.get("stepNames")).add(step.getType());
          ((Map<String, Boolean>) containerItems.get("steps")).put(step.getType(), valid);
          ((Map<String, Boolean>) containerItems.get("stepsFailed")).put(step.getType(), false);
          containerItems.putAll(items);
        } catch(RuntimeException e) {

          log.warn("Executing failed validationStep of type "+step.getType());
          log.warn(e.getMessage());

          // Default config for failed validations
          boolean valid = false;
          ((ArrayList<String>) containerItems.get("stepNames")).add(step.getType());
          ((Map<String, Boolean>) containerItems.get("steps")).put(step.getType(), valid);
          ((Map<String, Boolean>) containerItems.get("stepsFailed")).put(step.getType(), true);
        }
      }

      // Cleanup
      graphSet.cleanup();

      // Postprocessing of reportItems
      boolean valid = true;
      Map<String, Boolean> steps = (Map<String, Boolean>) containerItems.get("steps");
      for (Boolean stepValid : steps.values()) {
        valid &= stepValid;
      }
      containerItems.put("valid", valid);

      containers.put(containerConfig.getCode(), containerItems);
    }

    log.info("Close the connector");
    connector.close();


    log.info("Entered second phase of run, \uD83D\uDDDE generate reports");

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

      if(Locator.FILE.equals(report.getLocation().getType()) && payload != null) {
        ReportFactory.saveReport(payload, configFile.resolve(report.getLocation().getPath()));
      }
      if(Locator.ONLINE.equals(report.getLocation().getType()) && payload != null) {
        ReportFactory.postReport(payload, report.getLocation().getUri());
      }
    }
  }

  private static HashMap<String, String> availableNamespaces(ContainerFile containerFile) {
    HashMap<String, String> availableNamespaces = new HashMap();
    for(String libraryFile : containerFile.getRepositoryFiles()) {
      availableNamespaces.put(libraryFile, String.join(", ", containerFile.getRepositoryFileNamespaces(libraryFile)));
    }
    return availableNamespaces;
  }
}
