package com.sysunite.coinsweb.graphset;

import com.sysunite.coinsweb.parser.profile.Graph;
import com.sysunite.coinsweb.parser.profile.Query;
import com.sysunite.coinsweb.parser.profile.QueryConfiguration;
import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

/**
 * @author bastbijl, Sysunite 2017
 */
public class QueryFactory {

  private static final Logger log = LoggerFactory.getLogger(QueryFactory.class);




  public static String buildQuery(Query query, QueryConfiguration configuration) {

    Map<String, String> data = new HashMap<>();
    for(Graph graph : configuration.getGraphs()) {
      data.put(graph.getCode(), '<'+graph.getUri()+'>');
    }

    String cleanQuery = configuration.cleanDefaultPrefixes() + '\n' + query.cleanQuery();
    String finalQuery = parseFreemarker(cleanQuery, data);
    return finalQuery;
  }






  private static String parseFreemarker(String queryTemplate, Map<String, String> data) {
    Configuration cfg = new Configuration();
    StringTemplateLoader templateLoader = new StringTemplateLoader();
    cfg.setTemplateLoader(templateLoader);
    templateLoader.putTemplate("queryTemplate", queryTemplate);
    try {
      Template template = cfg.getTemplate("queryTemplate");
      Writer writer = new StringWriter();
      template.process(data, writer);
      return writer.toString();

    } catch (IOException e) {
      log.error(e.getMessage(), e);
    } catch (TemplateException e) {
      log.error(e.getMessage(), e);
    }

    throw new RuntimeException("Something went wrong building query.");
  }


}
