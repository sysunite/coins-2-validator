package com.sysunite.coinsweb.cli;

import com.sysunite.coinsweb.filemanager.ContainerFile;
import com.sysunite.coinsweb.graphset.ContainerGraphSet;
import com.sysunite.coinsweb.graphset.GraphSetFactory;
import com.sysunite.coinsweb.parser.config.*;
import com.sysunite.coinsweb.report.ReportFactory;
import com.sysunite.coinsweb.steps.ValidationStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * @author bastbijl, Sysunite 2017
 */
public class Application {

  private static final Logger log = LoggerFactory.getLogger(Application.class);

  public static void main(String[] args) {

    CliOptions options = new CliOptions(args);

    // Print header
    CliOptions.printHeader();

    // Asked for help
    if(options.printHelpOption()) {
      CliOptions.usage();
      System.exit(1);
      return;
    }

    if(options.writeLog()) {
      // todo
    }

    // Get config
    ConfigFile configFile;
    try {
      if (options.hasConfig()) {
        log.info("try to read config.yml from file");
        configFile = ConfigFile.parse(options.getConfig().toFile());
        log.info("read config.yml from file");
      } else {
        log.info("try to read config.yml from pipe");
        configFile = ConfigFile.parse(System.in);
        log.info("read config.yml from pipe");
      }
    } catch(RuntimeException e) {
      CliOptions.printHeader();
      CliOptions.printOutput("(!) no valid config.yml supplied\n");
      CliOptions.usage();
      System.exit(1);
      return;
    }

    Store storeConfig = configFile.getEnvironment().getStore();

    // For each container file execute steps
    for(Container containerConfig : configFile.getRun().getContainers()) {

      ContainerFile container = ContainerFile.parse(containerConfig.getLocation());
      ContainerGraphSet graphSet = GraphSetFactory.lazyLoad(container, storeConfig, containerConfig);

      Map<String, Object> reportItems = new HashMap();
      reportItems.put("steps", new HashMap<String, Boolean>());

      for(Step step : configFile.getRun().getSteps()) {

        ValidationStep validationStep = step.getValidationStep();
        Map<String, Object> items = validationStep.execute(container, graphSet);
        if(!items.containsKey("valid")) {
          throw new RuntimeException("Validator "+step.getType()+" dit not return the field \"valid\"");
        }
        boolean valid = (boolean) items.remove("valid");
        ((Map<String, Boolean>) reportItems.get("steps")).put(step.getType(), valid);
        reportItems.putAll(items);
      }

      // Postprocessing of reportItems
      boolean valid = true;
      Map<String, Boolean> steps = (Map<String, Boolean>) reportItems.get("steps");
      for(Boolean stepValid : steps.values()) {
        valid &= stepValid;
      }
      reportItems.put("valid", valid);

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
          ReportFactory.saveReport(payload, report.getLocation().getPath());
        }
        if(Locator.ONLINE.equals(report.getLocation().getType()) && payload != null) {
          ReportFactory.postReport(payload, report.getLocation().getUri());
        }
      }

      log.info("finished successfully, quitting");
      System.exit(0);
    }

  }


}
