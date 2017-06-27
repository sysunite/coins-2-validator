package com.sysunite.coinsweb.parser.config.pojo;

import com.fasterxml.jackson.databind.util.StdConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.sysunite.coinsweb.parser.Parser.*;

/**
 * @author bastbijl, Sysunite 2017
 */
class GraphSanitizer extends StdConverter<Graph, Graph> {

  private static final Logger log = LoggerFactory.getLogger(GraphSanitizer.class);

  @Override
  public Graph convert(Graph obj) {

    isNotNull(obj.getGraphname());
    isNotNull(obj.getContent());
    isNotNull(obj.getType());

    if(obj.getType().equals("file")) {
      if(obj.getPath() != null && obj.getPath().contains("*")) {
        if(!obj.anyContentFile() && !obj.anyLibraryFile()) {
          throw new RuntimeException("Only wildcard allowed for bim or bim-repository folder");
        }
      }
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