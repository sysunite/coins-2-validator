package com.sysunite.coinsweb.parser.config;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.util.StdConverter;
import org.apache.log4j.Logger;

import static com.sysunite.coinsweb.parser.config.Parser.*;

/**
 * @author bastbijl, Sysunite 2017
 */
@JsonDeserialize(converter=ReportSanitizer.class)
public class Report {

  private static final Logger log = Logger.getLogger(Report.class);

  public static final String XML = "xml";
  public static final String HTML = "html";
  public static final String DEBUG = "debug";

  private String type;
  private Locator location;

  public String getType() {
    return type;
  }
  public Locator getLocation() {
    return location;
  }

  public void setType(String type) {
    validate(type, "xml", "html", "debug");
    this.type = type;
  }
  public void setLocation(Locator location) {
    this.location = location;
  }

}

class ReportSanitizer extends StdConverter<Report, Report> {

  private static final Logger log = Logger.getLogger(ReportSanitizer.class);

  @Override
  public Report convert(Report obj) {

    isNotNull(obj.getType());
    if(obj.getLocation() != null && "file".equals(obj.getLocation().getType())) {
      canCreateFile(obj.getLocation().getPath());
    }

    return obj;
  }
}

