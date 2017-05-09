package com.sysunite.coinsweb.parser.profile;


import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;



import java.util.ArrayList;

/**
 * @author bastbijl, Sysunite 2017
 */
@JacksonXmlRootElement(localName = "profile")
public class ProfileFile {



  @JacksonXmlProperty(localName = "name")
  private String name;

  @JacksonXmlProperty(localName = "version")
  private String version;

  @JacksonXmlProperty(localName = "author")
  private String author;

  @JacksonXmlProperty(localName = "requirements")
  private ArrayList<Step> requirements;




  private ArrayList<Run> runs;
  private ArrayList<Step> schemaInferences;
  private ArrayList<Step> dataInferences;
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

  public ArrayList<Run> getRuns() {
    return runs;
  }
  public void setRuns(ArrayList<Run> runs) {
    this.runs = runs;
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
