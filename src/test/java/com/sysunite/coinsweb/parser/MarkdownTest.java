package com.sysunite.coinsweb.parser;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author bastbijl, Sysunite 2017
 */
public class MarkdownTest {

  @Test
  public void testLinkToHtml() {
    assertEquals("before <a href=\"http://to.com/sub/d#this\">Link word</a> after",
    Markdown.parseLinksToHtml("before [Link word](http://to.com/sub/d#this) after"));
    assertEquals("before <a href=\"https://to.com/sub/d#this\">Link word</a> after",
    Markdown.parseLinksToHtml("before [Link word] (https://to.com/sub/d#this) after"));
    assertEquals("before <a href=\"https://to.com/sub/d#this\">Link word</a> after",
    Markdown.parseLinksToHtml("before [Link word]\n(https://to.com/sub/d#this) after"));
  }

}
