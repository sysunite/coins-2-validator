package com.sysunite.coinsweb.filemanager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * copied from https://stackoverflow.com/questions/4693968/is-there-an-existing-fileinputstream-delete-on-close
 */
public class DeleteOnCloseFileInputStream extends FileInputStream {

  static Logger log = LoggerFactory.getLogger(DeleteOnCloseFileInputStream.class);

  private File file;
  public DeleteOnCloseFileInputStream(String name) throws FileNotFoundException {
    this(new File(name));
  }
  public DeleteOnCloseFileInputStream(File file) throws FileNotFoundException{
    super(file);
    this.file = file;
  }

  public void close() throws IOException {
    try {
      super.close();
    } finally {
      if(file != null) {
        file.delete();
        file = null;
      }
    }
  }

  public static BufferedInputStream getBuffered(String name) throws FileNotFoundException {
    return new BufferedInputStream(new DeleteOnCloseFileInputStream(name));
  }
  public static BufferedInputStream getBuffered(File file) throws FileNotFoundException {
    return new BufferedInputStream(new DeleteOnCloseFileInputStream(file));
  }

  public static DigestInputStream getBufferedMd5(String name) throws FileNotFoundException {
    try {
      return new DigestInputStream(new BufferedInputStream(new DeleteOnCloseFileInputStream(name)), MessageDigest.getInstance("md5"));
    } catch (NoSuchAlgorithmException e) {
      log.error(e.getMessage(), e);;
    }
    return null;
  }
  public static DigestInputStream getBufferedMd5(File file) throws FileNotFoundException {
    try {
      return new DigestInputStream(new BufferedInputStream(new DeleteOnCloseFileInputStream(file)), MessageDigest.getInstance("md5"));
    } catch (NoSuchAlgorithmException e) {
      log.error(e.getMessage(), e);;
      return null;
    }
  }
}