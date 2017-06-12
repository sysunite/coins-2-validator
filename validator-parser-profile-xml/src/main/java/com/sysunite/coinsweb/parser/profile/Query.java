package com.sysunite.coinsweb.parser.profile;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlCData;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.sysunite.coinsweb.parser.Parser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author bastbijl, Sysunite 2017
 */
@JacksonXmlRootElement(localName = "step")
public class Query {

  public static final String NO_RESULT = "no-result";
  public static final String UPDATE = "update";

  private static final Logger log = LoggerFactory.getLogger(Query.class);

  @JacksonXmlProperty(localName = "type", isAttribute = true)
  private String type;

  @JacksonXmlProperty(localName = "reference")
  private String reference;

  @JacksonXmlProperty(localName = "description")
  private String description;


  @JsonInclude(Include.NON_NULL)
  @JacksonXmlCData
  @JacksonXmlProperty(localName = "format")
  private String format;

  @JacksonXmlCData
  @JacksonXmlProperty(localName = "query")
  private String query;


  public String getType() {
    return type;
  }
  public void setType(String type) {
    this.type = type;
  }

  public String getReference() {
    return reference;
  }
  public void setReference(String reference) {
    this.reference = reference;
  }

  public String getDescription() {
    return description;
  }
  public void setDescription(String description) {
    this.description = description;
  }

  public String getFormat() {
    return format;
  }
  public String cleanFormat() {
    if(format == null) {
      return "";
    } else {
      return Parser.indentText(format, 0).trim();
    }
  }
  public void setFormat(String format) {
    this.format = format;
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
