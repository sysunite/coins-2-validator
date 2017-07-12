package com.sysunite.coinsweb.report;

import com.sysunite.coinsweb.cli.CliOptions;
import freemarker.core.InvalidReferenceException;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.commons.io.FileUtils;
import org.eclipse.rdf4j.query.Binding;
import org.eclipse.rdf4j.query.BindingSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
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

  public static String buildXml(Map<String, Object> reportItems) {
    return build(reportItems, "report.xml");
  }
  public static String buildHtml(Map<String, Object> reportItems) {
    return build(reportItems, "report.html");
  }
  public static String buildDebug(Map<String, Object> reportItems) {
    return build(reportItems, "debug.xml");
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

    String filePath = path.toString();
    String filePathBase = filePath.substring(0, filePath.lastIndexOf("."));
    String fileExtension = filePath.substring(filePath.lastIndexOf("."));
    int i = 1;
    while(path.toFile().exists()) {
      filePath = filePathBase + "."+ (i++) + fileExtension;
      path = CliOptions.resolvePath(filePath);
    }

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
}
