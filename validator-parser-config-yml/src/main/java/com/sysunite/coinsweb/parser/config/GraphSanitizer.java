package com.sysunite.coinsweb.parser.config;

import com.fasterxml.jackson.databind.util.StdConverter;
import org.apache.log4j.Logger;

import static com.sysunite.coinsweb.parser.Parser.*;

/**
 * @author bastbijl, Sysunite 2017
 */
class GraphSanitizer extends StdConverter<Graph, Graph> {

  private static final Logger log = Logger.getLogger(GraphSanitizer.class);

  @Override
  public Graph convert(Graph obj) {

    isNotNull(obj.getGraphname());
    isNotNull(obj.getContent());
    isNotNull(obj.getType());

    if(obj.getType().equals("file")) {
      isFile(obj.getPath());
    }
    if(obj.getType().equals("online")) {
      isResolvable(obj.getUri());
    }
    if(obj.getType().equals("container")) {
      // a container should be referenced in the run section
    }
    if(obj.getType().equals("store")) {
      isNotNull(obj.getStore());
    }
    return obj;
  }
}