package com.sysunite.coinsweb.filemanager;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @author bastbijl, Sysunite 2017
 */
public class ContainerFile extends File {

  private static final Logger log = Logger.getLogger(ContainerFile.class);

  public static String tempLocation = "/tmp";
  private boolean scanned = false;

  public ContainerFile(String pathname) {
    super(pathname);
  }

  public ContainerFile(URI uri) {
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

  private File getFile(Path zipPath) {

    byte[] buffer = new byte[1024];

    try {

      // Get the zip file content
      ZipInputStream zis = new ZipInputStream(new FileInputStream(this));
      ZipEntry ze = zis.getNextEntry();

      while(ze != null) {

        if(ze.isDirectory()) {
          ze = zis.getNextEntry();
          continue;
        }

        if(ze.getName().equals(zipPath.toString())) {
          File file = new File(tempLocation + "/" + RandomStringUtils.random(8, true, true) + "-" + zipPath.getFileName());
          FileOutputStream fos = new FileOutputStream(file);

          int len;
          while ((len = zis.read(buffer)) > 0) {
            fos.write(buffer, 0, len);
          }

          fos.close();
          zis.closeEntry();
          zis.close();

          file.deleteOnExit();
          return file;
        }

        ze = zis.getNextEntry();
      }

      zis.closeEntry();
      zis.close();

    } catch(IOException ex) {
      ex.printStackTrace();
    }
    throw new RuntimeException("File not found in container: " + zipPath);
  }

  public void scan() {

    Path leadingPath = null;

    try {

      // Get the zip file content
      ZipInputStream zis = new ZipInputStream(new FileInputStream(this));
      ZipEntry ze = zis.getNextEntry();

      while(ze != null) {

        // Skip directories
        if(ze.isDirectory()) {
          ze = zis.getNextEntry();
          continue;
        }

        // Skip file names that start with a dot
        Path zipPath = Paths.get(ze.getName());
        if(zipPath.getFileName().toString().startsWith(".")) {
          ze = zis.getNextEntry();
          continue;
        }

        Path normalizedPath = zipPath;
        if(leadingPath != null) {
          normalizedPath = leadingPath.relativize(normalizedPath);
        }

        // bim
        if(normalizedPath.startsWith(bimPath)) {
          Path inside = bimPath.relativize(normalizedPath);
          if(!inside.startsWith(repositoryPath)) {
            String filename = inside.getFileName().toString();
            contentFiles.put(filename, zipPath);

          // bim/repository
          } else {
            String filename = repositoryPath.relativize(inside).getFileName().toString();
            repositoryFiles.put(filename, zipPath);
          }

        // woa
        } else if(normalizedPath.startsWith(woaPath)) {

          String filename = woaPath.relativize(normalizedPath).getFileName().toString();
          woaFiles.put(filename, zipPath);

        // doc
        } else if(normalizedPath.startsWith(attachmentPath)) {

          String filename = attachmentPath.relativize(normalizedPath).getFileName().toString();
          attachmentFiles.put(filename, zipPath);

        // handle leading path
        } else {
          if(leadingPath == null) {
            leadingPath = normalizedPath.subpath(0, 1);
            continue;
          } else {
            leadingPath = null;
            log.warn("Was not able to categorize this file in the container: " + zipPath);
          }
        }

        ze = zis.getNextEntry();
      }

      zis.closeEntry();
      zis.close();

    } catch(IOException ex) {
      ex.printStackTrace();
    }
    scanned = true;
  }
}
