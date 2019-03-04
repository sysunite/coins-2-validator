package com.sysunite.coinsweb.parser.config.pojo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.util.StdConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.sysunite.coinsweb.parser.Parser.isNotNull;

/**
 * @author bastbijl, Sysunite 2017
 */
@JsonInclude(Include.NON_NULL)
@JsonDeserialize(converter=AttachmentSanitizer.class)
public class Attachment extends ConfigPart {
  private static final Logger log = LoggerFactory.getLogger(Graph.class);

  private Locator location;
  private String as;

  public Locator getLocation() {
    return location;
  }
  public String getAs() {
    return as;
  }

  public void setLocation(Locator location) {
    this.location = location;
  }
  public void setAs(String as) {
    this.as = as;
  }

  @Override
  public void setParent(Object parent) {
    super.setParent(parent);
    this.location.setParent(this.getParent());
  }
}

class AttachmentSanitizer extends StdConverter<Attachment, Attachment> {
  private static final Logger log = LoggerFactory.getLogger(GraphSanitizer.class);

  @Override
  public Attachment convert(Attachment obj) {

    isNotNull(obj.getLocation());
    isNotNull(obj.getAs());
    return obj;
  }
}
