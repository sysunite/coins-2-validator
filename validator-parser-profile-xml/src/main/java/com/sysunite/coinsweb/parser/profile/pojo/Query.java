package com.sysunite.coinsweb.parser.profile.pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlCData;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.sysunite.coinsweb.parser.profile.util.Markdown;
import com.sysunite.coinsweb.parser.Parser;
import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @author bastbijl, Sysunite 2017
 */
@JacksonXmlRootElement(localName = "step")
public class Query {

  private static final Logger log = LoggerFactory.getLogger(Query.class);

  @JacksonXmlProperty(localName = "reference", isAttribute = true)
  private String reference;

  @JacksonXmlProperty(localName = "description")
  private String description;

  @JsonInclude(Include.NON_NULL)
  @JacksonXmlCData
  @JacksonXmlProperty(localName = "resultFormat")
  private String format;

  @JacksonXmlCData
  @JacksonXmlProperty(localName = "sparql")
  private String query;


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
    return Markdown.parseLinksToHtml(format);
  }
  public String cleanFormat() {
    if(format == null) {
      return "";
    } else {
      return Parser.indentText(getFormat(), 0).trim();
    }
  }
  public void setFormat(String format) {
    this.format = format;
  }
  @JsonIgnore
  private Template formatTemplate ;
  @JsonIgnore
  public Template getFormatTemplate() {
    if(formatTemplate == null) {
      StringTemplateLoader templateLoader = new StringTemplateLoader();
      Configuration cfg = new Configuration();
      cfg.setTemplateLoader(templateLoader);
      templateLoader.putTemplate("resultFormat", cleanFormat());
      try {
        formatTemplate = cfg.getTemplate("resultFormat");
      } catch (IOException e) {
        e.printStackTrace();
        throw new RuntimeException("Something went wrong creating the query result format");
      }
    }
    return formatTemplate;
  }


  public String getQuery() {
    return Parser.indentText(query, 6);
  }
  public String cleanQuery() {
    return Parser.indentText(query, 0).trim();
  }
  public void setQuery(String query) {
    this.query = Parser.indentText(query, 6);
  }


}
