package com.sysunite.coinsweb.parser.profile;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import org.apache.log4j.Logger;

import java.util.ArrayList;

/**
 * @author bastbijl, Sysunite 2017
 */
public class QueryConfiguration {

  private static final Logger log = Logger.getLogger(QueryConfiguration.class);

  @JacksonXmlProperty(localName = "graphs")
  private ArrayList<Graph> graphs;

  @JacksonXmlProperty(localName = "defaultPrefixes")
  private String defaultPrefixes;


  public String getDefaultPrefixes() {
    return defaultPrefixes;
  }
  public void setDefaultPrefixes(String defaultPrefixes) {
    this.defaultPrefixes = defaultPrefixes;
  }

  public ArrayList<Graph> getGraphs() {
    return graphs;
  }
  public void setGraphs(ArrayList<Graph> graphs) {
    this.graphs = graphs;
  }
}
