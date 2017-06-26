package com.sysunite.coinsweb.parser;

/**
 * @author bastbijl, Sysunite 2017
 */
public class Markdown {

  public static String parseLinksToHtml(String input) {
    if(input == null) {
      return null;
    }
    return input.replaceAll("\\[([^\\]]*)\\][\n\t\r ]*\\(http([^)]*)\\)", "<a href=\"http$2\">$1</a>");
  }
}
