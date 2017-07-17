package com.sysunite.coinsweb.parser.profile.pojo;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.sysunite.coinsweb.parser.profile.util.Markdown;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

/**
 * @author bastbijl, Sysunite 2017
 */
public class Bundle {

  private static final Logger log = LoggerFactory.getLogger(Bundle.class);

  public static final String VALIDATION = "validation";
  public static final String INFERENCE = "inference";

  @JacksonXmlProperty(localName = "type", isAttribute = true)
  private String type;

  @JacksonXmlProperty(localName = "reference", isAttribute = true)
  private String reference;

  @JacksonXmlProperty(localName = "description")
  private String description;

  @JacksonXmlProperty(localName = "query")
  @JacksonXmlElementWrapper(localName="queries")
  private ArrayList<Query> queries;

  private boolean valid = false;


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
    return Markdown.parseLinksToHtml(description);
  }
  public void setDescription(String description) {
    this.description = description;
  }


  public ArrayList<Query> getQueries() {
    return queries;
  }
  public void setQueries(ArrayList<Query> queries) {
    this.queries = queries;
  }

  public boolean getValid() {
    return valid;
  }
  public void setValid(boolean valid) {
    this.valid = valid;
  }
}
