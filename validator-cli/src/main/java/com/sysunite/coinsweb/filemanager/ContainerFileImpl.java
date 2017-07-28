package com.sysunite.coinsweb.filemanager;

import com.sysunite.coinsweb.parser.config.factory.FileFactory;
import com.sysunite.coinsweb.parser.config.pojo.Container;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * @author bastbijl, Sysunite 2017
 */
public class ContainerFileImpl extends File implements ContainerFile {

  private static final Logger log = LoggerFactory.getLogger(ContainerFileImpl.class);

  private boolean scanned = false;
  private boolean wrongSlashes = false;
  private boolean corruptZip = false;

  private Container containerConfig;

  public ContainerFileImpl(Container containerConfig) {
    super(FileFactory.toFile(containerConfig).getPath());
    this.containerConfig = containerConfig;
  }

  public ContainerFileImpl(String pathname) {
    super(pathname);
  }

  public ContainerFileImpl(URI uri) {
    super(uri);
  }

  private static Path bimPath = Paths.get("bim");
  private static Path repositoryPath = Paths.get("repository");
  private static Path woaPath = Paths.get("woa");
  private static Path attachmentPath = Paths.get("doc");

  private HashMap<String, Path> contentFiles = new HashMap();
  private HashMap<String, Path> invalidContentFiles = new HashMap();
  private HashMap<String, Path> repositoryFiles = new HashMap();
  private HashMap<String, Path> invalidRepositoryFiles = new HashMap();
  private HashMap<String, Path> woaFiles = new HashMap();
  private HashMap<String, Path> attachmentFiles = new HashMap();
  private HashMap<String, Path> orphanFiles = new HashMap();

  public boolean isScanned() {
    return scanned;
  }
  public boolean isCorruptZip() {
    return corruptZip;
  }
  public boolean hasWrongSlashes() {
    if(!scanned) scan();
    return wrongSlashes;
  }

  public Set<String> getContentFiles() {
    if(!scanned) scan();
    return contentFiles.keySet();
  }
  public Set<String> getInvalidContentFiles() {
    if(!scanned) scan();
    return invalidContentFiles.keySet();
  }
  public Set<String> getRepositoryFiles() {
    if(!scanned) scan();
    return repositoryFiles.keySet();
  }
  public Set<String> getInvalidRepositoryFiles() {
    if(!scanned) scan();
    return invalidRepositoryFiles.keySet();
  }
  public Set<String> getWoaFiles() {
    if(!scanned) scan();
    return woaFiles.keySet();
  }
  public Set<String> getAttachmentFiles() {
    if(!scanned) scan();
    return attachmentFiles.keySet();
  }
  public Set<String> getOrphanFiles() {
    if(!scanned) scan();
    return orphanFiles.keySet();
  }

  public DeleteOnCloseFileInputStream getContentFile(String filename) {
    return getFile(contentFiles.get(filename));
  }
  public DeleteOnCloseFileInputStream getInvalidContentFile(String filename) {
    return getFile(invalidContentFiles.get(filename));
  }
  public DeleteOnCloseFileInputStream getRepositoryFile(String filename) {
    return getFile(repositoryFiles.get(filename));
  }
  public DeleteOnCloseFileInputStream getInvalidRepositoryFile(String filename) {
    return getFile(invalidRepositoryFiles.get(filename));
  }
  public DeleteOnCloseFileInputStream getWoaFile(String filename) {
    return getFile(woaFiles.get(filename));
  }
  public DeleteOnCloseFileInputStream getAttachmentFile(String filename) {
    return getFile(attachmentFiles.get(filename));
  }
  public DeleteOnCloseFileInputStream getOrphanFile(String filename) {
    return getFile(orphanFiles.get(filename));
  }

  HashMap<String, ArrayList<String>> contentFileNamespaces = new HashMap();
  public ArrayList<String> getContentFileNamespaces(String filename) {
    if(!contentFileNamespaces.containsKey(filename)) {
      contentFileNamespaces.put(filename, DescribeFactoryImpl.namespacesForFile(getContentFile(filename), filename));
    }
    return contentFileNamespaces.get(filename);
  }

  HashMap<String, ArrayList<String>> repositoryFileNamespaces = new HashMap();
  public ArrayList<String> getRepositoryFileNamespaces(String filename) {
    if(!repositoryFileNamespaces.containsKey(filename)) {
      repositoryFileNamespaces.put(filename, DescribeFactoryImpl.namespacesForFile(getRepositoryFile(filename), filename));
    }
    return repositoryFileNamespaces.get(filename);
  }

