package com.sysunite.coinsweb.filemanager;

import com.sysunite.coinsweb.parser.config.ConfigFile;
import com.sysunite.coinsweb.parser.config.Locator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

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
          file = configFile.resolve(locator).toFile();
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

    throw new RuntimeException("Profile file could not be loaded.");
  }
}
