package com.sysunite.coinsweb.report;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.sysunite.coinsweb.parser.config.pojo.Container;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author bastbijl, Sysunite 2017
 */
@JsonInclude(Include.NON_NULL)
public class ReportFile {
  private static final Logger log = LoggerFactory.getLogger(ReportFile.class);

  @JacksonXmlProperty(localName = "container")
  @JacksonXmlElementWrapper(localName="containers")
  private Container[] containers;

  public Container[] getContainers() {
    return containers;
  }

  public void setContainers(Container[] containers) {
    this.containers = containers;
  }
}
