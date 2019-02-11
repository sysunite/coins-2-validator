package com.sysunite.coinsweb.rdfutil;

import org.junit.Test;

import java.io.File;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author bastbijl, Sysunite 2017
 */
public class UtilsTest {
  @Test
  public void testFileFormat() {
    assertFalse(Utils.isRdfFile(new File("file.jpg")));
    assertFalse(Utils.isRdfFile(new File("file.txt")));
    assertTrue(Utils.isRdfFile(new File("file.ttl")));
    assertTrue(Utils.isRdfFile(new File("."+File.separator+"path"+File.separator+"file.rdf")));
  }
}
