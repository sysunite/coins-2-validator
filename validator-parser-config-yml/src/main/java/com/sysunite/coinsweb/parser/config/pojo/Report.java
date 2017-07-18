package com.sysunite.coinsweb.parser.config.pojo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.util.StdConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.sysunite.coinsweb.parser.Parser.*;

/**
 * @author bastbijl, Sysunite 2017
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonDeserialize(converter=ReportSanitizer.class)
public class Report extends ConfigPart {

  private static final Logger log = LoggerFactory.getLogger(Report.class);

  public static final String XML = "xml";
  public static final String HTML = "html";
  public static final String CUSTOM = "custom";

  private String type;
  private Locator location;
  private Locator template;

  public String getType() {
    return type;
  }
  public Locator getLocation() {
    return location;
  }
  public Locator getTemplate() {
    return template;
  }

  public void setType(String type) {
    validate(type, XML, HTML, CUSTOM);
    this.type = type;
  }
  public void setLocation(Locator location) {
    this.location = location;
    this.location.setParent(this.getParent());
  }
  public void setTemplate(Locator template) {
    this.template = template;
    this.template.setParent(this.getParent());
  }



  @Override
  public void setParent(Object parent) {
    super.setParent(parent);
    if(this.location != null) {
      this.location.setParent(parent);
    }
    if(this.template != null) {
      this.template.setParent(parent);
    }
  }

}

class ReportSanitizer extends StdConverter<Report, Report> {

  private static final Logger log = LoggerFactory.getLogger(ReportSanitizer.class);

  @Override
  public Report convert(Report obj) {

    isNotNull(obj.getType());
    isNotNull(obj.getLocation());
    if(Report.CUSTOM.equals(obj.getType())) {
      isNotNull(obj.getTemplate());
    }

    return obj;
  }
}
