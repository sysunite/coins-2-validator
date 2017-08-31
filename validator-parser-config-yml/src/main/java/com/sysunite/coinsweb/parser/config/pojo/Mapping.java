package com.sysunite.coinsweb.parser.config.pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.util.StdConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

import static com.sysunite.coinsweb.parser.Parser.isNotNull;

/**
 * @author bastbijl, Sysunite 2017
 */
@JsonInclude(Include.NON_NULL)
@JsonDeserialize(converter=MappingSanitizer.class)
public class Mapping extends ConfigPart {

  private static final Logger log = LoggerFactory.getLogger(Mapping.class);

  private GraphVarImpl variable;
  private String graphname;
  private String filename;
  private String hash;
//  @JsonIgnore
  private Set<String> inclusionSet;
  private Set<String> bundleFingerPrints;
  @JsonIgnore
  private boolean initialized = false;

  public Mapping() {
  }

  public Mapping(GraphVarImpl graphVar, String graphName) {
    this.variable = graphVar;
    this.graphname = graphName;
  }
  public Mapping(GraphVarImpl graphVar, String graphName, String fileName, String hash, Set<String> inclusionSet, Set<String> bundleFingerPrints) {
    this.variable = graphVar;
    this.graphname = graphName;
    this.filename = fileName;
    this.hash = hash;
    this.inclusionSet = inclusionSet;
    this.bundleFingerPrints = bundleFingerPrints;
  }

  public GraphVarImpl getVariable() {
    return variable;
  }
  public String getGraphname() {
    return graphname;
  }
  public String getFilename() {
    return filename;
  }
  public String getHash() {
    return hash;
  }
  public Set<String> getInclusionSet() {
    return inclusionSet;
  }
  public Set<String> getBundleFingerPrints() {
    return bundleFingerPrints;
  }
  public boolean getInitialized() {
    return initialized;
  }

  public void setVariable(GraphVarImpl variable) {
    this.variable = variable;
  }
  public void setGraphname(String graphname) {
    this.graphname = graphname;
  }
  public void setFilename(String filename) {
    this.filename = filename;
  }
  public void setHash(String hash) {
    this.hash = hash;
  }
  public void setInclusionSet(Set<String> inclusionSet) {
    this.inclusionSet = inclusionSet;
  }
  public void setBundleFingerPrints(Set<String> bundleFingerPrints) {
    this.bundleFingerPrints = bundleFingerPrints;
  }
  public void setInitialized() {
    this.initialized = true;
  }

  @JsonIgnore
  public Mapping clone() {
    Mapping clone = new Mapping();
    clone.setVariable(this.variable);
    clone.setGraphname(this.graphname);
    clone.setFilename(this.filename);
    clone.setHash(this.hash);
    clone.setInclusionSet(this.getInclusionSet());
    clone.setBundleFingerPrints(this.getBundleFingerPrints());
    clone.setParent(this.getParent());
    if(this.getInitialized()) {
      clone.setInitialized();
    }
    return clone;
  }
}

class MappingSanitizer extends StdConverter<Mapping, Mapping> {

  private static final Logger log = LoggerFactory.getLogger(MappingSanitizer.class);

  @Override
  public Mapping convert(Mapping obj) {

    isNotNull(obj.getGraphname());
    isNotNull(obj.getVariable());

    return obj;
  }
}
