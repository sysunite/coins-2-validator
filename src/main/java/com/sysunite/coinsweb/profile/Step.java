package com.sysunite.coinsweb.profile;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;
import org.apache.log4j.Logger;

/**
 * @author bastbijl, Sysunite 2017
 */
@JacksonXmlRootElement(localName = "step")
public class Step {

  private static final Logger log = Logger.getLogger(Step.class);

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
    return description;
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

  public String buildQuery() {

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

    return prefixes + query;
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