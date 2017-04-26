package com.sysunite.coinsweb.config;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.util.StdConverter;
import org.apache.log4j.Logger;

import static com.sysunite.coinsweb.config.Parser.isNotNull;
import static com.sysunite.coinsweb.config.Parser.validate;

/**
 * @author bastbijl, Sysunite 2017
 */
@JsonDeserialize(converter=StepSanitizer.class)
public class Step {

  private static final Logger log = Logger.getLogger(Step.class);

  private String type;
  private Locator profile;
  private int maxResults;

  public String getType() {
    return type;
  }
  public int getMaxResults() {
    return maxResults;
  }
  public Locator getProfile() {
    return profile;
  }

  public void setType(String type) {
    validate(type, "FileSystemValidation", "ProfileValidation");
    this.type = type;
  }

  public void setMaxResults(int maxResults) {
    this.maxResults = maxResults;
  }

  public void setProfile(Locator profile) {
    this.profile = profile;
  }
}

class StepSanitizer extends StdConverter<Step, Step> {

  private static final Logger log = Logger.getLogger(StepSanitizer.class);

  @Override
  public Step convert(Step obj) {
    isNotNull(obj.getType());

    if(obj.getType().equals("ProfileValidation")) {
      isNotNull(obj.getProfile());
    }

    return obj;
  }
}