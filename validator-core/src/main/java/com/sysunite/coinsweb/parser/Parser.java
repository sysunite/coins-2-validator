package com.sysunite.coinsweb.parser;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author bastbijl, Sysunite 2017
 */
public class Parser {

  public static void validate(String value, String... options) {

    RuntimeException error = new RuntimeException("The value "+value+" was not one of the allowed options: "+Arrays.toString(options));

    List<String> optionsList = Arrays.asList(options);
    if(value == null || value.isEmpty() || !optionsList.contains(value)) {
      throw error;
    }
  }

  public static void isNotEmpty(Object[] array) {

    RuntimeException error = new RuntimeException("The array is empty but should contain at least one element");

    if(array.length < 1) {
      throw error;
    }
  }

  public static void isNotNull(Object object) {

    RuntimeException error = new RuntimeException("The object should not be null");

    if(object == null) {
      throw error;
    }
  }

  public static void isNull(Object object) {

    RuntimeException error = new RuntimeException("The object should not be set");

    if(object != null) {
      throw error;
    }
  }

  public static void isFile(String value) {

    RuntimeException error = new RuntimeException("The value " + value + " is not a valid file");

    if(value == null || value.isEmpty()) {
      throw error;
    }

    File file = new File(value);
    if(!file.exists() || !file.isFile()) {
      throw error;
    }
  }

  public static void canCreateFile(String value) {

    RuntimeException error = new RuntimeException("The value " + value + " can not be interpreted as a output file");

    if(value == null || value.isEmpty()) {
      throw error;
    }

    File file = new File(value);
    try {
      if (!(file.getParentFile().exists() || file.getParentFile().mkdirs()) || !(file.createNewFile() || file.canWrite())) {
        throw error;
      }
    } catch(IOException e) {
      throw error;
    }
  }

  public static URL isUri(String value) {

    RuntimeException error = new RuntimeException("The value " + value + " is not a valid uri");

    if(value == null || value.isEmpty()) {
      throw error;
    }

    try {
      return new URL(value);
    } catch (MalformedURLException e) {
      throw error;
    }
  }

  public static void isResolvable(String value) {

    RuntimeException error = new RuntimeException("The value " + value + " is not resolvable online");

    URL u = isUri(value);

    HttpURLConnection connection = null;
    try {
      connection = (HttpURLConnection) u.openConnection();
      connection.setRequestMethod("HEAD");
      if(connection.getResponseCode() != 200) {
        throw error;
      }
    } catch (IOException e) {
      throw error;
    } finally {
      if (connection != null) {
        connection.disconnect();
      }
    }
  }

  public static String INDENT = "  ";
  public static String indentText(String body, int level) {
    String result = "";
    String indent = String.join("", Collections.nCopies(level, INDENT));
    String[] lines = body.split(System.lineSeparator());
    int cutoff = -1;
    boolean skipping = true;
    for(String line : lines) {
      if(skipping) {
        if(line.trim().isEmpty()) {
          continue;
        } else {
          skipping = false;
        }
      }
      int count = line.indexOf(line.trim());
      if(cutoff == -1) {
        cutoff = count;
      }
      count = Math.min(count, cutoff);
      result += indent + line.substring(count) + System.lineSeparator();
    }
    return result.substring(0, result.length()-1);
  }
}
