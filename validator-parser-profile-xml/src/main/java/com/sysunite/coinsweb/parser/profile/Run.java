package com.sysunite.coinsweb.parser.profile;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.util.ArrayList;

/**
 * @author bastbijl, Sysunite 2017
 */
public class Run {

  @JacksonXmlProperty(localName = "name")
  private String name;

  @JacksonXmlProperty(localName = "step")
  @JacksonXmlElementWrapper(useWrapping = false)
  private ArrayList<Step> steps;


  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  public ArrayList<Step> getSteps() {
    return steps;
  }
  public void setSteps(ArrayList<Step> steps) {
    this.steps = steps;
  }
}
