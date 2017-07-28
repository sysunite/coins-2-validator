package com.sysunite.coinsweb.parser.config.pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.util.StdConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.sysunite.coinsweb.parser.Parser.isNotNull;

/**
 * @author bastbijl, Sysunite 2017
 */
@JsonInclude(Include.NON_NULL)
@JsonDeserialize(converter=MappingSanitizer.class)
public class Mapping extends ConfigPart {

  private static final Logger log = LoggerFactory.getLogger(Mapping.class);

  private String graphname;
  private String filename;
  private GraphVarImpl variable;

  public Mapping() {
  }

  public Mapping(GraphVarImpl graphVar, String graphName) {
    this.variable = graphVar;
    this.graphname = graphName;
  }
  public Mapping(GraphVarImpl graphVar, String graphName, String fileName) {
    this.variable = graphVar;
    this.graphname = graphName;
    this.filename = fileName;
  }

  public String getGraphname() {
    return graphname;
  }
  public String getFilename() {
    return filename;
  }
  public GraphVarImpl getVariable() {
    return variable;
  }

  public void setGraphname(String graphname) {
    this.graphname = graphname;
  }
  public void setFilename(String filename) {
    this.filename = filename;
  }
  public void setVariable(GraphVarImpl variable) {
    this.variable = variable;
  }

  @JsonIgnore
  public Mapping clone() {
    Mapping clone = new Mapping();
    clone.setGraphname(this.graphname);
    clone.setFilename(this.filename);
    clone.setVariable(this.variable);
    clone.setParent(this.getParent());
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
