package com.sysunite.coinsweb.filemanager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Set;

/**
 * @author bastbijl, Sysunite 2017
 */
public class VirtualContainerFileImpl implements ContainerFile {

  private static final Logger log = LoggerFactory.getLogger(VirtualContainerFileImpl.class);


  public VirtualContainerFileImpl() {

  }


  @Override
  public Set<String> getContentFiles() {
    return null;
  }

  @Override
  public Set<String> getRepositoryFiles() {
    return null;
  }

  @Override
  public Set<String> getWoaFiles() {
    return null;
  }

  @Override
  public Set<String> getAttachmentFiles() {
    return null;
  }

  @Override
  public Set<String> getOrphanFiles() {
    return null;
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
}
