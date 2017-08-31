package com.sysunite.coinsweb.steps;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.sysunite.coinsweb.filemanager.ContainerFile;
import com.sysunite.coinsweb.filemanager.ContainerFileImpl;
import com.sysunite.coinsweb.graphset.ContainerGraphSet;
import com.sysunite.coinsweb.parser.config.pojo.ConfigPart;
import com.sysunite.coinsweb.parser.config.pojo.GraphVarImpl;
import com.sysunite.coinsweb.rdfutil.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.sysunite.coinsweb.parser.Parser.isNotNull;

/**
 * @author bastbijl, Sysunite 2017
 */
@JsonInclude(Include.NON_NULL)
public class FileSystemValidation extends ConfigPart implements ValidationStep {

  private static final Logger log = LoggerFactory.getLogger(FileSystemValidation.class);

  public static final String REFERENCE = "FileSystemValidation";


  // Configuration items
  private String type = REFERENCE;
  public String getType() {
    return type;
  }
  public void setType(String type) {
    this.type = type;
  }

  private GraphVarImpl lookIn;
  public GraphVarImpl getLookIn() {
    return lookIn;
  }
  public void setLookIn(GraphVarImpl lookIn) {
    this.lookIn = lookIn;
  }


  // Result items
  private boolean failed = true;
  public boolean getFailed() {
    return failed;
  }
  public void setFailed(boolean failed) {
    this.failed = failed;
  }

  private boolean valid;
  public boolean getValid() {
    return valid;
  }
  public void setValid(boolean valid) {
    this.valid = valid;
  }

  private Boolean fileFound;
  public Boolean getFileFound() {
    return fileFound;
  }
  public void setFileFound(Boolean fileFound) {
    this.fileFound = fileFound;
  }

  private Boolean nonCorruptZip;
  public Boolean getNonCorruptZip() {
    return nonCorruptZip;
  }
  public void setNonCorruptZip(Boolean nonCorruptZip) {
    this.nonCorruptZip = nonCorruptZip;
  }

  private Boolean forwardSlashes;
  public Boolean getForwardSlashes() {
    return forwardSlashes;
  }
  public void setForwardSlashes(Boolean forwardSlashes) {
    this.forwardSlashes = forwardSlashes;
  }

  private Boolean oneRepoFile;
  public Boolean getOneRepoFile() {
    return oneRepoFile;
  }
  public void setOneRepoFile(Boolean oneRepoFile) {
    this.oneRepoFile = oneRepoFile;
  }

  private Boolean noWrongContentFile;
  public Boolean getNoWrongContentFile() {
    return noWrongContentFile;
  }
  public void setNoWrongContentFile(Boolean noWrongContentFile) {
    this.noWrongContentFile = noWrongContentFile;
  }

  private Boolean noWrongRepositoryFile;
  public Boolean getNoWrongRepositoryFile() {
    return noWrongRepositoryFile;
  }
  public void setNoWrongRepositoryFile(Boolean noWrongRepositoryFile) {
    this.noWrongRepositoryFile = noWrongRepositoryFile;
  }

  private Boolean noSubsInBim;
  public Boolean getNoSubsInBim() {
    return noSubsInBim;
  }
  public void setNoSubsInBim(Boolean noSubsInBim) {
    this.noSubsInBim = noSubsInBim;
  }

  private Boolean noOrphans;
  public Boolean getNoOrphans() {
    return noOrphans;
  }
  public void setNoOrphans(Boolean noOrphans) {
    this.noOrphans = noOrphans;
  }

  private Boolean allImportsImportable;
  public Boolean getAllImportsImportable() {
    return allImportsImportable;
  }
  public void setAllImportsImportable(Boolean allImportsImportable) {
    this.allImportsImportable = allImportsImportable;
  }

  @JsonInclude(Include.NON_EMPTY)
  private List<String> imports = new ArrayList();
  public List<String> getImports() {
    return imports;
  }
  public void setImports(List<String> imports) {
    this.imports = imports;
  }

