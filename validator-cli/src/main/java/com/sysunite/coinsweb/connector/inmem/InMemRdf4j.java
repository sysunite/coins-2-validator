package com.sysunite.coinsweb.connector.inmem;

import com.sysunite.coinsweb.connector.Rdf4jConnector;
import com.sysunite.coinsweb.parser.config.pojo.Environment;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author bastbijl, Sysunite 2017
 */
public class InMemRdf4j extends Rdf4jConnector {

  private static final Logger log = LoggerFactory.getLogger(InMemRdf4j.class);




  public InMemRdf4j(Environment config) {

//    log.info(config.getConfig().containsKey("custom"));
//    log.info(config.getConfig().containsKey("endpoint"));
//    log.info(config.getConfig().containsKey("user"));
//    log.info(config.getConfig().containsKey("password"));





  }

  public void init() {
    repository = new SailRepository(new MemoryStore());
    repository.initialize();
  }



}
