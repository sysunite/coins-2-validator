package com.sysunite.coinsweb.steps;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sysunite.coinsweb.filemanager.ContainerFile;
import com.sysunite.coinsweb.graphset.ContainerGraphSet;
import com.sysunite.coinsweb.graphset.GraphVar;
import com.sysunite.coinsweb.graphset.QueryFactory;
import com.sysunite.coinsweb.parser.config.factory.FileFactory;
import com.sysunite.coinsweb.parser.config.pojo.ConfigPart;
import com.sysunite.coinsweb.parser.config.pojo.Locator;
import com.sysunite.coinsweb.parser.profile.pojo.ProfileFile;
import com.sysunite.coinsweb.steps.profile.ValidationExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author bastbijl, Sysunite 2017
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProfileValidation extends ConfigPart implements ValidationStep {

  private static final Logger log = LoggerFactory.getLogger(ProfileValidation.class);


  // Configuration items
  private String type = "ProfileValidation";
  private Locator profile;
  private int maxResults;

  public String getType() {
    return type;
  }
  public Locator getProfile() {
    return profile;
  }
  public int getMaxResults() {
    return maxResults;
  }

  public void setType(String type) {
    this.type = type;
  }
  public void setProfile(Locator profile) {
    this.profile = profile;
    this.profile.setParent(this.getParent());
  }
  public void setMaxResults(int maxResults) {
    this.maxResults = maxResults;
  }

  @Override
  public void setParent(Object parent) {
    super.setParent(parent);
    if(this.profile != null) {
      this.profile.setParent(parent);
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

  private HashMap<String, HashMap<String, Object>> bundleResults;
  public HashMap<String, HashMap<String, Object>> getBundleResults() {
    return bundleResults;
  }
  public void setBundleResults(HashMap<String, HashMap<String, Object>> bundleResults) {
    this.bundleResults = bundleResults;
  }

  public void checkConfig() {
  }

  @Override
  public Map<String, Object> execute(ContainerFile container, ContainerGraphSet graphSet) {

    try {

      // Load the profile file
      InputStream inputStream = FileFactory.toInputStream(profile);
      ProfileFile profileFile = ProfileFile.parse(inputStream);

      // Check if the vars used in the profile are available
      Set<GraphVar> usedVars = QueryFactory.usedVars(profileFile);
      for (GraphVar graphVar : usedVars) {
        if (!graphSet.hasContext(graphVar)) {
          throw new RuntimeException("The specified profile requires " + graphVar + ", please specify it in the config.yml");
        }
      }

      ValidationExecutor executor = new ValidationExecutor(profileFile, graphSet, this, graphSet.getConnector());

      // Execute the validation
      executor.validate();
    } catch (RuntimeException e) {
      log.warn("Executing failed validationStep of type "+getType());
      log.warn(e.getMessage());
      failed = true;
    }

    // Prepare data to transfer to the template
    if(getValid()) {
      log.info("\uD83E\uDD47 valid");
    } else {
      log.info("\uD83E\uDD48 invalid");
    }

    Map<String, Object> reportItems = new HashMap();

    reportItems.put("valid",         getValid());
    reportItems.put("bundleResults", getBundleResults());

    return reportItems;
  }




}



