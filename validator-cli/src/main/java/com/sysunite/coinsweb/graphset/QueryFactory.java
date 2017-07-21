package com.sysunite.coinsweb.graphset;

import com.sysunite.coinsweb.parser.config.pojo.GraphVarImpl;
import com.sysunite.coinsweb.parser.profile.pojo.Bundle;
import com.sysunite.coinsweb.parser.profile.pojo.ProfileFile;
import com.sysunite.coinsweb.parser.profile.pojo.Query;
import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author bastbijl, Sysunite 2017
 */
public class QueryFactory {

  private static final Logger log = LoggerFactory.getLogger(QueryFactory.class);


  public static final String VALIDATOR_NS = "http://validator#";


  public static String buildQuery(Query query, Map<String, String> data) {
    return buildQuery(query, data, null, 0);
  }
  public static String buildQuery(Query query, Map<String, String> data, String prefixes) {
    return buildQuery(query, data, prefixes, 0);
  }
  public static String buildQuery(Query query, Map<String, String> data, String prefixes, int maxResults) {
    String cleanQuery = query.getQuery();
    if(prefixes != null && !prefixes.isEmpty()) {
      cleanQuery = prefixes + '\n' + cleanQuery;
    }
    String finalQuery = parseFreemarker(cleanQuery, data);
    if(maxResults > 0) {
      finalQuery += " LIMIT " + maxResults;
    }
    return finalQuery;
  }

  public static Set<GraphVar> usedVars(ProfileFile profileFile) {
    HashSet<GraphVar> result = new HashSet();
    for(Bundle bundle : profileFile.getBundles()) {
      result.addAll(usedVars(bundle));
    }
    return result;
  }
  public static Set<GraphVar> usedVars(Bundle bundle) {
    HashSet<GraphVar> result = new HashSet();
    Pattern pattern = Pattern.compile("(?<=\\$\\{)([^\\}]+)(?=\\})");
    for(Query query : bundle.getQueries()) {
      Matcher matcher = pattern.matcher(query.getQuery());
      while(matcher.find()) {
        result.add(new GraphVarImpl(matcher.group()));
      }
    }
    return result;
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
