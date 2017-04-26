package com.sysunite.coinsweb.config;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.util.StdConverter;
import org.apache.log4j.Logger;

import static com.sysunite.coinsweb.config.Parser.*;

/**
 * @author bastbijl, Sysunite 2017
 */
@JsonDeserialize(converter=ReportSanitizer.class)
public class Report {

  private static final Logger log = Logger.getLogger(Report.class);

  private String type;
  private String path;
  private Locator template;

  public String getType() {
    return type;
  }
  public String getPath() {
    return path;
  }
  public Locator getTemplate() {
    return template;
  }

  public void setType(String type) {
    validate(type, "xml", "html", "custom");
    this.type = type;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public void setTemplate(Locator template) {
    this.template = template;
  }
}

class ReportSanitizer extends StdConverter<Report, Report> {

  private static final Logger log = Logger.getLogger(ReportSanitizer.class);

  @Override
  public Report convert(Report obj) {

    isNotNull(obj.getType());
    canCreateFile(obj.getPath());

    if(obj.getType().equals("xml") || obj.getType().equals("html")) {
      isNull(obj.getTemplate());
    }

    if(obj.getType().equals("custom")) {
      isNotNull(obj.getTemplate());
    }

    return obj;
  }
}

