package com.sysunite.coinsweb.parser.profile.pojo;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlCData;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.sysunite.coinsweb.parser.Parser;
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
    return Parser.indentText(defaultPrefixes, 3);
  }
  public String cleanDefaultPrefixes() {
    return Parser.indentText(defaultPrefixes, 0).trim();
  }
  public void setDefaultPrefixes(String defaultPrefixes) {
    this.defaultPrefixes = Parser.indentText(defaultPrefixes, 3);
  }

}