  public Path getContentFilePath(String filename) {
    return contentFiles.get(filename);
  }
  public Path getInvalidContentFilePath(String filename) {
    return invalidContentFiles.get(filename);
  }
  public Path getRepositoryFilePath(String filename) {
    return repositoryFiles.get(filename);
  }
  public Path getInvalidRepositoryFilePath(String filename) {
    return invalidRepositoryFiles.get(filename);
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



  public DeleteOnCloseFileInputStream getFile(Path zipPath) {

    byte[] buffer = new byte[1024];

    try {

      // Get the zip file content
      ZipInputStream zis = new ZipInputStream(new FileInputStream(this));
      ZipEntry ze = zis.getNextEntry();
      Path zePath;

      while(ze != null) {
        zePath = Paths.get(FilenameUtils.separatorsToUnix(ze.getName()));

        if(ze.isDirectory()) {
          ze = zis.getNextEntry();
          continue;
        }

        if(zePath.equals(zipPath)) {
          File file = File.createTempFile(RandomStringUtils.random(8, true, true), zipPath.getFileName().toString());
          FileOutputStream fos = new FileOutputStream(file);

          int len;
          while ((len = zis.read(buffer)) > 0) {
            fos.write(buffer, 0, len);
          }

          fos.close();
          zis.closeEntry();
          zis.close();

          return new DeleteOnCloseFileInputStream(file);
        }

        ze = zis.getNextEntry();
      }

      zis.closeEntry();
      zis.close();

    } catch(IOException e) {
      log.error(e.getMessage(), e);
    }
    throw new RuntimeException("File not found in container: " + zipPath);
  }

  private void scan() {

    if(!exists()) {
      throw new RuntimeException("Container file could not be found");
    }

    Path leadingPath = null;

    try {

      // Get the zip file content
      ZipInputStream zis = new ZipInputStream(new FileInputStream(this));
      ZipEntry ze = zis.getNextEntry();

      int logCount = 0;
      final int MAX_LOG_COUNT = 10;
      boolean noFolderSeenYet = true;
      while(ze != null) {

        // Skip directories
        if(ze.isDirectory()) {
          ze = zis.getNextEntry();
          continue;
        }

        Path zipPath = Paths.get(FilenameUtils.separatorsToUnix(ze.getName()));

        // Detect wrong slashes
        if(ze.getName().contains("\\")) {
          wrongSlashes = true;
        }

//        // Skip file names that start with a dot
//        if(zipPath.getFileName().toString().startsWith(".")) {
//          ze = zis.getNextEntry();
//          continue;
//        }

        Path normalizedPath = zipPath;
        if(leadingPath != null) {
          normalizedPath = leadingPath.relativize(normalizedPath);
        }

        if(++logCount < MAX_LOG_COUNT)
          log.info("Scan " + normalizedPath);
        else if(logCount == MAX_LOG_COUNT)
          log.info("Scan ...");


        // bim
        if(normalizedPath.startsWith(bimPath)) {
          Path inside = bimPath.relativize(normalizedPath);
          if(!inside.startsWith(repositoryPath)) {

            try {
              contentFiles.put(inside.toString(), zipPath);
              getContentFileNamespaces(inside.toString());
            } catch (Exception e) {
              contentFiles.remove(inside.toString());
              invalidContentFiles.put(inside.toString(), zipPath);
            }

          // bim/repository
          } else {
            inside = repositoryPath.relativize(inside);


            // Do this to detect errors upfront
            try {
              repositoryFiles.put(inside.toString(), zipPath);
              getRepositoryFileNamespaces(inside.toString());
            } catch (Exception e) {
              repositoryFiles.remove(inside.toString());
              invalidRepositoryFiles.put(inside.toString(), zipPath);
            }

          }

        // woa
        } else if(normalizedPath.startsWith(woaPath)) {
          Path inside = woaPath.relativize(normalizedPath);
          woaFiles.put(inside.toString(), zipPath);

        // doc
        } else if(normalizedPath.startsWith(attachmentPath)) {
          Path inside = attachmentPath.relativize(normalizedPath);
          attachmentFiles.put(inside.toString(), zipPath);

        // handle leading path
        } else {
          if(leadingPath == null && normalizedPath.getNameCount() > 1 && noFolderSeenYet) {
            leadingPath = normalizedPath.subpath(0, 1);
            continue;
          } else {
            orphanFiles.put(normalizedPath.toString(), zipPath);
          }
        }

        // Disable setting a leading folder
        if(zipPath.getNameCount() > 1) {
          noFolderSeenYet = false;
        }
        ze = zis.getNextEntry();
      }

      zis.closeEntry();
      zis.close();

    } catch(IOException e) {
      log.error(e.getMessage(), e);

      scanned = true;
      corruptZip = true;
      throw new RuntimeException("Something went wrong scanning the container file, concluding invalid zip");
    }

    scanned = true;
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
}
