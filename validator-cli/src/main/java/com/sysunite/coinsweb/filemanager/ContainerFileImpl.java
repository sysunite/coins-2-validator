package com.sysunite.coinsweb.filemanager;

import com.sysunite.coinsweb.parser.config.ConfigFile;
import com.sysunite.coinsweb.parser.config.Locator;
import org.apache.commons.lang3.RandomStringUtils;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.helpers.StatementCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @author bastbijl, Sysunite 2017
 */
public class ContainerFileImpl extends File implements ContainerFile {

  private static final Logger log = LoggerFactory.getLogger(ContainerFileImpl.class);

  private boolean scanned = false;

  public static ContainerFileImpl parse(Locator locator, ConfigFile configFile) {

    if(Locator.FILE.equals(locator.getType())) {
      return new ContainerFileImpl(configFile.resolve(locator.getPath()).toString());
    }
    if(Locator.ONLINE.equals(locator.getType())) {
      try {
        File file = File.createTempFile(RandomStringUtils.random(8, true, true),".ccr");
        file.deleteOnExit();
        URL url = new URL(locator.getUri());
        URLConnection connection = url.openConnection();
        InputStream input = connection.getInputStream();
        byte[] buffer = new byte[4096];
        int n;

        OutputStream output = new FileOutputStream(file);
        while ((n = input.read(buffer)) != -1) {
          output.write(buffer, 0, n);
        }
        output.close();

        return new ContainerFileImpl(file.getPath());
      } catch (MalformedURLException e) {
        log.error(e.getMessage(), e);
      } catch (FileNotFoundException e) {
        log.error(e.getMessage(), e);
      } catch (IOException e) {
        log.error(e.getMessage(), e);
      }
    }
    throw new RuntimeException("Profile file could not be loaded.");
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

  public File getFile(Path zipPath) {

    byte[] buffer = new byte[1024];

    try {

      // Get the zip file content
      ZipInputStream zis = new ZipInputStream(new FileInputStream(this));
      ZipEntry ze = zis.getNextEntry();
      Path zePath = Paths.get(ze.getName());

      while(ze != null) {

        if(ze.isDirectory()) {
          ze = zis.getNextEntry();
          zePath = Paths.get(ze.getName());
          continue;
        }

        if(zePath.equals(zipPath)) {
          File file = File.createTempFile(RandomStringUtils.random(8, true, true), zipPath.getFileName().toString());
          file.deleteOnExit();
          FileOutputStream fos = new FileOutputStream(file);

          int len;
          while ((len = zis.read(buffer)) > 0) {
            fos.write(buffer, 0, len);
          }

          fos.close();
          zis.closeEntry();
          zis.close();


          return file;
        }

        ze = zis.getNextEntry();
        zePath = Paths.get(ze.getName());
      }

      zis.closeEntry();
      zis.close();

    } catch(IOException e) {
      log.error(e.getMessage(), e);
    }
    throw new RuntimeException("File not found in container: " + zipPath);
  }

  public void scan() {

    Path leadingPath = null;

    try {

      // Get the zip file content
      ZipInputStream zis = new ZipInputStream(new FileInputStream(this));
      ZipEntry ze = zis.getNextEntry();


      boolean noFolderSeenYet = true;
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

        log.info("scan "+normalizedPath);

        // bim
        if(normalizedPath.startsWith(bimPath)) {
          Path inside = bimPath.relativize(normalizedPath);
          if(!inside.startsWith(repositoryPath)) {
//            String filename = inside.getFileName().toString();
            contentFiles.put(inside.toString(), zipPath);

          // bim/repository
          } else {
            inside = repositoryPath.relativize(inside);
//            String filename = repositoryPath.relativize(inside).getFileName().toString();
            repositoryFiles.put(inside.toString(), zipPath);
          }

        // woa
        } else if(normalizedPath.startsWith(woaPath)) {
          Path inside = woaPath.relativize(normalizedPath);
//          String filename = woaPath.relativize(normalizedPath).getFileName().toString();
          woaFiles.put(inside.toString(), zipPath);

        // doc
        } else if(normalizedPath.startsWith(attachmentPath)) {
          Path inside = attachmentPath.relativize(normalizedPath);
//          String filename = attachmentPath.relativize(normalizedPath).getFileName().toString();
          attachmentFiles.put(inside.toString(), zipPath);

        // handle leading path
        } else {
          if(leadingPath == null && normalizedPath.getNameCount() > 1 && noFolderSeenYet) {
            leadingPath = normalizedPath.subpath(0, 1);
            continue;
          } else {
//            String filename = normalizedPath.getFileName().toString();
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
    }
    scanned = true;
  }



  public static ArrayList<String> namespacesForFile(File file) {

    ArrayList<String> namespaces = new ArrayList();

    log.info("Determine file type for file: "+file.toString());
    Optional<RDFFormat> format = Rio.getParserFormatForFileName(file.toString());
    if(!format.isPresent()) {
      throw new RuntimeException("Not able to determine format of file: " + file.getName());
    }
    Model model = new LinkedHashModel();
    RDFParser rdfParser = Rio.createParser(format.get());
    rdfParser.setRDFHandler(new StatementCollector(model));

    try {
      rdfParser.parse(new FileInputStream(file), "http://backup");
    } catch (Exception e) {
      throw new RuntimeException(e.getMessage());
    }

    // If there are contexts, use these
    for(Resource context : model.contexts()) {
      if(context != null) {
        namespaces.add(context.toString());
      }
    }

    // If no contexts, use the empty namespace
    if(namespaces.size() < 1) {
      Optional<Namespace> namespace = model.getNamespace("");
      if (namespace.isPresent()) {
        namespaces.add(namespace.get().getName());
      }
    }

    // If still no namespace
    if(namespaces.size() < 1) {
      throw new RuntimeException("No namespace found to represent this file.");
    }
    return namespaces;
  }
}
