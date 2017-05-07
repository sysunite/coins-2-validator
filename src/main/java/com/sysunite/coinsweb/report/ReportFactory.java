package com.sysunite.coinsweb.report;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Locale;
import java.util.Map;

/**
 * @author bastbijl, Sysunite 2017
 */
public class ReportFactory {

  private static final Logger log = Logger.getLogger(ReportFactory.class);

  public static String buildXml(Map<String, Object> reportItems) {
    return build(reportItems, "report.xml");
  }
  public static String buildHtml(Map<String, Object> reportItems) {
    return build(reportItems, "report.html");
  }

  private static String build(Map<String, Object> reportItems, String templatePath) {
    try {

      Configuration cfg = new Configuration();
      cfg.setLocale(Locale.GERMAN); // for dutch number format
      cfg.setClassForTemplateLoading(ReportFactory.class, "/validator/");
      cfg.setDefaultEncoding("UTF-8");
      Template template = cfg.getTemplate(templatePath);
      StringWriter writer = new StringWriter();
      template.process(reportItems, writer);
      return writer.toString();

    } catch (TemplateException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    throw new RuntimeException("Was not able to build the report from template.");
  }

  public static void postReport(String payload, String uri) {

  }

  public static void saveReport(String payload, String path) {
    try {
      FileUtils.writeStringToFile(new File(path), payload);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
