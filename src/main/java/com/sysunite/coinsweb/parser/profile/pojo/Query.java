package com.sysunite.coinsweb.parser.profile.pojo;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlCData;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.sysunite.coinsweb.parser.profile.util.IndentedCDATAPrettyPrinter;
import com.sysunite.coinsweb.parser.profile.util.Markdown;
import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @author bastbijl, Sysunite 2017
 */
@JacksonXmlRootElement(localName = "query")
public class Query {
  private static final Logger log = LoggerFactory.getLogger(Query.class);

  @JacksonXmlProperty(localName = "reference", isAttribute = true)
  private String reference;

  private String description;

  @JsonInclude(Include.NON_NULL)
  @JacksonXmlCData
  private String resultFormat;

  @JacksonXmlCData
  @JacksonXmlProperty(localName = "sparql")
  protected String query;

  public String getReference() {
    return reference;
  }
  public void setReference(String reference) {
    this.reference = reference;
  }

  @JsonGetter("description")
  public String getDescription() {
    return description;
  }
  public String parseDescription() {
    return Markdown.parseLinksToHtml(description);
  }
  public void setDescription(String description) {
    this.description = description;
  }

  @JsonGetter("resultFormat")
  public String getResultFormat() {
    return resultFormat;
  }
  public String parseResultFormat() {
    return Markdown.parseLinksToHtml(resultFormat);
  }
  public void setResultFormat(String resultFormat) {
    if(resultFormat != null && !resultFormat.isEmpty()) {
      this.resultFormat = IndentedCDATAPrettyPrinter.indentText(resultFormat, 0).trim();
    } else {
      this.resultFormat = "";
    }
  }
  @JsonIgnore
  private Template formatTemplate ;
  @JsonIgnore
  public Template getFormatTemplate() {
    if(formatTemplate == null) {
      StringTemplateLoader templateLoader = new StringTemplateLoader();
      Configuration cfg = new Configuration();
      cfg.setTemplateLoader(templateLoader);
      templateLoader.putTemplate("resultFormat", parseResultFormat());
      try {
        formatTemplate = cfg.getTemplate("resultFormat");
      } catch (IOException e) {
        log.error(e.getMessage(), e);
        throw new RuntimeException("Something went wrong creating the query result format");
      }
    }
    return formatTemplate;
  }

  public String getQuery() {
    return query;
  }
  public void setQuery(String query) {
    this.query = IndentedCDATAPrettyPrinter.indentText(query, 0).trim();
  }

  public Query clone() {
    Query clone = new Query();

    clone.setReference(this.getReference());
    clone.setDescription(this.getDescription());
    clone.setQuery(this.getQuery());
    clone.setResultFormat(this.getResultFormat());

    return clone;
  }
}
