package com.sysunite.coinsweb.parser.config.factory;


import com.sysunite.coinsweb.filemanager.ContainerFile;
import com.sysunite.coinsweb.parser.config.pojo.ConfigFile;
import com.sysunite.coinsweb.parser.config.pojo.Container;
import com.sysunite.coinsweb.parser.config.pojo.Locator;
import com.sysunite.coinsweb.parser.config.pojo.Source;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.math.BigInteger;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

/**
 * @author bastbijl, Sysunite 2017
 */
public class FileFactory {

  private static final Logger log = LoggerFactory.getLogger(ConfigFactory.class);

  public static File toFile(Container containerConfig) {
    if(containerConfig.isVirtual()) {
      try {
        return File.createTempFile("container", ".rdf");
      } catch (IOException e) {
        log.error(e.getMessage(), e);
        throw new RuntimeException();
      }
    } else {
      return toFile(containerConfig.getLocation());
    }
  }

  public static File toFile(Locator locator) {

    if(Locator.FILE.equals(locator.getType())) {
      ConfigFile configFile = locator.getParent();
      String path = locator.getPath();
      File file = new File(configFile.resolve(path).toString());
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
    String triedReference = "error interpreting locator";

    if(Locator.FILE.equals(locator.getType())) {
      try {
        File file;
        if(locator.getParent() != null) {
          triedReference = locator.getParent().resolve(locator.getPath()).toString();
          file = locator.getParent().resolve(locator.getPath()).toFile();
        } else {
          triedReference = locator.getPath();
          file =  new File(locator.getPath());
        }
        return new FileInputStream(file);
      } catch (Exception e) {}
    }
    if(Locator.ONLINE.equals(locator.getType())) {
      try {
        URL url = new URL(locator.getUri());
        triedReference = url.toString();
        return url.openStream();
      } catch (Exception e) {}
    }

    throw new RuntimeException("Locator of type "+locator.getType()+" could not be read as inputStream: "+triedReference);
  }

  public static DigestInputStream toInputStream(Source source, ContainerFile container) {
    String triedReference = "error interpreting source";
    if (Source.FILE.equals(source.getType())) {
      try {
        File file;
        if (source.getParent() != null) {
          triedReference = source.getParent().resolve(source.getPath()).toString();
          file = source.getParent().resolve(source.getPath()).toFile();
        } else {
          triedReference = source.getPath();
          file = new File(source.getPath());
        }
        return new DigestInputStream(new BufferedInputStream(new FileInputStream(file)), MessageDigest.getInstance("md5"));
      } catch (Exception e) {
      }
    } else if (Source.ONLINE.equals(source.getType())) {
      try {
        URL url = new URL(source.getUri());
        return new DigestInputStream(new BufferedInputStream(url.openStream()), MessageDigest.getInstance("md5"));
      } catch (Exception e) {
      }
    } else if (Source.CONTAINER.equals(source.getType())) {

      triedReference = "in container: "+Paths.get(source.getPath());
      return container.getFile(Paths.get(source.getPath()));
    }
    throw new RuntimeException("Source of type "+source.getType()+" could not be read as inputStream: "+triedReference);
  }

  public static ArrayList<String> getImports(Source source, ContainerFile container) {
    String triedReference = "error interpreting source";
    if (Source.FILE.equals(source.getType())) {
      throw new RuntimeException("Not implemented");
    } else if (Source.ONLINE.equals(source.getType())) {
      throw new RuntimeException("Not implemented");
    } else if (Source.CONTAINER.equals(source.getType())) {

      triedReference = "in container: "+Paths.get(source.getPath());
      return container.getFileImports(Paths.get(source.getPath()));
    }
    throw new RuntimeException("Source of type "+source.getType()+" could not be read as inputStream: "+triedReference);
  }




  public static void calculateAndSetHash(Source source, ContainerFile container) {
    if(source.getHash() != null) {
      return;
    }
    DigestInputStream inputStream = FileFactory.toInputStream(source, container);
    try {
      while (inputStream.read() != -1);
    } catch (IOException e) {
      log.error(e.getMessage(), e);
    }
    String hash = FileFactory.getFileHash(inputStream);
    source.setHash(hash);
  }

  public static String getFileHash(DigestInputStream dis) {
    return StringUtils.leftPad(new BigInteger(1, dis.getMessageDigest().digest()).toString(16), 32, '0');
  }
  public static String getHash(String payload) {
    try {
      MessageDigest md5 = MessageDigest.getInstance("md5");
      DigestInputStream dis = new DigestInputStream(new ByteArrayInputStream(payload.getBytes(StandardCharsets.UTF_8)), md5);
      byte buf[] = new byte[8 * 1024];
      while (dis.read(buf, 0, buf.length) > 0);
      dis.close();
      return StringUtils.leftPad(new BigInteger(1, md5.digest()).toString(16), 32, '0');
    } catch (FileNotFoundException e) {
      log.error(e.getMessage(), e);;
      throw new RuntimeException("Failed calculating md5 hash", e);
    } catch (NoSuchAlgorithmException e) {
      log.error(e.getMessage(), e);;
      throw new RuntimeException("Failed calculating md5 hash", e);
    } catch (IOException e) {
      log.error(e.getMessage(), e);;
      throw new RuntimeException("Failed calculating md5 hash", e);
    }
  }
}
