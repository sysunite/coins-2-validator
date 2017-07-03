package com.sysunite.coinsweb.parser.config.pojo;

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
  private String variable;

  public String getGraphname() {
    return graphname;
  }
  public String getVariable() {
    return variable;
  }


  public void setGraphname(String graphname) {
    this.graphname = graphname;
  }

  public void setVariable(String variable) {
    this.variable = variable;
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
