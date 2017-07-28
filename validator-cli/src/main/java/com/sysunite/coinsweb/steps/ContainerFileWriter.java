package com.sysunite.coinsweb.steps;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.sysunite.coinsweb.filemanager.ContainerFile;
import com.sysunite.coinsweb.filemanager.ContainerFileImpl;
import com.sysunite.coinsweb.graphset.ContainerGraphSet;
import com.sysunite.coinsweb.graphset.GraphVar;
import com.sysunite.coinsweb.parser.config.factory.FileFactory;
import com.sysunite.coinsweb.parser.config.pojo.*;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;


/**
 * @author bastbijl, Sysunite 2017
 */
@JsonInclude(JsonInclude.Include.NON_NULL) // todo wrong this line?
public class ContainerFileWriter extends ConfigPart implements ValidationStep {

  private static final Logger log = LoggerFactory.getLogger(ContainerFileWriter.class);

  public static final String REFERENCE = "ContainerFileWriter";


  // Configuration items
  private String type = REFERENCE;
  public String getType() {
    return type;
  }
  public void setType(String type) {
    this.type = type;
  }

  private Locator location;
  public Locator getLocation() {
    return location;
  }
  public void setLocation(Locator location) {
    this.location = location;
  }

  @Override
  public void setParent(Object parent) {
    super.setParent(parent);
    if(this.location != null) {
      this.location.setParent(parent);
    }
  }


  // Result items
  private boolean failed = true;
  public boolean getFailed() {
    return failed;
  }
  public void setFailed(boolean failed) {
    this.failed = failed;
  }

  private boolean valid = false;
  public boolean getValid() {
    return valid;
  }
  public void setValid(boolean valid) {
    this.valid = valid;
  }

  public void checkConfig() {
  }

  @Override
  public void execute(ContainerFile container, ContainerGraphSet graphSet) {


    try {

      if(graphSet.getMain() == null) {
        throw new RuntimeException("No main context (graphname) is set");
      }
      GraphVarImpl main = (GraphVarImpl) graphSet.getMain();

      try {
        log.info("Dump graphSet contexts to temp files:");
        for(GraphVar graphVar : graphSet.contextMap().keySet()) {
          File rdfFile = File.createTempFile(RandomStringUtils.random(8, true, true), ".rdf");
          OutputStream outputStream = new FileOutputStream(rdfFile);
          String context = graphSet.contextMap().get(graphVar);
          graphSet.writeContextToFile(new String[]{context}, outputStream);
          outputStream.close();
          log.info("Compare "+context+" to "+main);
          for(Graph graph : ((ContainerFileImpl)container).getConfig().getGraphs()) {
            if(graph.getAs().contains(graphVar)) {
              if(graph.getMain() != null && graph.getMain()) {
                log.info("- "+graphVar+" to "+rdfFile.getName()+" (is main)");
                ((ContainerFileImpl)container).addContentFile(rdfFile, graph.getSource().getGraphname());
              } else {
                log.info("- "+graphVar+" to "+rdfFile.getName());
                ((ContainerFileImpl)container).addLibraryFile(rdfFile, graph.getSource().getGraphname());
              }
            }
          }

        }

        log.info("Register the configured files as attachments:");
        for(Attachment attachment : ((ContainerFileImpl)container).getConfig().getAttachments()) {
          ((ContainerFileImpl)container).addAttachmentFile(FileFactory.toFile(attachment.getLocation()));
        }
      } catch (FileNotFoundException e) {
        e.printStackTrace();
      } catch (IOException e) {
        e.printStackTrace();
      }


      File ccrFile;
      if (location.getParent() != null) {
        ccrFile = location.getParent().resolve(location.getPath()).toFile();
      } else {
        ccrFile = new File(location.getPath());
      }

      log.info("Save the container file to: "+ccrFile.getPath());
      ((ContainerFileImpl)container).writeZip(ccrFile.toPath());




      valid = true;
      failed = false;

    } catch (RuntimeException e) {
      log.warn("Executing failed validationStep of type "+getType());
      log.warn(e.getMessage(), e);
      failed = true;
    }
  }

  @JsonIgnore
  public ContainerFileWriter clone() {
    ContainerFileWriter clone = new ContainerFileWriter();

    // Configuration
    clone.setType(this.getType());
    clone.setLocation(this.getLocation().clone());
    clone.setParent(this.getParent());

    // Results
    clone.setValid(this.getValid());
    clone.setFailed(this.getFailed());
    return clone;
  }



}
