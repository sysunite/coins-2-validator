package com.sysunite.coinsweb.parser.config.pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.util.StdConverter;
import com.sysunite.coinsweb.filemanager.ContainerFile;
import com.sysunite.coinsweb.graphset.GraphVar;
import com.sysunite.coinsweb.steps.ValidationStep;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.sysunite.coinsweb.parser.Parser.*;

/**
 * @author bastbijl, Sysunite 2017
 */
@JsonInclude(Include.NON_NULL)
@JsonDeserialize(converter=ContainerSanitizer.class)
public class Container extends ConfigPart {

  private static final Logger log = LoggerFactory.getLogger(Environment.class);

  public static final String CONTAINER = "container";
  public static final String VIRTUAL = "virtual";

  private String type;
  private Locator location;
  private Mapping[] variables = new Mapping[0];
  private Graph[] graphs = new Graph[0];
  @JsonInclude(Include.NON_EMPTY)
  private Attachment[] attachments = new Attachment[0];
  @JsonInclude(Include.NON_EMPTY)
  private ArrayList<ValidationStep> steps = new ArrayList();
  private Boolean valid;
  @JsonIgnore
  private ContainerFile containerFile;

  @JsonIgnore
  private String code;

  public Container() {
    this.code = RandomStringUtils.random(8, true, true);
  }

  public String getType() {
    return type;
  }
  public Locator getLocation() {
    return location;
  }
  public Mapping[] getVariables() {
    return variables;
  }
  @JsonIgnore
  public HashMap<GraphVar, String> getVariablesMap() {
    HashMap<GraphVar, String> graphs = new HashMap();
    for(Mapping mapping : getVariables()) {
      graphs.put(mapping.getVariable(), mapping.getGraphname());
    }
    return graphs;
  }
  public Graph[] getGraphs() {
    return graphs;
  }
  public Attachment[] getAttachments() {
    return attachments;
  }
  public ArrayList<ValidationStep> getSteps() {
    return steps;
  }
  public Boolean getValid() {
    return valid;
  }
  public ContainerFile getContainerFile() {
    return containerFile;
  }

  @JsonIgnore
  public String getCode() {
    return code;
  }
  @JsonIgnore
  public boolean isVirtual() {
    return VIRTUAL.equals(this.type);
  }


  public void setType(String type) {
    validate(type, CONTAINER, VIRTUAL);
    this.type = type;
  }
  public void setLocation(Locator location) {
    this.location = location;
    this.location.setParent(this.getParent());
  }
  public void setVariables(Mapping[] variables) {
    this.variables = variables;
    for(Mapping mapping : this.variables) {
      mapping.setParent(this.getParent());
    }
  }
  public void updateVariables(Map<GraphVar, String> map) {
    ArrayList<Mapping> list = new ArrayList<>();
    for(GraphVar graphVar : map.keySet()) {
      list.add(new Mapping((GraphVarImpl)graphVar, map.get(graphVar)));
    }
    setVariables(list.toArray(new Mapping[0]));
  }
  public void setGraphs(Graph[] graphs) {
    this.graphs = graphs;
    for(Graph graph : this.graphs) {
      graph.setParent(this.getParent());
    }
  }
  public void setAttachments(Attachment[] attachments) {
    this.attachments = attachments;
    for(Attachment attachment : this.attachments) {
      attachment.setParent(this.getParent());
    }
  }
  public void addStep(ValidationStep step) {
    this.steps.add(step);
  }
  public void setValid(Boolean valid) {
    this.valid = valid;
  }
  public void setContainer(ContainerFile containerFile) {
    this.containerFile = containerFile;
  }

  @Override
  public void setParent(Object parent) {
    super.setParent(parent);
    if(this.location != null) {
      this.location.setParent(this.getParent());
    }
    for(Mapping mapping : this.variables) {
      mapping.setParent(this.getParent());
    }
    for(Graph graph : this.graphs) {
      graph.setParent(this.getParent());
    }
    for(Attachment attachment : this.attachments) {
      attachment.setParent(this.getParent());
    }
  }


  @JsonIgnore
  public Container clone() {
    Container clone = new Container();
    clone.setType(this.type);
    clone.setLocation(this.location.clone());
    Mapping[] variables = new Mapping[this.variables.length];
    for(int i = 0; i < this.variables.length; i++) {
      variables[i] = this.variables[i].clone();
    }
    clone.setVariables(variables);
    Graph[] graphs = new Graph[this.graphs.length];
    for(int i = 0; i < this.graphs.length; i++) {
      graphs[i] = this.graphs[i].clone();
    }
    clone.setGraphs(graphs);
    clone.setParent(this.getParent());
    return clone;
  }
}

class ContainerSanitizer extends StdConverter<Container, Container> {

  private static final Logger log = LoggerFactory.getLogger(ContainerSanitizer.class);

  @Override
  public Container convert(Container obj) {
    if(Container.CONTAINER.equals(obj.getType())) {
      isNotNull(obj.getLocation());
    }
    if(Container.VIRTUAL.equals(obj.getType())) {
      isNull(obj.getLocation());

      // Not allowed to have sources of type container
      for(Graph graph : obj.getGraphs()) {
        if(Source.CONTAINER.equals(graph.getSource().getType())) {
          throw new RuntimeException("A source of type 'container' is not allowed for a virtual container");
        }
      }
    }
    isNotEmpty(obj.getGraphs());

    return obj;
  }
}
