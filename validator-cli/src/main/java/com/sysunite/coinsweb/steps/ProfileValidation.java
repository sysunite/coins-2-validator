package com.sysunite.coinsweb.steps;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.sysunite.coinsweb.filemanager.ContainerFile;
import com.sysunite.coinsweb.graphset.ContainerGraphSet;
import com.sysunite.coinsweb.graphset.GraphVar;
import com.sysunite.coinsweb.graphset.QueryFactory;
import com.sysunite.coinsweb.parser.config.factory.FileFactory;
import com.sysunite.coinsweb.parser.config.pojo.ConfigPart;
import com.sysunite.coinsweb.parser.config.pojo.Locator;
import com.sysunite.coinsweb.parser.profile.pojo.Bundle;
import com.sysunite.coinsweb.parser.profile.pojo.ProfileFile;
import com.sysunite.coinsweb.steps.profile.ValidationExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * @author bastbijl, Sysunite 2017
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProfileValidation extends ConfigPart implements ValidationStep {

  private static final Logger log = LoggerFactory.getLogger(ProfileValidation.class);

  public static final String REFERENCE = "ProfileValidation";


  // Configuration items
  private String type = REFERENCE;
  private Locator profile;
  private int maxResults = 0;
  private int maxInferenceRuns = 50;
  private boolean reportInferenceResults = false;

  public String getType() {
    return type;
  }
  public Locator getProfile() {
    return profile;
  }
  public int getMaxResults() {
    return maxResults;
  }
  public int getMaxInferenceRuns() {
    return maxInferenceRuns;
  }
  public boolean getReportInferenceResults() {
    return reportInferenceResults;
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
  public void setMaxInferenceRuns(int maxInferenceRuns) {
    this.maxInferenceRuns = maxInferenceRuns;
  }
  public void setReportInferenceResults(boolean reportInferenceResults) {
    this.reportInferenceResults = reportInferenceResults;
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

  @JacksonXmlProperty(localName = "name")
  @JacksonXmlElementWrapper(localName="bundleNames")
  private List<String> bundleNames = new ArrayList();
  public List<String> getBundleNames() {
    return bundleNames;
  }
  public void setBundleNames(List<String> bundleNames) {
    this.bundleNames = bundleNames;
  }


  @JsonSerialize(keyUsing = BundleKeySerializer.class)
  private HashMap<String, Bundle> bundles = new HashMap();
  public HashMap<String, Bundle> getBundles() {
    return bundles;
  }
  public void setBundles(HashMap<String, Bundle> bundles) {
    this.bundles = bundles;
  }
  public void addBundle(Bundle bundle) {
    this.bundleNames.add(bundle.getReference());
    this.bundles.put(bundle.getReference(), bundle);
  }

  public void checkConfig() {
  }

  @Override
  public void execute(ContainerFile container, ContainerGraphSet graphSet) {

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

      ValidationExecutor executor = new ValidationExecutor(profileFile, graphSet, this);

      // Execute the validation
      executor.validate();


    } catch (RuntimeException e) {
      log.warn("Executing failed validationStep of type "+getType());
      log.warn(e.getMessage(), e);
      failed = true;
    }

    // Prepare data to transfer to the template
    if(getFailed()) {
      log.info("\uD83E\uDD49 failed");
    } else {
      if (getValid()) {
        log.info("\uD83E\uDD47 valid");
      } else {
        log.info("\uD83E\uDD48 invalid");
      }
    }
  }


  @JsonIgnore
  public ProfileValidation clone() {
    ProfileValidation clone = new ProfileValidation();

    // Configuration
    clone.setType(this.getType());
    clone.setProfile(this.getProfile().clone());
    clone.setMaxResults(this.getMaxResults());
    clone.setMaxInferenceRuns(this.getMaxInferenceRuns());
    clone.setReportInferenceResults(this.getReportInferenceResults());
    clone.setParent(this.getParent());

    // Results
//    clone.setBundleNames(this.getBundleNames());
//    clone.setBundles(this.getBundles());
//    clone.setValid(this.getValid());
//    clone.setFailed(this.getFailed());
    return clone;
  }


}
class BundleKeySerializer extends JsonSerializer<String> {
  @Override
  public void serialize(String value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
    gen.writeFieldName("bundle");
  }
}
