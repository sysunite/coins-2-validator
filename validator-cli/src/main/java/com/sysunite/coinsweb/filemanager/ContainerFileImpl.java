package com.sysunite.coinsweb.filemanager;

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
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @author bastbijl, Sysunite 2017
 */
public class ContainerFileImpl extends File implements ContainerFile {

  private static final Logger log = LoggerFactory.getLogger(ContainerFileImpl.class);

  private boolean scanned = false;

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
  private HashMap<String, Path> repositoryFiles = new HashMap();
  private HashMap<String, Path> woaFiles = new HashMap();
  private HashMap<String, Path> attachmentFiles = new HashMap();
  private HashMap<String, Path> orphanFiles = new HashMap();

  public Set<String> getContentFiles() {
    if(!scanned) scan();
    return contentFiles.keySet();
  }
  public Set<String> getRepositoryFiles() {
    if(!scanned) scan();
    return repositoryFiles.keySet();
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
  public DeleteOnCloseFileInputStream getRepositoryFile(String filename) {
    return getFile(repositoryFiles.get(filename));
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



  public DeleteOnCloseFileInputStream getFile(Path zipPath) {

    byte[] buffer = new byte[1024];

    try {

      // Get the zip file content
      ZipInputStream zis = new ZipInputStream(new FileInputStream(this));
      ZipEntry ze = zis.getNextEntry();
      Path zePath;

      while(ze != null) {
        zePath = Paths.get(FilenameUtils.separatorsToSystem(ze.getName()));

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

  public void scan() {

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

        // Skip file names that start with a dot
        Path zipPath = Paths.get(FilenameUtils.separatorsToSystem(ze.getName()));
        if(zipPath.getFileName().toString().startsWith(".")) {
          ze = zis.getNextEntry();
          continue;
        }

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
            contentFiles.put(inside.toString(), zipPath);

          // bim/repository
          } else {
            inside = repositoryPath.relativize(inside);
            repositoryFiles.put(inside.toString(), zipPath);

            // Do this to detect errors upfront
            getRepositoryFileNamespaces(inside.toString());
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
      throw new RuntimeException("Something went wrong scanning the container file");
    }

    scanned = true;
  }

}
