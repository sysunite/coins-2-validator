package com.sysunite.coinsweb.profile;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import org.apache.log4j.Logger;

import java.util.ArrayList;

/**
 * @author bastbijl, Sysunite 2017
 */
@JacksonXmlRootElement(localName = "profile")
public class ProfileFile {

  private static final Logger log = Logger.getLogger(ProfileFile.class);

  @JacksonXmlProperty(localName = "name")
  private String name;

  @JacksonXmlProperty(localName = "version")
  private String version;

  @JacksonXmlProperty(localName = "author")
  private String author;

  @JacksonXmlProperty(localName = "requirements")
  private ArrayList<Step> requirements;

  @JacksonXmlProperty(localName = "schemaInferences")
  private ArrayList<Step> schemaInferences;

  @JacksonXmlProperty(localName = "dataInferences")
  private ArrayList<Step> dataInferences;

  @JacksonXmlProperty(localName = "rules")
  private ArrayList<Step> rules;


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

  public ArrayList<Step> getRequirements() {
    return requirements;
  }
  public void setRequirements(ArrayList<Step> requirements) {
    this.requirements = requirements;
  }

  public ArrayList<Step> getSchemaInferences() {
    return schemaInferences;
  }
  public void setSchemaInferences(ArrayList<Step> schemaInferences) {
    this.schemaInferences = schemaInferences;
  }

  public ArrayList<Step> getDataInferences() {
    return dataInferences;
  }
  public void setDataInferences(ArrayList<Step> dataInferences) {
    this.dataInferences = dataInferences;
  }

  public ArrayList<Step> getRules() {
    return rules;
  }
  public void setRules(ArrayList<Step> rules) {
    this.rules = rules;
  }
}
