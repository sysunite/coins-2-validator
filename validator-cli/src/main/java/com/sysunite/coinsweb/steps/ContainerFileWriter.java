package com.sysunite.coinsweb.steps;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sysunite.coinsweb.filemanager.ContainerFile;
import com.sysunite.coinsweb.filemanager.VirtualContainerFileImpl;
import com.sysunite.coinsweb.graphset.ContainerGraphSet;
import com.sysunite.coinsweb.graphset.GraphVar;
import com.sysunite.coinsweb.parser.config.factory.FileFactory;
import com.sysunite.coinsweb.parser.config.pojo.Attachment;
import com.sysunite.coinsweb.parser.config.pojo.ConfigPart;
import com.sysunite.coinsweb.parser.config.pojo.Graph;
import com.sysunite.coinsweb.parser.config.pojo.Locator;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.HashMap;
import java.util.Map;


/**
 * @author bastbijl, Sysunite 2017
 */
@JsonInclude(JsonInclude.Include.NON_NULL) // todo wrong this line?
public class ContainerFileWriter extends ConfigPart implements ValidationStep {

  private static final Logger log = LoggerFactory.getLogger(ContainerFileWriter.class);


  // Configuration items
  private String type = "ContainerFileWriter";
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
  public Map<String, Object> execute(ContainerFile container, ContainerGraphSet graphSet) {

    if(!(container instanceof VirtualContainerFileImpl)) {
      throw new RuntimeException("Only virtual container files can be stored for now");
    }

    try {




      if(graphSet.getMain() == null) {
        throw new RuntimeException("No main context (graphname) is set");
      }
      Graph main = (Graph) graphSet.getMain();

      try {
        log.info("Dump graphSet contexts to temp files:");
        for(GraphVar graphVar : graphSet.contextMap().keySet()) {
          File rdfFile = File.createTempFile(RandomStringUtils.random(8, true, true), ".rdf");
          OutputStream outputStream = new FileOutputStream(rdfFile);
          String context = graphSet.contextMap().get(graphVar);
          graphSet.writeContextToFile(new String[]{context}, outputStream);
          outputStream.close();
          log.info("Compare "+context+" to "+main.getSource().getGraphname());
          for(Graph graph : ((VirtualContainerFileImpl)container).getConfig().getGraphs()) {
            if(graph.getAs().contains(graphVar)) {
              if(graph.getMain() != null && graph.getMain()) {
                log.info("- "+graphVar+" to "+rdfFile.getName()+" (is main)");
                ((VirtualContainerFileImpl)container).addContentFile(rdfFile, graph.getSource().getGraphname());
              } else {
                log.info("- "+graphVar+" to "+rdfFile.getName());
                ((VirtualContainerFileImpl)container).addLibraryFile(rdfFile, graph.getSource().getGraphname());
              }
            }
          }

        }

        log.info("Register the configured files as attachments:");
        for(Attachment attachment : ((VirtualContainerFileImpl)container).getConfig().getAttachments()) {
          ((VirtualContainerFileImpl)container).addAttachmentFile(FileFactory.toFile(attachment.getLocation()));
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
      ((VirtualContainerFileImpl)container).writeZip(ccrFile.toPath());




      this.valid = true;
      this.failed = false;

    } catch (RuntimeException e) {
      log.warn("Executing failed validationStep of type "+getType());
      log.warn(e.getMessage(), e);
      this.failed = true;
    }

    // Prepare data to transfer to the template
    Map<String, Object> reportItems = new HashMap();

    reportItems.put("failed",     getFailed());
    reportItems.put("valid",      getValid());


    return reportItems;
  }



}
