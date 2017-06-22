package com.sysunite.coinsweb.parser.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author bastbijl, Sysunite 2017
 */
@JsonInclude(Include.NON_NULL)
@JsonDeserialize(converter=MappingSanitizer.class)
public class Mapping {

  private static final Logger log = LoggerFactory.getLogger(Mapping.class);

  private String graphname;
  private String content;

  public String getGraphname() {
    return graphname;
  }
  public String getContent() {
    return content;
  }


  public void setGraphname(String graphname) {
    this.graphname = graphname;
  }

  public void setContent(String content) {
    this.content = content;
  }
}
