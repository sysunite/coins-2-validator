package com.sysunite.coinsweb.filemanager;

import com.sysunite.coinsweb.rdfutil.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

/**
 * @author bastbijl, Sysunite 2017
 */
public class VirtualContainerFileImpl implements ContainerFile {

  private static final Logger log = LoggerFactory.getLogger(VirtualContainerFileImpl.class);


  public VirtualContainerFileImpl() {

  }

  private static Path bimPath = Paths.get("bim");
  private static Path repositoryPath = Paths.get("repository");
  private static Path woaPath = Paths.get("woa");
  private static Path attachmentPath = Paths.get("doc");

  private HashMap<String, Path> contentFiles = new HashMap();
  private HashMap<String, Path> repositoryFiles = new HashMap();
  private HashMap<String, Path> woaFiles = new HashMap();
  private HashMap<String, Path> attachmentFiles = new HashMap();
  private HashMap<String, Path> orphanFiles = new HashMap();

  public Set<String> getContentFiles() {
    return contentFiles.keySet();
  }
  public Set<String> getRepositoryFiles() {
    return repositoryFiles.keySet();
  }
  public Set<String> getWoaFiles() {
    return woaFiles.keySet();
  }
  public Set<String> getAttachmentFiles() {
    return attachmentFiles.keySet();
  }
  public Set<String> getOrphanFiles() {
    return orphanFiles.keySet();
  }

  @Override
  public File getFile(Path zipPath) {
    return null;
  }

  public File getContentFile(String filename) {
    return getFile(contentFiles.get(filename));
  }
  public File getRepositoryFile(String filename) {
    return getFile(repositoryFiles.get(filename));
  }
  public File getWoaFile(String filename) {
    return getFile(woaFiles.get(filename));
  }
  public File getAttachmentFile(String filename) {
    return getFile(attachmentFiles.get(filename));
  }
  public File getOrphanFile(String filename) {
    return getFile(orphanFiles.get(filename));
  }

  HashMap<String, ArrayList<String>> repositoryFileNamespaces = new HashMap();
  public ArrayList<String> getRepositoryFileNamespaces(String filename) {
    if(!repositoryFileNamespaces.containsKey(filename)) {
      repositoryFileNamespaces.put(filename, Utils.namespacesForFile(getRepositoryFile(filename)));
    }
    return repositoryFileNamespaces.get(filename);
  }

  @Override
  public Path toPath() {
    return null;
  }

  public Path getContentFilePath(String filename) {
    return contentFiles.get(filename);
  }
  public Path getRepositoryFilePath(String filename) {
    return repositoryFiles.get(filename);
  }
  public Path getWoaFilePath(String filename) {
    return woaFiles.get(filename);
  }
  public Path getAttachmentFilePath(String filename) {
    return attachmentFiles.get(filename);
  }
  public Path getOrphanFilePath(String filename) {
    return orphanFiles.get(filename);
  }

  @Override
  public void scan() {

  }


}
