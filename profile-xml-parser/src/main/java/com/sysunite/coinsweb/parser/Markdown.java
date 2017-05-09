package com.sysunite.coinsweb.parser;

/**
 * @author bastbijl, Sysunite 2017
 */
public class Markdown {

  public static String parseLinksToHtml(String input) {
    return input.replaceAll("\\[([^\\]]*)\\][\n\t\r ]*\\(http([^)]*)\\)", "<a href=\"http$2\">$1</a>");
  }
}
