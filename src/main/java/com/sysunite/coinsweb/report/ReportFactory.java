package com.sysunite.coinsweb.report;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import com.sysunite.coinsweb.cli.CliOptions;
import com.sysunite.coinsweb.parser.config.pojo.ConfigFile;
import com.sysunite.coinsweb.parser.profile.util.IndentedCDATAPrettyPrinter;
import freemarker.cache.FileTemplateLoader;
import freemarker.core.InvalidReferenceException;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.eclipse.rdf4j.query.Binding;
import org.eclipse.rdf4j.query.BindingSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Base64;
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
    reportItems.put("validatorVersion", CliOptions.getVersion());
    reportItems.put("runConfig", configFile);
    reportItems.put("instanceOf", new InstanceOfMethod());
    reportItems.put("printBoolean", new NullTrueFalseMethod());

    try {

      Configuration cfg = new Configuration();
      cfg.setLocale(Locale.GERMAN); // for dutch number format
      cfg.setClassForTemplateLoading(ReportFactory.class, "/report-template/");
      cfg.setDefaultEncoding("UTF-8");
      Template template = cfg.getTemplate(templatePath);
      StringWriter writer = new StringWriter();
      template.process(reportItems, writer);
      return writer.toString();

    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }
    throw new RuntimeException("Was not able to build the report from template.");
  }

  private static String build(ConfigFile configFile, File file) {

    Map<String, Object> reportItems = new HashMap();
    reportItems.put("runConfig", configFile);
    reportItems.put("instanceOf", new InstanceOfMethod());
    reportItems.put("printBoolean", new NullTrueFalseMethod());

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

  public static String postReport(String payload, String uri, String contentType) {
    return postReport(payload, uri, contentType, null, null);
  }
  public static String postReport(String payload, String uri, String contentType, String username, String password) {

    int code = 0;
    String responseBody = "";
    try {

      URL obj = new URL(uri);
      HttpURLConnection connection = (HttpURLConnection) obj.openConnection();
      if(username != null && password != null) {
        String encoded = Base64.getEncoder().encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8));
        connection.setRequestProperty("Authorization", "Basic " + encoded);
      }

      // Setting basic post request
      connection.setRequestMethod("POST");
      if(contentType != null) {
        connection.setRequestProperty("Content-Type", contentType);
      }

      // Send post request
      connection.setDoOutput(true);
      DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
      wr.writeBytes(payload);
      wr.flush();
      wr.close();

      code = connection.getResponseCode();
      responseBody = IOUtils.toString(connection.getInputStream(), StandardCharsets.UTF_8);
      return responseBody;
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
    return responseBody;
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

  public static String buildXml(Object reportFile) {
    XmlMapper objectMapper = new XmlMapper();
    objectMapper.enable(ToXmlGenerator.Feature.WRITE_XML_DECLARATION);
    objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    ObjectWriter xmlWriter = objectMapper.writer(new IndentedCDATAPrettyPrinter());

    try {
      return xmlWriter.writeValueAsString(reportFile);
    } catch (JsonProcessingException e) {
      log.error(e.getMessage(), e);
    }
    throw new RuntimeException("Failed to produce xml");
  }

  public static String buildJson(Object reportFile) {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

    try {
      return objectMapper.writeValueAsString(reportFile);
    } catch (JsonProcessingException e) {
      log.error(e.getMessage(), e);
    }
    throw new RuntimeException("Failed to produce json");
  }
}
