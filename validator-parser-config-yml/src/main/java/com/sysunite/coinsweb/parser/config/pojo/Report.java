package com.sysunite.coinsweb.parser.config.pojo;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.util.StdConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.sysunite.coinsweb.parser.Parser.*;

/**
 * @author bastbijl, Sysunite 2017
 */
@JsonDeserialize(converter=ReportSanitizer.class)
public class Report extends ConfigPart {

  private static final Logger log = LoggerFactory.getLogger(Report.class);

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
    this.location.setParent(this.getParent());
  }



  @Override
  public void setParent(ConfigFile parent) {
    super.setParent(parent);
    if(this.location != null) {
      this.location.setParent(parent);
    }
  }

}

class ReportSanitizer extends StdConverter<Report, Report> {

  private static final Logger log = LoggerFactory.getLogger(ReportSanitizer.class);

  @Override
  public Report convert(Report obj) {

    isNotNull(obj.getType());

    return obj;
  }
}
