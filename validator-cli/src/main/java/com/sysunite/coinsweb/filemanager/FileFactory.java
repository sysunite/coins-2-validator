package com.sysunite.coinsweb.filemanager;

import com.sysunite.coinsweb.parser.config.ConfigFile;
import com.sysunite.coinsweb.parser.config.Locator;

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
        e.printStackTrace();
      }
    }
    if(Locator.ONLINE.equals(locator.getType())) {
      try {
        URL url = new URL(locator.getUri());
        return url.openStream();
      } catch (MalformedURLException e) {
        e.printStackTrace();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    throw new RuntimeException("Profile file could not be loaded.");
  }
}
