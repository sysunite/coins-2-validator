package com.sysunite.coinsweb.parser.config.pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.util.StdConverter;
import com.sysunite.coinsweb.filemanager.ContainerFile;
import com.sysunite.coinsweb.steps.ValidationStep;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

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
  private List<Mapping> variables = new ArrayList<>();
  private List<Graph> graphs = new ArrayList<>();
  @JsonInclude(Include.NON_EMPTY)
  private List<Attachment> attachments = new ArrayList<>();
  @JsonInclude(Include.NON_EMPTY)
  private List<ValidationStep> steps = new ArrayList<>();
  private Boolean valid;
  @JsonIgnore
  private ContainerFile containerFile;

  @JsonIgnore
  private String code;

  public Container() {
    this.code = RandomStringUtils.random(8, true, true);
  }


  private String mapSigmaContext(String confContext) {
    return confContext + "-" + code;
  }

  public String getType() {
    return type;
  }
  public Locator getLocation() {
    return location;
  }
  public List<Mapping> getVariables() {
    for(Mapping mapping : variables) {
      if(!mapping.getInitialized()) {
        mapping.setGraphname(mapSigmaContext(mapping.getGraphname()));
      }
      mapping.setInitialized();
    }
    return variables;
  }
  public List<Graph> getGraphs() {
    return graphs;
  }
  public List<Attachment> getAttachments() {
    return attachments;
  }
  public List<ValidationStep> getSteps() {
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
  public void setVariables(List<Mapping> variables) {
    this.variables = variables;
    for(Mapping mapping : this.variables) {
      mapping.setParent(this.getParent());
    }
  }
  public void setGraphs(List<Graph> graphs) {
    this.graphs = graphs;
    for(Graph graph : this.graphs) {
      graph.setParent(this.getParent());
    }
  }
  public void setAttachments(List<Attachment> attachments) {
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
    List<Mapping> variables = new ArrayList<>();
    for(Mapping variable : this.variables) {
      variables.add(variable.clone());
    }
    clone.setVariables(variables);
    List<Graph> graphs = new ArrayList<>();
    for(Graph graph : this.graphs) {
      graphs.add(graph.clone());
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
