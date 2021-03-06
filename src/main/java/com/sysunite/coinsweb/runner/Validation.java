package com.sysunite.coinsweb.runner;

import com.sysunite.coinsweb.cli.CliOptions;
import com.sysunite.coinsweb.connector.Connector;
import com.sysunite.coinsweb.filemanager.ContainerFile;
import com.sysunite.coinsweb.filemanager.ContainerFileImpl;
import com.sysunite.coinsweb.graphset.ContainerGraphSet;
import com.sysunite.coinsweb.graphset.ContainerGraphSetFactory;
import com.sysunite.coinsweb.graphset.GraphVar;
import com.sysunite.coinsweb.parser.config.factory.FileFactory;
import com.sysunite.coinsweb.parser.config.pojo.ConfigFile;
import com.sysunite.coinsweb.parser.config.pojo.Container;
import com.sysunite.coinsweb.parser.config.pojo.Locator;
import com.sysunite.coinsweb.parser.config.pojo.Report;
import com.sysunite.coinsweb.report.ReportFactory;
import com.sysunite.coinsweb.report.ReportFile;
import com.sysunite.coinsweb.steps.ProfileValidation;
import com.sysunite.coinsweb.steps.ValidationStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author bastbijl, Sysunite 2017
 */
public class Validation {
  private static final Logger log = LoggerFactory.getLogger(Validation.class);

  public static boolean run(ConfigFile configFile, Connector connector) {

    boolean failed = false;

    log.info("Entered first phase of run, \uD83D\uDD0E iterate over containers");

    // For each container file execute steps
    for(Container containerConfig : configFile.getRun().getContainers()) {

      Boolean valid = true;

      // Init containerFile
      if(containerConfig.isVirtual()) {
        log.info("\uD83D\uDCCF Validate a virtual file");
      } else {
        String containerFilePath = FileFactory.toFile(containerConfig.getLocation()).toString();
        log.info("\uD83D\uDCD0 Validate "+containerFilePath);
      }
      ContainerFileImpl containerFile = new ContainerFileImpl(containerConfig);

      containerConfig.setContainer(containerFile);

//      // Skip all tests if loading container file failed
//      if(!containerFile.exists() || containerFile.isCorruptZip()) {
//        log.warn("Skipping validation because of problem with container file");
//        continue;
//      }

      // Get inference preferences
      Map<String, Set<GraphVar>> inferencePreference = new HashMap<>();
      for (ValidationStep stepTemplate : configFile.getRun().getSteps()) {

        if(stepTemplate instanceof ProfileValidation) {
          ProfileValidation profileValidation = (ProfileValidation) stepTemplate;
          inferencePreference = ContainerGraphSetFactory.inferencePreference(profileValidation.loadProfileFile());
        }
      }

      // Init graphSet
      ContainerGraphSet graphSet = ContainerGraphSetFactory.lazyLoad(containerFile, containerConfig, connector, inferencePreference);

      // Execute the steps
      for (ValidationStep stepTemplate : configFile.getRun().getSteps()) {

        // Skip all tests if loading container graph set failed
        if(graphSet.loadingFailed()) {
          log.warn("Skipping validation step because of problem loading graphs for this graph set");
          continue;
        }

        // Make this step container-specific
        ValidationStep step = stepTemplate.clone();

        log.info("\uD83D\uDD2C Will now execute validationStep of type "+step.getType());

        step.execute(containerFile, graphSet);

        if(step.getFailed()) {
          containerConfig.addStep(step);
          valid &= false;
          failed = true;
          break;
        } else {
          containerConfig.addStep(step);
          valid &= step.getValid();
        }
      }

      containerConfig.setValid(valid);

      // Cleanup
      graphSet.cleanup();
    }

    log.info("Close the connector");
    connector.close();

    log.info("Entered second phase of run, \uD83D\uDDDE generate reports");

    // Generate the reports
    for(Report report : configFile.getRun().getReports()) {

      String payload = null;
      if(Report.XML.equals(report.getType())) {
        ReportFile reportFile = new ReportFile();
        reportFile.setContainers(configFile.getRun().getContainers());
        payload = ReportFactory.buildXml(reportFile);
      }
      if(Report.JSON.equals(report.getType())) {
        ReportFile reportFile = new ReportFile();
        reportFile.setContainers(configFile.getRun().getContainers());
        payload = ReportFactory.buildJson(reportFile);
      }
      if(Report.HTML.equals(report.getType())) {
        payload = ReportFactory.buildHtml(configFile);
      }
      if(Report.CUSTOM.equals(report.getType())) {
        payload = ReportFactory.buildCustom(configFile, FileFactory.toFile(report.getTemplate()));
      }

      if(Locator.FILE.equals(report.getLocation().getType()) && payload != null) {
        Path path = CliOptions.makeUnique(configFile.resolve(report.getLocation().getPath()));
        report.getLocation().setPath(path.toString());
        ReportFactory.saveReport(payload, path);
      }
      if(Locator.ONLINE.equals(report.getLocation().getType()) && payload != null) {
        ReportFactory.postReport(payload, report.getLocation().getUri(), "application/xml");
      }
    }
    return !failed;
  }

  private static HashMap<String, String> availableNamespaces(ContainerFile containerFile) {
    HashMap<String, String> availableNamespaces = new HashMap();
    for(String libraryFile : containerFile.getRepositoryFiles()) {
      availableNamespaces.put(libraryFile, String.join(", ", containerFile.getRepositoryFileNamespaces(libraryFile)));
    }
    return availableNamespaces;
  }
}
