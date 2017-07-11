package com.sysunite.coinsweb.filemanager;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Set;

/**
 * @author bastbijl, Sysunite 2017
 */
public interface ContainerFile {

  Set<String> getContentFiles();
  Set<String> getRepositoryFiles();
  Set<String> getWoaFiles();
  Set<String> getAttachmentFiles();
  Set<String> getOrphanFiles();

  DeleteOnCloseFileInputStream getFile(Path zipPath);
  DeleteOnCloseFileInputStream getContentFile(String filename);
  DeleteOnCloseFileInputStream getRepositoryFile(String filename);
  DeleteOnCloseFileInputStream getWoaFile(String filename);
  DeleteOnCloseFileInputStream getAttachmentFile(String filename);
  DeleteOnCloseFileInputStream getOrphanFile(String filename);

  ArrayList<String> getRepositoryFileNamespaces(String filename);

  Path toPath();
  Path getContentFilePath(String filename);
  Path getRepositoryFilePath(String filename);
  Path getWoaFilePath(String filename);
  Path getAttachmentFilePath(String filename);
  Path getOrphanFilePath(String filename);

  void scan();
}
