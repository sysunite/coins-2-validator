package com.sysunite.coinsweb.cli;

import com.sysunite.coinsweb.connector.Connector;
import com.sysunite.coinsweb.connector.ConnectorFactory;
import com.sysunite.coinsweb.filemanager.ContainerFile;
import com.sysunite.coinsweb.graphset.ContainerGraphSet;
import com.sysunite.coinsweb.graphset.GraphSetFactory;
import com.sysunite.coinsweb.parser.config.*;
import com.sysunite.coinsweb.report.ReportFactory;
import com.sysunite.coinsweb.steps.ValidationStep;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * @author bastbijl, Sysunite 2017
 */
public class Application {

  private static final Logger log = Logger.getLogger(Application.class);

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

    // Get config
    ConfigFile configFile = ConfigFile.parse(options.getConfig().toFile());
    Connector connector = ConnectorFactory.build(configFile.getEnvironment().getStore());

    // For each container file execute steps
    for(Container containerConfig : configFile.getRun().getContainers()) {

      ContainerFile container = ContainerFile.parse(containerConfig.getLocation());
      ContainerGraphSet graphSet = GraphSetFactory.loadContainer(container, connector, containerConfig);

      Map<String, Object> reportItems = new HashMap();

      for(Step step : configFile.getRun().getSteps()) {

        ValidationStep validationStep = step.getValidationStep();
        Map<String, Object> items = validationStep.execute(container, graphSet);
        reportItems.putAll(items);
      }

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
          ReportFactory.saveReport(payload, report.getLocation().getPath());
        }
        if(Locator.ONLINE.equals(report.getLocation().getType()) && payload != null) {
          ReportFactory.postReport(payload, report.getLocation().getUri());
        }
      }

    }

  }


}
