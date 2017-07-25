package com.sysunite.coinsweb.connector.inmem;

import com.sysunite.coinsweb.connector.Rdf4jConnector;
import com.sysunite.coinsweb.parser.config.pojo.Environment;
import org.apache.commons.io.FileUtils;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * @author bastbijl, Sysunite 2017
 */
public class InMemRdf4j extends Rdf4jConnector {

  private static final Logger log = LoggerFactory.getLogger(InMemRdf4j.class);

  public static final String REFERENCE = "rdf4j-sail-memory";

  private boolean useDisk;
  private File tempFolder;

  public InMemRdf4j(Environment config) {
    useDisk = config.getUseDisk();
    if(useDisk) {
      try {
        File temp = File.createTempFile("temp-file-name", ".tmp");
        tempFolder = temp.getParentFile();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  public void init() {
    if(initialized) {
      return;
    }

    log.info("Initialize connector ("+REFERENCE+")");

    MemoryStore memStore;
    if(useDisk) {
      memStore = new MemoryStore(tempFolder);
    } else {
      memStore = new MemoryStore();
    }
    repository = new SailRepository(memStore);

    repository.initialize();
    initialized = true;
  }

  public void close() {
    if(!initialized) {
      return;
    }
    repository.shutDown();
    if(useDisk) {
      try {
        FileUtils.deleteDirectory(tempFolder);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }



}
