package com.sysunite.coinsweb.steps;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sysunite.coinsweb.filemanager.ContainerFile;
import com.sysunite.coinsweb.graphset.ContainerGraphSet;
import com.sysunite.coinsweb.parser.config.pojo.ConfigPart;
import com.sysunite.coinsweb.parser.config.pojo.GraphVarImpl;
import com.sysunite.coinsweb.rdfutil.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.sysunite.coinsweb.parser.Parser.isNotNull;

/**
 * @author bastbijl, Sysunite 2017
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
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

  private boolean valid = false;
  public boolean getValid() {
    return valid;
  }

  private boolean oneRepoFile = false;
  public boolean getOneRepoFile() {
    return oneRepoFile;
  }

  private boolean noSubsInBim = false;
  public boolean getNoSubsInBim() {
    return noSubsInBim;
  }

  private boolean noOrphans = false;
  public boolean getNoOrphans() {
    return noOrphans;
  }

  private boolean allImportsImportable = false;
  public boolean getAllImportsImportable() {
    return allImportsImportable;
  }

  private List<String> imports;
  public List<String> getImports() {
    return imports;
  }

  public void checkConfig() {
    isNotNull(lookIn);
  }

  @Override
  public Map<String, Object> execute(ContainerFile container, ContainerGraphSet graphSet) {

    try {

      // Should be one repo file
      oneRepoFile = container.getContentFiles().size() == 1;

      // Should be no sub folders in bim
      noSubsInBim = true;
      for (String path : container.getContentFiles()) {
        noSubsInBim &= (Paths.get(path).getNameCount() == 1);
      }

      // Should be no orphan files
      noOrphans = container.getOrphanFiles().isEmpty();

      // Should be able to satisfy all ontology imports from repository folder
      allImportsImportable = true;

      ArrayList<String> availableGraphs = new ArrayList();
      for (String repoFilePath : container.getRepositoryFiles()) {
        availableGraphs.addAll(container.getRepositoryFileNamespaces(repoFilePath));
      }

      imports = new ArrayList();
      if (graphSet.hasContext(getLookIn())) {
        imports = graphSet.getImports(getLookIn());
        for (String namespace : imports) {

          boolean found = Utils.containsNamespace(namespace, availableGraphs);
          if (!found) {
            log.info("Namespace to import " + namespace + " was not found in " + String.join(", ", availableGraphs));
          }
          allImportsImportable &= found;
        }
      }

      valid = oneRepoFile && noSubsInBim && noOrphans && allImportsImportable;
      failed = false;

    } catch (RuntimeException e) {
      log.warn("Executing failed validationStep of type "+getType());
      log.warn(e.getMessage());
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

    Map<String, Object> reportItems = new HashMap();

    reportItems.put("failed",               getFailed());
    reportItems.put("valid",                getValid());
    reportItems.put("oneRepoFile",          getOneRepoFile());
    reportItems.put("noSubsInBim",          getNoSubsInBim());
    reportItems.put("noOrphans",            getNoOrphans());
    reportItems.put("allImportsImportable", getAllImportsImportable());
    reportItems.put("imports",              getImports());

    return reportItems;
  }



}
