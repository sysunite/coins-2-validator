package com.sysunite.coinsweb.parser.profile;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * @author bastbijl, Sysunite 2017
 */
@JacksonXmlRootElement(localName = "profile")
public class ProfileFile {

  private static final Logger log = LoggerFactory.getLogger(ProfileFile.class);

  @JacksonXmlProperty(localName = "name")
  private String name;

  @JacksonXmlProperty(localName = "version")
  private String version;

  @JacksonXmlProperty(localName = "author")
  private String author;

  @JsonInclude(Include.NON_NULL)
  @JacksonXmlProperty(localName = "queryLanguage")
  private String queryLanguage;

  @JsonInclude(Include.NON_NULL)
  @JacksonXmlProperty(localName = "queryConfiguration")
  private QueryConfiguration queryConfiguration;

  @JacksonXmlProperty(localName = "bundles")
  private ArrayList<Bundle> bundles;

  public static ProfileFile parse(InputStream input) {
    ObjectMapper objectMapper = new XmlMapper();
    try {
      return objectMapper.readValue(new BufferedReader(new InputStreamReader(input)), ProfileFile.class);
    } catch (IOException e) {
      e.printStackTrace();
    }
    throw new RuntimeException("Profile file could not be loaded.");
  }



  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  public String getVersion() {
    return version;
  }
  public void setVersion(String version) {
    this.version = version;
  }

  public String getAuthor() {
    return author;
  }
  public void setAuthor(String author) {
    this.author = author;
  }

  public String getQueryLanguage() {
    return queryLanguage;
  }
  public void setQueryLanguage(String queryLanguage) {
    this.queryLanguage = queryLanguage;
  }

  @JsonIgnore
  public boolean hasQueryConfiguration() {
    return queryConfiguration != null;
  }
  public QueryConfiguration getQueryConfiguration() {
    return queryConfiguration;
  }
  public void setQueryConfiguration(QueryConfiguration queryConfiguration) {
    this.queryConfiguration = queryConfiguration;
  }

  public ArrayList<Bundle> getBundles() {
    return bundles;
  }
  public void setBundles(ArrayList<Bundle> bundles) {
    this.bundles = bundles;
  }
}
