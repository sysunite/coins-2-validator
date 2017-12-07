package com.sysunite.coinsweb.steps;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.sysunite.coinsweb.filemanager.ContainerFile;
import com.sysunite.coinsweb.filemanager.ContainerFileImpl;
import com.sysunite.coinsweb.graphset.ContainerGraphSet;
import com.sysunite.coinsweb.parser.config.pojo.ConfigPart;
import com.sysunite.coinsweb.parser.config.pojo.GraphVarImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static com.sysunite.coinsweb.parser.Parser.isNotNull;
import static com.sysunite.coinsweb.rdfutil.Utils.containsNamespace;

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

  private Boolean oneBimFile;
  public Boolean getOneBimFile() {
    return oneBimFile;
  }
  public void setOneBimFile(Boolean oneBimFile) {
    this.oneBimFile = oneBimFile;
  }

  private Boolean noCorruptContentFile;
  public Boolean getNoCorruptContentFile() {
    return noCorruptContentFile;
  }
  public void setNoCorruptContentFile(Boolean noCorruptContentFile) {
    this.noCorruptContentFile = noCorruptContentFile;
  }

  private Boolean noWrongContentFile;
  public Boolean getNoWrongContentFile() {
    return noWrongContentFile;
  }
  public void setNoWrongContentFile(Boolean noWrongContentFile) {
    this.noWrongContentFile = noWrongContentFile;
  }

  private Boolean noCorruptRepositoryFile;
  public Boolean getNoCorruptRepositoryFile() {
    return noCorruptRepositoryFile;
  }
  public void setNoCorruptRepositoryFile(Boolean noCorruptRepositoryFile) {
    this.noCorruptRepositoryFile = noCorruptRepositoryFile;
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

  private Boolean noCollidingNamespaces;
  public Boolean getNoCollidingNamespaces() {
    return noCollidingNamespaces;
  }
  public void setNoCollidingNamespaces(Boolean noCollidingNamespaces) {
    this.noCollidingNamespaces = noCollidingNamespaces;
  }

  private Boolean isLoadableAsGraphSet;
  public Boolean isLoadableAsGraphSet() {
    return isLoadableAsGraphSet;
  }
  public void isLoadableAsGraphSet(Boolean isLoadableAsGraphSet) {
    this.isLoadableAsGraphSet = isLoadableAsGraphSet;
  }

  private Boolean allImportsImportable;
  public Boolean getAllImportsImportable() {
    return allImportsImportable;
  }
  public void setAllImportsImportable(Boolean allImportsImportable) {
    this.allImportsImportable = allImportsImportable;
  }

  private Boolean coreModelImported;
  public Boolean getCoreModelImported() {
    return coreModelImported;
  }
  public void setCoreModelImported(Boolean coreModelImported) {
    this.coreModelImported = coreModelImported;
  }

  private Boolean oneOntologyIndividual;
  public Boolean getOneOntologyIndividual() {
    return oneOntologyIndividual;
  }
  public void setOneOntologyIndividual(Boolean oneOntologyIndividual) {
    this.oneOntologyIndividual = oneOntologyIndividual;
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


    if(container.isScanned()) {
      log.warn("This ContainerFileImpl was already scanned, please let FileSystemValidation be the first to do this");
    }

    if(!container.exists() || !container.isFile()) {
      fileFound = false;
      failed = true;
      return;
    }
    fileFound = true;

    if(container.isCorruptZip()) {
      nonCorruptZip = false;
      failed = true;
      return;
    }
    nonCorruptZip = true;

    if(container.hasWrongSlashes()) {
      forwardSlashes = false;
      failed = true;
      return;
    }
    forwardSlashes = true;

    oneBimFile = (container.getContentFiles().size() + container.getCorruptContentFiles().size() + container.getInvalidContentFiles().size()) == 1;
    noWrongContentFile = container.getInvalidContentFiles().size() < 1;
    noCorruptContentFile = container.getCorruptContentFiles().size() < 1;
    noWrongRepositoryFile = container.getInvalidRepositoryFiles().size() < 1;
    noCorruptRepositoryFile = container.getCorruptRepositoryFiles().size() < 1;
    oneOntologyIndividual = container.getContentOntologiesCount() == 1;

    // Should be no sub folders in bim
    noSubsInBim = true;
    for (String path : container.getContentFiles()) {
      noSubsInBim &= (Paths.get(path).getNameCount() == 1);
    }

    // Should be no orphan files
    noOrphans = container.getOrphanFiles().isEmpty();

    noCollidingNamespaces = container.getCollidingNamespaces().isEmpty();

    // Should be able to satisfy all ontology imports from repository folder
    allImportsImportable = container.getInvalidImports().isEmpty();
    coreModelImported = containsNamespace("http://www.coinsweb.nl/cbim-2.0.rdf", container.getInvalidImports());
    unmatchedImports = container.getInvalidImports();



    graphSet.load();
    isLoadableAsGraphSet = !graphSet.loadingFailed();

    valid = fileFound && nonCorruptZip && forwardSlashes && oneBimFile && noWrongContentFile && noWrongRepositoryFile && noSubsInBim && noOrphans && noCollidingNamespaces && allImportsImportable && coreModelImported && isLoadableAsGraphSet;
    failed = !isLoadableAsGraphSet;



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

    return clone;
  }



}
