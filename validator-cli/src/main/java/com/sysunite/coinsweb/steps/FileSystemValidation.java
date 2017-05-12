package com.sysunite.coinsweb.steps;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.sysunite.coinsweb.filemanager.ContainerFile;
import com.sysunite.coinsweb.graphset.ContainerGraphSet;
import com.sysunite.coinsweb.graphset.GraphSetFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.sysunite.coinsweb.filemanager.ContainerFileImpl.namespacesForFile;


/**
 * @author bastbijl, Sysunite 2017
 */
@JsonIgnoreProperties({"type"})
public class FileSystemValidation implements ValidationStep {

  private static final Logger log = LoggerFactory.getLogger(FileSystemValidation.class);

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
      File repoFile = container.getRepositoryFile(repoFilePath);
      availableGraphs.addAll(namespacesForFile(repoFile));
    }

    ArrayList<String> imports = GraphSetFactory.imports(container.getContentFile(container.getContentFiles().iterator().next()));
    for(String namespace : imports) {
      allImportsImportable &= availableGraphs.contains(namespace);
    }

    boolean valid = oneRepoFile && noSubsInBim && noOrphans && allImportsImportable;

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
