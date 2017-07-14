package com.sysunite.coinsweb.steps;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sysunite.coinsweb.filemanager.ContainerFile;
import com.sysunite.coinsweb.graphset.ContainerGraphSet;
import com.sysunite.coinsweb.parser.config.pojo.ConfigPart;
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


  private String type = "FileSystemValidation";
  public String getType() {
    return type;
  }
  public void setType(String type) {
    this.type = type;
  }

  private String lookIn;
  public String getLookIn() {
    return lookIn;
  }
  public void setLookIn(String lookIn) {
    this.lookIn = lookIn;
  }

  public void checkConfig() {
    isNotNull(lookIn);
  }

  @Override
  public Map<String, Object> execute(ContainerFile container, ContainerGraphSet graphSet) {

    // Should be one repo file
    boolean oneRepoFile = container.getContentFiles().size() == 1;

    // Should be no sub folders in bim
    boolean noSubsInBim = true;
    for(String path : container.getContentFiles()) {
      noSubsInBim &= (Paths.get(path).getNameCount() == 1);
    }

    // Should be no orphan files
    boolean noOrphans = container.getOrphanFiles().isEmpty();

    // Should be able to satisfy all ontology imports from repository folder
    boolean allImportsImportable = true;

    ArrayList<String> availableGraphs = new ArrayList();
    for(String repoFilePath : container.getRepositoryFiles()) {
      availableGraphs.addAll(container.getRepositoryFileNamespaces(repoFilePath));
    }

    List<String> imports = new ArrayList();
    if(graphSet.hasContext(getLookIn())) {
      imports = graphSet.getImports(getLookIn());
      for (String namespace : imports) {

        boolean found = Utils.containsNamespace(namespace, availableGraphs);
        if(!found) {
          log.info("Namespace to import "+ namespace+ " was not found in "+String.join(", ", availableGraphs));
        }
        allImportsImportable &= found;
      }
    }

    boolean valid = oneRepoFile && noSubsInBim && noOrphans && allImportsImportable;
    if(valid) {
      log.info("\uD83E\uDD47 valid");
    } else {
      log.info("\uD83E\uDD48 invalid");
    }

    Map<String, Object> reportItems = new HashMap();

    reportItems.put("valid",                valid);
    reportItems.put("oneRepoFile",          oneRepoFile);
    reportItems.put("noSubsInBim",          noSubsInBim);
    reportItems.put("noOrphans",            noOrphans);
    reportItems.put("allImportsImportable", allImportsImportable);
    reportItems.put("imports",              imports);

    return reportItems;
  }



}
