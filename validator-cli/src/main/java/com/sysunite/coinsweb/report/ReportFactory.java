package com.sysunite.coinsweb.report;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import com.sysunite.coinsweb.parser.config.pojo.ConfigFile;
import com.sysunite.coinsweb.parser.profile.util.IndentedCDATAPrettyPrinter;
import freemarker.cache.FileTemplateLoader;
import freemarker.core.InvalidReferenceException;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.commons.io.FileUtils;
import org.eclipse.rdf4j.query.Binding;
import org.eclipse.rdf4j.query.BindingSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * @author bastbijl, Sysunite 2017
 */
public class ReportFactory {

  private static final Logger log = LoggerFactory.getLogger(ReportFactory.class);


  public static String buildHtml(ConfigFile configFile) {
    return build(configFile, "report.ftl");
  }
  public static String buildCustom(ConfigFile configFile, File file) {
    return build(configFile, file);
  }

  private static String build(ConfigFile configFile, String templatePath) {

    Map<String, Object> reportItems = new HashMap();
    reportItems.put("runConfig", configFile);
    reportItems.put("instanceOf", new InstanceOfMethod());

    try {

      Configuration cfg = new Configuration();
      cfg.setLocale(Locale.GERMAN); // for dutch number format
      cfg.setClassForTemplateLoading(ReportFactory.class, "/report-template/");
      cfg.setDefaultEncoding("UTF-8");
      Template template = cfg.getTemplate(templatePath);
      StringWriter writer = new StringWriter();
      template.process(reportItems, writer);
      return writer.toString();

    } catch (TemplateException e) {
      log.error(e.getMessage(), e);
    } catch (IOException e) {
      log.error(e.getMessage(), e);
    }
    throw new RuntimeException("Was not able to build the report from template.");
  }

  private static String build(ConfigFile configFile, File file) {

    Map<String, Object> reportItems = new HashMap();
    reportItems.put("runConfig", configFile);
    reportItems.put("instanceOf", new InstanceOfMethod());

    try {

      log.info("Try to load custom template: "+file.getPath());

      Configuration cfg = new Configuration();
      cfg.setLocale(Locale.GERMAN); // for dutch number format
      cfg.setDefaultEncoding("UTF-8");
      cfg.setTemplateLoader(new FileTemplateLoader(file.getParentFile()));
      Template template = cfg.getTemplate(file.getName());
      StringWriter writer = new StringWriter();
      template.process(reportItems, writer);
      return writer.toString();

    } catch (TemplateException e) {
      log.error(e.getMessage(), e);
    } catch (IOException e) {
      log.error(e.getMessage(), e);
    }
    throw new RuntimeException("Was not able to build the report from template.");
  }

  public static void postReport(String payload, String uri) {

    int code = 0;
    try {

      URL obj = new URL(uri);
      HttpURLConnection con = (HttpURLConnection) obj.openConnection();

      // Setting basic post request
      con.setRequestMethod("POST");
      con.setRequestProperty("Content-Type", "application/xml");

      // Send post request
      con.setDoOutput(true);
      DataOutputStream wr = new DataOutputStream(con.getOutputStream());
      wr.writeBytes(payload);
      wr.flush();
      wr.close();

      code = con.getResponseCode();
    } catch (MalformedURLException e) {
      log.error(e.getMessage(), e);
    } catch (ProtocolException e) {
      log.error(e.getMessage(), e);
    } catch (IOException e) {
      log.error(e.getMessage(), e);
    }

    if(code != 200) {
      throw new RuntimeException("Not able to upload report to uri "+uri);
    }
  }



  public static void saveReport(String payload, Path path) {

    log.info("Write report to "+path.toFile().getName());
    try {
      FileUtils.writeStringToFile(path.toFile(), payload, "UTF-8");
    } catch (IOException e) {
      log.error(e.getMessage(), e);
    }
  }

  public static String formatResult(BindingSet row, Template template) {

    HashMap<String, String> data = new HashMap();
    for(String var : row.getBindingNames()) {
      Binding binding = row.getBinding(var);
      if(binding != null) {
        data.put(var, condense(binding.getValue().stringValue()));
      } else {
        data.put(var, "NO_VALUE");
      }
    }

    try {

      Writer writer = new StringWriter();
      template.process(data, writer);
      return writer.toString();

    } catch (IOException e) {
      log.error(e.getMessage(), e);
    } catch (InvalidReferenceException e) {
      log.error(e.getMessage(), e);
    } catch (TemplateException e) {
      log.error(e.getMessage(), e);
    }
    throw new RuntimeException("Something went wrong formatting a result");
  }

  private static String condense(String input) {
    if(input.contains("#")) {
      input = "<u><div class=\"condense\">" + input.replace("#", "</div>#") + "</u>";
    }
    return input;
  }

  public static String buildXml(ConfigFile configFile) {
    XmlMapper objectMapper = new XmlMapper();
    objectMapper.enable(ToXmlGenerator.Feature.WRITE_XML_DECLARATION);
    objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    ObjectWriter xmlWriter = objectMapper.writer(new IndentedCDATAPrettyPrinter());

    try {
      return xmlWriter.writeValueAsString(configFile);
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }
    throw new RuntimeException("Failed to produce xml");
  }

  public static String buildJson(ConfigFile configFile) {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

    try {
      return objectMapper.writeValueAsString(configFile);
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }
    throw new RuntimeException("Failed to produce json");
  }
}
