package com.sysunite.coinsweb.parser.config.pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.util.StdConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

import static com.sysunite.coinsweb.parser.Parser.isNotNull;

/**
 * @author bastbijl, Sysunite 2017
 */
@JsonInclude(Include.NON_NULL)
@JsonDeserialize(converter=GraphSanitizer.class)
public class Graph extends ConfigPart {

  private static final Logger log = LoggerFactory.getLogger(Graph.class);

  private Source source;
  private ArrayList<GraphVarImpl> as;
  private Boolean main;



  public Source getSource() {
    return source;
  }
  public ArrayList<GraphVarImpl> getAs() {
    return as;
  }
  public Boolean getMain() {
    return main;
  }

  public void setSource(Source source) {
    this.source = source;
  }
  public void setAs(ArrayList<GraphVarImpl> as) {
    this.as = as;
  }
  public void setMain(Boolean main) {
    this.main = main;
  }

  @JsonIgnore
  public Graph clone() {
    Graph clone = new Graph();
    clone.setSource(this.getSource().clone());
    clone.setAs((ArrayList<GraphVarImpl>) this.getAs().clone());
    clone.setMain(this.getMain());
    clone.setParent(this.getParent());
    return clone;
  }

  @Override
  public void setParent(Object parent) {
    super.setParent(parent);
    this.source.setParent(this.getParent());
  }
}

class GraphSanitizer extends StdConverter<Graph, Graph> {

  private static final Logger log = LoggerFactory.getLogger(GraphSanitizer.class);

  @Override
  public Graph convert(Graph obj) {

    isNotNull(obj.getSource());
    isNotNull(obj.getAs());


    return obj;
  }
}
