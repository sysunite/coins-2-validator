package com.sysunite.coinsweb.filemanager;

import com.sysunite.coinsweb.parser.config.ConfigFile;
import com.sysunite.coinsweb.parser.config.Graph;
import com.sysunite.coinsweb.parser.config.Locator;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author bastbijl, Sysunite 2017
 */
public class FileFactory {

  private static final Logger log = LoggerFactory.getLogger(ConfigGenerator.class);

  public static InputStream toInputStream(Locator locator, ConfigFile configFile) {
    if(Locator.FILE.equals(locator.getType())) {
      try {
        File file;
        if(configFile != null) {
          file = configFile.resolve(locator.getPath()).toFile();
        } else {
          file =  new File(locator.getPath());
        }
        return new FileInputStream(file);
      } catch (IOException e) {
        log.error(e.getMessage(), e);
      }
    }
    if(Locator.ONLINE.equals(locator.getType())) {
      try {
        URL url = new URL(locator.getUri());
        return url.openStream();
      } catch (MalformedURLException e) {
        log.error(e.getMessage(), e);
      } catch (IOException e) {
        log.error(e.getMessage(), e);
      }
    }

    throw new RuntimeException("File could not be loaded.");
  }

  public static InputStream toInputStream(Graph graph, ContainerFile container, ConfigFile configFile) {
    if(Graph.FILE.equals(graph.getType())) {
      try {
        File file;
        if(configFile != null) {
          file = configFile.resolve(graph.getPath()).toFile();
        } else {
          file =  new File(graph.getPath());
        }
        return new FileInputStream(file);
      } catch (IOException e) {
        log.error(e.getMessage(), e);
      }
    } else if(Graph.ONLINE.equals(graph.getType())) {
      try {
        URL url = new URL(graph.getUri());
        return url.openStream();
      } catch (MalformedURLException e) {
        log.error(e.getMessage(), e);
      } catch (IOException e) {
        log.error(e.getMessage(), e);
      }
    } else if(Graph.CONTAINER.equals(graph.getType())) {
      try {

        File fromContainer = container.getFile(Paths.get(graph.getPath()));
        FileInputStream stream = new FileInputStream(fromContainer);
        return stream;
      } catch (IOException e) {
        log.error(e.getMessage(), e);
      }
    }
    throw new RuntimeException("File could not be loaded.");

  }

  public static String getFileHash(Graph graph, ContainerFile container) {
    if(Graph.FILE.equals(graph.getType())) {
      return getFileHash(Paths.get(graph.getPath()));
    } else if(Graph.ONLINE.equals(graph.getType())) {
      return getHash(graph.getUri());
    } else if(Graph.CONTAINER.equals(graph.getType())) {
      return getFileHash(container.getFile(Paths.get(graph.getPath())).toPath());
    }
    throw new RuntimeException("File could not be loaded.");
  }


  public final static int NUM_HASH_CHARS = 8;
  public static String getFileHash(Path zipPath) {
    try {
      MessageDigest md5 = MessageDigest.getInstance("md5");
      DigestInputStream dis = new DigestInputStream(new FileInputStream(zipPath.toFile()), md5);
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
