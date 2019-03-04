package com.sysunite.coinsweb.filemanager;

import java.nio.file.Path;
import java.security.DigestInputStream;
import java.util.ArrayList;
import java.util.Set;

/**
 * @author bastbijl, Sysunite 2017
 */
public interface ContainerFile {

  boolean  isCorruptZip();
  boolean  hasWrongSlashes();

  Set<String> getContentFiles();
  Set<String> getInvalidContentFiles();
  Set<String> getCorruptContentFiles();
  Set<String> getRepositoryFiles();
  Set<String> getInvalidRepositoryFiles();
  Set<String> getCorruptRepositoryFiles();
  Set<String> getWoaFiles();
  Set<String> getAttachmentFiles();
  Set<String> getOrphanFiles();

  DigestInputStream getFile(Path zipPath);
  DigestInputStream getContentFile(String filename);
  DigestInputStream getInvalidContentFile(String filename);
  DigestInputStream getCorruptContentFile(String filename);
  DigestInputStream getRepositoryFile(String filename);
  DigestInputStream getInvalidRepositoryFile(String filename);
  DigestInputStream getCorruptRepositoryFile(String filename);
  DigestInputStream getWoaFile(String filename);
  DigestInputStream getAttachmentFile(String filename);
  DigestInputStream getOrphanFile(String filename);

  ArrayList<String> getFileImports(Path zipPath);

  ArrayList<String> getContentFileNamespaces(String filename);
  ArrayList<String> getRepositoryFileNamespaces(String filename);

  Path toPath();
  Path getContentFilePath(String filename);
  Path getInvalidContentFilePath(String filename);
  Path getCorruptContentFilePath(String filename);
  Path getRepositoryFilePath(String filename);
  Path getInvalidRepositoryFilePath(String filename);
  Path getCorruptRepositoryFilePath(String filename);
  Path getWoaFilePath(String filename);
  Path getAttachmentFilePath(String filename);
  Path getOrphanFilePath(String filename);
}
