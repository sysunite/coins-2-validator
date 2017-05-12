package com.sysunite.coinsweb.filemanager;

import java.io.File;
import java.nio.file.Path;
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

  File getContentFile(String filename);
  File getRepositoryFile(String filename);
  File getWoaFile(String filename);
  File getAttachmentFile(String filename);
  File getOrphanFile(String filename);

  Path getContentFilePath(String filename);
  Path getRepositoryFilePath(String filename);
  Path getWoaFilePath(String filename);
  Path getAttachmentFilePath(String filename);
  Path getOrphanFilePath(String filename);

  void scan();
}
