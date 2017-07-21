package com.sysunite.coinsweb.parser.profile.pojo;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlCData;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.sysunite.coinsweb.parser.profile.util.IndentedCDATAPrettyPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author bastbijl, Sysunite 2017
 */
public class QueryConfiguration {

  private static final Logger log = LoggerFactory.getLogger(QueryConfiguration.class);

  @JacksonXmlCData
  @JacksonXmlProperty(localName = "defaultPrefixes")
  private String defaultPrefixes;


  public String getDefaultPrefixes() {
    return defaultPrefixes;
  }
  public void setDefaultPrefixes(String defaultPrefixes) {
    this.defaultPrefixes = IndentedCDATAPrettyPrinter.indentText(defaultPrefixes, 0).trim();
  }

}
