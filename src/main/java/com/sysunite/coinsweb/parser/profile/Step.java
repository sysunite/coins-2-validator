package com.sysunite.coinsweb.parser.profile;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;
import com.sysunite.coinsweb.parser.Markdown;
import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

/**
 * @author bastbijl, Sysunite 2017
 */
@JacksonXmlRootElement(localName = "step")
public class Step {

  private static final Logger log = Logger.getLogger(Step.class);

  @JacksonXmlProperty(localName = "type", isAttribute = true)
  private String type;

  @JacksonXmlProperty(localName = "reference")
  private String reference;

  @JacksonXmlProperty(localName = "description")
  private String description;

  @JacksonXmlProperty(localName = "format")
  private String format;

  @JacksonXmlProperty(localName = "query")
  private Query query;


  public String getReference() {
    return reference;
  }

  public void setReference(String reference) {
    this.reference = reference;
  }

  public String getDescription() {
    return Markdown.parseLinksToHtml(description);
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getFormat() {
    return format;
  }

  public void setFormat(String format) {
    this.format = format;
  }

  private String buildQueryResult = null;
  public String buildQuery() {

    if(buildQueryResult != null) {
      return buildQueryResult;
    }

    // Build prefixes
    String prefixes = "";

    String[] parts = getQuery().getPrefixes().trim().split("\\s+");
    if((parts.length& 1) != 0 ) {
      throw new RuntimeException("The prefixes attribute should contain an even number of items in order to parse it, found "+parts.length+" with: "+getQuery().getPrefixes());
    }
    for(int i = 0; i < parts.length-2; i+=2) {
      prefixes += "PREFIX "+parts[i]+": <"+parts[i+1]+">\n";
    }

    // Remove wrong indention in the query
    String query = "";

    String[] lines = getQuery().getValue().trim().split("\\n");
    int cutoff = 0;
    for(String line : lines) {
      int count = line.indexOf(line.trim());
      if(cutoff == 0) {
        cutoff = count;
      }
      count = Math.min(count, cutoff);
      query += line.substring(count) + "\n";
    }

    return parseFreemarker(prefixes + query);
  }


  private String parseFreemarker(String query) {
    StringTemplateLoader templateLoader = new StringTemplateLoader();
    Configuration cfg = new Configuration();
    templateLoader.putTemplate("sparqlQuery", query);
    try {
      Template template = cfg.getTemplate("sparqlQuery");

      Map<String, String> data = new HashMap<>();
//      data.put("INSTANCE_GRAPH", "<"+ InMemGraphSet.INSTANCE_GRAPH +">");
//      data.put("WOA_GRAPH", "<"+ InMemGraphSet.WOA_GRAPH +">");
////      data.put("CORE_GRAPH", "<"+ InMemGraphSet.SCHEMA_GRAPH +">");
////      data.put("SCHEMA_GRAPH", "<"+ InMemGraphSet.SCHEMA_GRAPH +">");
//      data.put("SCHEMA_UNION_GRAPH", "<"+ InMemGraphSet.SCHEMA_UNION_GRAPH +">");
//      data.put("FULL_UNION_GRAPH", "<"+ graphSet.getFullUnionNamespace() +">");

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


  public Query getQuery() {
    return query;
  }

  public void setQuery(Query query) {
    this.query = query;
  }
}

class Query {

  private static final Logger log = Logger.getLogger(Query.class);

  @JacksonXmlProperty(localName = "prefixes", isAttribute = true)
  private String prefixes;

  @JacksonXmlText
  private String value;

  public String getPrefixes() {
    return prefixes;
  }
  public void setPrefixes(String prefixes) {
    this.prefixes = prefixes;
  }

  public String getValue() {
    return value;
  }
  public void setValue(String value) {
    this.value = value;
  }
}