  @JsonInclude(Include.NON_EMPTY)
  private List<String> unmatchedImports = new ArrayList();
  public List<String> getUnmatchedImports() {
    return unmatchedImports;
  }
  public void setUnmatchedImports(List<String> unmatchedImports) {
    this.unmatchedImports = unmatchedImports;
  }

  public void checkConfig() {
    isNotNull(lookIn);
  }

  @Override
  public void execute(ContainerFile containerCandidate, ContainerGraphSet graphSet) {

    if(!(containerCandidate instanceof ContainerFileImpl)) {
      throw new RuntimeException("Running the FileSystemValidation step does not make sense for a non-ContainerFileImpl container");
    }

    ContainerFileImpl container = (ContainerFileImpl) containerCandidate;

    try {

      if(container.isScanned()) {
        log.warn("This ContainerFileImpl was already scanned, please let FileSystemValidation be the first to do this");
      }

      if(!container.exists() || !container.isFile()) {
        fileFound = false;
        return;
      }

      fileFound = true;
      nonCorruptZip = !container.isCorruptZip();
      forwardSlashes = !container.hasWrongSlashes();
      oneRepoFile = container.getContentFiles().size() == 1;
      noWrongContentFile = container.getInvalidContentFiles().size() < 1;
      noWrongRepositoryFile = container.getInvalidRepositoryFiles().size() < 1;

      // Should be no sub folders in bim
      noSubsInBim = true;
      for (String path : container.getContentFiles()) {
        noSubsInBim &= (Paths.get(path).getNameCount() == 1);
      }

      // Should be no orphan files
      noOrphans = container.getOrphanFiles().isEmpty();

      // Should be able to satisfy all ontology imports from repository folder
      boolean allImportsImportable = true;
      ArrayList<String> availableGraphs = new ArrayList();
      for (String repoFilePath : container.getRepositoryFiles()) {
        availableGraphs.addAll(container.getRepositoryFileNamespaces(repoFilePath));
      }

      if (graphSet.hasContext(getLookIn())) {
        Map<String, String> imports = graphSet.getImports(getLookIn());
        for (String storeContext : imports.keySet()) {

          String originalNamespace = imports.get(storeContext);

          log.info("Found import in content rdf-file: "+originalNamespace);

          boolean found = Utils.containsNamespace(originalNamespace, availableGraphs);
          this.imports.add(originalNamespace);
          if (!found) {
            log.info("Namespace to import " + originalNamespace + " was not found in " + String.join(", ", availableGraphs));
            unmatchedImports.add(originalNamespace);
          }
          allImportsImportable &= found;
        }
      }
      setAllImportsImportable(allImportsImportable);

      valid = fileFound && nonCorruptZip && forwardSlashes && oneRepoFile && noWrongContentFile && noWrongRepositoryFile && noSubsInBim && noOrphans && allImportsImportable;
      failed = false;

    } catch (RuntimeException e) {
      log.warn("Executing failed validationStep of type '"+getType()+"': "+e.getMessage(), e);
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
  public FileSystemValidation clone() {
    FileSystemValidation clone = new FileSystemValidation();

    // Configuration
    clone.setType(this.getType());
    clone.setLookIn(this.getLookIn());
    clone.setParent(this.getParent());

    // Results
//    clone.setFileFound(this.getFileFound());
//    clone.setNonCorruptZip(this.getNonCorruptZip());
//    clone.setForwardSlashes(this.getForwardSlashes());
//    clone.setNoWrongContentFile(this.getNoWrongContentFile());
//    clone.setNoWrongRepositoryFile(this.getNoWrongRepositoryFile());
//    clone.setOneRepoFile(this.getOneRepoFile());
//    clone.setNoSubsInBim(this.getNoSubsInBim());
//    clone.setNoOrphans(this.getNoOrphans());
//    clone.setAllImportsImportable(this.getAllImportsImportable());
//    clone.setValid(this.getValid());
//    clone.setFailed(this.getFailed());
    return clone;
  }



}
