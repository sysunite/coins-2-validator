package com.sysunite.coinsweb.parser.profile;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.util.ArrayList;

/**
 * @author bastbijl, Sysunite 2017
 */
public class Bundle {

  @JacksonXmlProperty(localName = "reference")
  private String reference;

  @JacksonXmlProperty(localName = "description")
  private String description;

  @JacksonXmlProperty(localName = "queries")
  private ArrayList<Query> queries;


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

  public ArrayList<Query> getQueries() {
    return queries;
  }
  public void setQueries(ArrayList<Query> queries) {
    this.queries = queries;
  }
}
