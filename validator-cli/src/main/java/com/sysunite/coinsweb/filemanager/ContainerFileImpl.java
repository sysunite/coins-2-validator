package com.sysunite.coinsweb.filemanager;

import com.sysunite.coinsweb.connector.Connector;
import com.sysunite.coinsweb.connector.ConnectorException;
import com.sysunite.coinsweb.parser.config.factory.FileFactory;
import com.sysunite.coinsweb.parser.config.pojo.Container;
import com.sysunite.coinsweb.parser.config.pojo.Source;
import com.sysunite.coinsweb.rdfutil.Utils;
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
import java.security.DigestInputStream;
import java.util.*;
import java.util.zip.*;

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
    if(!scanned) scan();
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

  public DigestInputStream getContentFile(String filename) {
    return getFile(contentFiles.get(filename));
  }
  public DigestInputStream getInvalidContentFile(String filename) {
    return getFile(invalidContentFiles.get(filename));
  }
  public DigestInputStream getRepositoryFile(String filename) {
    return getFile(repositoryFiles.get(filename));
  }
  public DigestInputStream getInvalidRepositoryFile(String filename) {
    return getFile(invalidRepositoryFiles.get(filename));
  }
  public DigestInputStream getWoaFile(String filename) {
    return getFile(woaFiles.get(filename));
  }
  public DigestInputStream getAttachmentFile(String filename) {
    return getFile(attachmentFiles.get(filename));
  }
  public DigestInputStream getOrphanFile(String filename) {
    return getFile(orphanFiles.get(filename));
  }


  HashMap<String, ArrayList<String>> fileImports = new HashMap();
  public ArrayList<String> getFileImports(Path zipPath) {
    if(fileImports.containsKey(zipPath.getFileName().toString())) {
      return fileImports.get(zipPath.getFileName().toString());
    }
    return new ArrayList<>();
  }

  HashMap<String, ArrayList<String>> contentFileNamespaces = new HashMap();
  public ArrayList<String> getContentFileNamespaces(String filename) {
    if(!contentFileNamespaces.containsKey(filename)) {
      DescribeFactoryImpl.contextsInFile(getContentFile(filename), filename, contentFileNamespaces, fileImports);
    }
    return contentFileNamespaces.get(filename);
  }

  HashMap<String, ArrayList<String>> repositoryFileNamespaces = new HashMap();
  public ArrayList<String> getRepositoryFileNamespaces(String filename) {
    if(!repositoryFileNamespaces.containsKey(filename)) {
      DescribeFactoryImpl.contextsInFile(getRepositoryFile(filename), filename, repositoryFileNamespaces, fileImports);
    }
    return repositoryFileNamespaces.get(filename);
  }

  HashSet<String> collidingNamespaces;
  public HashSet<String> getCollidingNamespaces() {
    if(collidingNamespaces == null) {
      if(!scanned) scan();
      collidingNamespaces = new HashSet<>();
    }
    return collidingNamespaces;
  }
  HashMap<String, String> namespaceToFileMap;
  public HashMap<String, String> getNamespaceToFileMap() {
    if(namespaceToFileMap == null) {
      namespaceToFileMap = new HashMap<>();

      if(!scanned) scan();

      for(String fileName : contentFileNamespaces.keySet()) {
        for(String namespace : contentFileNamespaces.get(fileName)) {
          if(Utils.containsNamespace(namespace, namespaceToFileMap.keySet())) {
            collidingNamespaces.add(namespace);
          } else {
            namespaceToFileMap.put(namespace, fileName);
          }
        }
      }

      for(String fileName : repositoryFileNamespaces.keySet()) {
        for(String namespace : repositoryFileNamespaces.get(fileName)) {
          if(Utils.containsNamespace(namespace, namespaceToFileMap.keySet())) {
            collidingNamespaces.add(namespace);
          } else {
            namespaceToFileMap.put(namespace, fileName);
          }
        }
      }
    }
    return namespaceToFileMap;
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



  public DigestInputStream getFile(Path zipPath) {

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

          return DeleteOnCloseFileInputStream.getBufferedMd5(file);
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
      return;
    }

    Path leadingPath = null;

    try {

      // See if this finds parsing problems
      new ZipFile(this);


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
          throw new RuntimeException();
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

    } catch(ZipException e) {
      corruptZip = true;
    } catch(IOException e) {
      corruptZip = true;
    } catch(RuntimeException e) {
      corruptZip = true;
    }

    scanned = true;
  }

  private String pendingContentContext;
  public void setPendingContentContext(String pendingContentContext) {
    this.pendingContentContext = pendingContentContext;
  }

  private List<File> pendingAttachmentFiles = new ArrayList();
  public void addPendingAttachmentFile(File file) {
    pendingAttachmentFiles.add(file);
  }

  private List<File> pendingLibraryFiles = new ArrayList();
  public void addPendingLibraryFile(File file) {
    pendingLibraryFiles.add(file);
  }

  public ContainerFileImpl writeZip(Path containerFile, Connector connector) throws ConnectorException {
    log.info("Will create container file at "+containerFile.toString());

    List<Object> sources = connector.listPhiGraphs();

    byte[] buffer = new byte[1024];

    try {

      // Get the zip file content
      ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(containerFile.toFile()));

      Map<String, String> imports;
      {
        String zipPath = "bim/content.rdf";
        log.info("Adding to zip " + zipPath);
        ZipEntry ze = new ZipEntry(zipPath);
        zout.putNextEntry(ze);
        imports = connector.exportPhiGraph(pendingContentContext, zout);
        zout.closeEntry();
      }

      ArrayList<String> todoUploadContexts = new ArrayList<>();
      todoUploadContexts.addAll(imports.keySet());
      for(int i = 0; i < todoUploadContexts.size(); i++) {
        String context = todoUploadContexts.get(i);
        String original = imports.get(context);

        // Find fileName
        for(Object sourceObject : sources) {
          Source source = (Source) sourceObject;
          if(Utils.equalNamespace(source.getGraphname(),original)) {
            String fileName = source.getDefaultFileName();

            // Execute download and zip
            {
              String zipPath = "bim/repository/"+fileName;
              log.info("Adding to zip " + zipPath);
              log.info("Checking for existence");
              if (!new File(zipPath).exists()){
                log.warn("Could not find file " + zipPath);
                continue;
              }

              ZipEntry ze = new ZipEntry(zipPath);
              zout.putNextEntry(ze);
              Map<String, String> libraryImports = connector.exportPhiGraph(context, zout);
              zout.closeEntry();

              for(String libraryImport : libraryImports.keySet()) {
                if(!Utils.containsNamespace(libraryImport, todoUploadContexts)) {
                  todoUploadContexts.add(libraryImport);
                  imports.put(libraryImport, libraryImports.get(libraryImport));
                }
              }
            }

            break;
          }
        }

      }

      // Adding rdf files finished
      pendingContentContext = null;

      for(File libraryFile : pendingLibraryFiles) {
        String zipPath = "bim/repository/"+libraryFile.getName();
        log.info("Adding to zip "+zipPath);
        log.info("Checking for existence");
        if (!libraryFile.exists()){
          log.warn("Could not find file " + libraryFile.getAbsolutePath());
          continue;
        }

        ZipEntry ze = new ZipEntry(zipPath);
        zout.putNextEntry(ze);
        FileInputStream inputStream = new FileInputStream(libraryFile);
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1)
          zout.write(buffer, 0, bytesRead);
        zout.closeEntry();
        inputStream.close();
      }

      // Adding attachments finished
      pendingLibraryFiles = new ArrayList();

      for(File attachmentFile : pendingAttachmentFiles) {
        String zipPath = "doc/"+attachmentFile.getName();
        log.info("Adding to zip "+zipPath);
        log.info("Checking for existence");
        if (!attachmentFile.exists()){
          log.warn("Could not find file " + attachmentFile.getAbsolutePath());
          continue;
        }

        ZipEntry ze = new ZipEntry(zipPath);
        zout.putNextEntry(ze);
        FileInputStream inputStream = new FileInputStream(attachmentFile);
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1)
          zout.write(buffer, 0, bytesRead);
        zout.closeEntry();
        inputStream.close();
      }

      // Adding attachments finished
      pendingAttachmentFiles = new ArrayList();


      zout.close();

    } catch(IOException e) {
      log.error(e.getMessage(), e);
    }

    return new ContainerFileImpl(containerFile.toString());
  }

  public Container getConfig() {
    return containerConfig;
  }
}
