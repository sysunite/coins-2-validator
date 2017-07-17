package com.sysunite.coinsweb.filemanager;

import com.sysunite.coinsweb.parser.config.pojo.Container;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author bastbijl, Sysunite 2017
 */
public class VirtualContainerFileImpl implements ContainerFile {

  private static final Logger log = LoggerFactory.getLogger(VirtualContainerFileImpl.class);


  private static Path bimPath = Paths.get("bim");
  private static Path repositoryPath = Paths.get("repository");
  private static Path woaPath = Paths.get("woa");
  private static Path attachmentPath = Paths.get("doc");

  private HashMap<String, Path> contentFiles = new HashMap();
  private HashMap<String, Path> repositoryFiles = new HashMap();
  private HashMap<String, Path> woaFiles = new HashMap();
  private HashMap<String, Path> attachmentFiles = new HashMap();
  private HashMap<String, Path> orphanFiles = new HashMap();

  private Container containerConfig;


  public VirtualContainerFileImpl(Container containerConfig) {
    this.containerConfig = containerConfig;
  }


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
  public DeleteOnCloseFileInputStream getFile(Path zipPath) {
    return null;
  }

  @Override
  public DeleteOnCloseFileInputStream getContentFile(String filename) {
    return null;
  }

  @Override
  public DeleteOnCloseFileInputStream getRepositoryFile(String filename) {
    return null;
  }

  @Override
  public DeleteOnCloseFileInputStream getWoaFile(String filename) {
    return null;
  }

  @Override
  public DeleteOnCloseFileInputStream getAttachmentFile(String filename) {
    return null;
  }

  @Override
  public DeleteOnCloseFileInputStream getOrphanFile(String filename) {
    return null;
  }

  @Override
  public ArrayList<String> getRepositoryFileNamespaces(String filename) {
    return null;
  }

  @Override
  public Path toPath() {
    return null;
  }

  @Override
  public Path getContentFilePath(String filename) {
    return null;
  }

  @Override
  public Path getRepositoryFilePath(String filename) {
    return null;
  }

  @Override
  public Path getWoaFilePath(String filename) {
    return null;
  }

  @Override
  public Path getAttachmentFilePath(String filename) {
    return null;
  }

  @Override
  public Path getOrphanFilePath(String filename) {
    return null;
  }

  @Override
  public void scan() {

  }


  public void writeZip(Path containerFile) {
    log.info("Will create container file at "+containerFile.toString());

    byte[] buffer = new byte[1024];

    try {

      // Get the zip file content
      ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(containerFile.toFile()));


      for(String context : pendingContentFile.keySet()) {
        String zipPath = "bim/content.rdf";
        log.info("Adding to zip "+zipPath);
        ZipEntry ze = new ZipEntry(zipPath);
        zout.putNextEntry(ze);
        DeleteOnCloseFileInputStream inputStream = new DeleteOnCloseFileInputStream(pendingContentFile.get(context));
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1)
          zout.write(buffer, 0, bytesRead);
        zout.closeEntry();
        inputStream.close();
      }

      int count = 1;
      for(String context : pendingLibraryFiles.keySet()) {
        String zipPath = "bim/repository/library_"+(count++)+".rdf";
        log.info("Adding to zip "+zipPath);
        ZipEntry ze = new ZipEntry(zipPath);
        zout.putNextEntry(ze);
        DeleteOnCloseFileInputStream inputStream = new DeleteOnCloseFileInputStream(pendingLibraryFiles.get(context));
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1)
          zout.write(buffer, 0, bytesRead);
        zout.closeEntry();
        inputStream.close();
      }

      for(File attachmentFile : pendingAttachmentFiles) {
        String zipPath = "doc/"+attachmentFile.getName();
        log.info("Adding to zip "+zipPath);
        ZipEntry ze = new ZipEntry(zipPath);
        zout.putNextEntry(ze);
        FileInputStream inputStream = new FileInputStream(attachmentFile);
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1)
          zout.write(buffer, 0, bytesRead);
        zout.closeEntry();
        inputStream.close();
      }

      zout.close();

    } catch(IOException e) {
      log.error(e.getMessage(), e);
    }
  }

  public Container getConfig() {
    return containerConfig;
  }

  private HashMap<String, File> pendingContentFile = new HashMap();
  public void addContentFile(File file, String namespace) {
    pendingContentFile.put(namespace, file);
  }

  private HashMap<String, File> pendingLibraryFiles = new HashMap();
  public void addLibraryFile(File file, String namespace) {
    pendingLibraryFiles.put(namespace, file);
  }

  private List<File> pendingAttachmentFiles = new ArrayList();
  public void addAttachmentFile(File file) {
    pendingAttachmentFiles.add(file);
  }
}
