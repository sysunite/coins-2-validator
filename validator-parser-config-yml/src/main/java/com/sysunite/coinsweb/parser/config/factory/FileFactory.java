package com.sysunite.coinsweb.parser.config.factory;


import com.sysunite.coinsweb.filemanager.ContainerFile;
import com.sysunite.coinsweb.filemanager.DeleteOnCloseFileInputStream;
import com.sysunite.coinsweb.parser.config.pojo.ConfigFile;
import com.sysunite.coinsweb.parser.config.pojo.Locator;
import com.sysunite.coinsweb.parser.config.pojo.Source;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author bastbijl, Sysunite 2017
 */
public class FileFactory {

  private static final Logger log = LoggerFactory.getLogger(ConfigFactory.class);

  public static File toFile(Locator locator) {

    if(Locator.FILE.equals(locator.getType())) {
      ConfigFile configFile = locator.getParent();
      String path = locator.getPath();
      File file = new File(configFile.resolve(path).toString());
      if(!file.exists()) {
        throw new RuntimeException("Configured file not found: "+file.getPath());
      }
      return file;
    }
    if(Locator.ONLINE.equals(locator.getType())) {
      throw new RuntimeException("Please don't do this");
//      try {
//        File file = File.createTempFile(RandomStringUtils.random(8, true, true),".ccr");
//        file.deleteOnExit();
//        URL url = new URL(locator.getUri());
//        URLConnection connection = url.openConnection();
//        InputStream input = connection.getInputStream();
//        byte[] buffer = new byte[4096];
//        int n;
//
//        OutputStream output = new FileOutputStream(file);
//        while ((n = input.read(buffer)) != -1) {
//          output.write(buffer, 0, n);
//        }
//        output.close();
//
//        return file;
//      } catch (MalformedURLException e) {
//        log.error(e.getMessage(), e);
//      } catch (FileNotFoundException e) {
//        log.error(e.getMessage(), e);
//      } catch (IOException e) {
//        log.error(e.getMessage(), e);
//      }
    }
    throw new RuntimeException("The locator could not be transformed to a File");
  }

  public static InputStream toInputStream(Locator locator) {
    String triedReference = "null";

    if(Locator.FILE.equals(locator.getType())) {
      try {
        File file;
        if(locator.getParent() != null) {
          file = locator.getParent().resolve(locator.getPath()).toFile();
        } else {
          file =  new File(locator.getPath());
        }
        triedReference = file.toString();
        return new FileInputStream(file);
      } catch (IOException e) {}
    }
    if(Locator.ONLINE.equals(locator.getType())) {
      try {
        URL url = new URL(locator.getUri());
        triedReference = url.toString();
        return url.openStream();
      } catch (MalformedURLException e) {
      } catch (IOException e) {}
    }

    throw new RuntimeException("File could not be loaded "+triedReference);
  }

  public static InputStream toInputStream(Source source, ContainerFile container) {
    String triedReference = "null";
    if (Source.FILE.equals(source.getType())) {
      try {
        File file;
        if (source.getParent() != null) {
          file = source.getParent().resolve(source.getPath()).toFile();
        } else {
          file = new File(source.getPath());
        }
        triedReference = file.toString();
        return new FileInputStream(file);
      } catch (IOException e) {
      }
    } else if (Source.ONLINE.equals(source.getType())) {
      try {
        URL url = new URL(source.getUri());
        return url.openStream();
      } catch (MalformedURLException e) {
      } catch (IOException e) {
      }
    } else if (Source.CONTAINER.equals(source.getType())) {

      DeleteOnCloseFileInputStream stream = container.getFile(Paths.get(source.getPath()));
      triedReference = "in container: "+Paths.get(source.getPath());
      return stream;
    }
    throw new RuntimeException("File could not be loaded "+triedReference);
  }

  public static String getFileHash(Source source, ContainerFile container) {
    try {
      if (Source.FILE.equals(source.getType())) {
        return getFileHash(new DeleteOnCloseFileInputStream(Paths.get(source.getPath()).toFile()));
      } else if (Source.ONLINE.equals(source.getType())) {
        return getHash(source.getUri());
      } else if (Source.CONTAINER.equals(source.getType())) {
        return getFileHash(container.getFile(Paths.get(source.getPath())));
      }
    } catch (FileNotFoundException e) {}
    throw new RuntimeException("File could not be loaded.");
  }


  public final static int NUM_HASH_CHARS = 8;
  public static String getFileHash(FileInputStream inputStream) {
    try {
      MessageDigest md5 = MessageDigest.getInstance("md5");
      DigestInputStream dis = new DigestInputStream(inputStream, md5);
      byte buf[] = new byte[8 * 1024];
      while (dis.read(buf, 0, buf.length) > 0);
      dis.close();
      return StringUtils.leftPad(new BigInteger(1, md5.digest()).toString(16), 32, '0').substring(0, NUM_HASH_CHARS);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
      throw new RuntimeException("Failed calculating md5 hash", e);
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
      throw new RuntimeException("Failed calculating md5 hash", e);
    } catch (IOException e) {
      e.printStackTrace();
      throw new RuntimeException("Failed calculating md5 hash", e);
    }
  }
  public static String getHash(String payload) {
    try {
      MessageDigest md5 = MessageDigest.getInstance("md5");
      DigestInputStream dis = new DigestInputStream(new ByteArrayInputStream(payload.getBytes(StandardCharsets.UTF_8)), md5);
      byte buf[] = new byte[8 * 1024];
      while (dis.read(buf, 0, buf.length) > 0);
      dis.close();
      return StringUtils.leftPad(new BigInteger(1, md5.digest()).toString(16), 32, '0').substring(0, NUM_HASH_CHARS);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
      throw new RuntimeException("Failed calculating md5 hash", e);
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
      throw new RuntimeException("Failed calculating md5 hash", e);
    } catch (IOException e) {
      e.printStackTrace();
      throw new RuntimeException("Failed calculating md5 hash", e);
    }
  }
}