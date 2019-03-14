package com.sysunite.coinsweb.filemanager;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author bastbijl, Sysunite 2017
 */
public class DescribeFactoryImplTest {
  Logger log = LoggerFactory.getLogger(DescribeFactoryImplTest.class);

  @Test
  public void testWildcard() {
    assertTrue(DescribeFactoryImpl.filterPath("bim/testdata_211.ttl", "bim/*.ttl"));
    assertTrue(DescribeFactoryImpl.filterPath("bim/testdata_211.ttl", "bim/*2*.ttl"));
    assertTrue(DescribeFactoryImpl.filterPath("bim/testdata_211.ttl", "bim/*"));
    assertTrue(DescribeFactoryImpl.filterPath("bim/testdata_211.ttl", "**"));
    assertFalse(DescribeFactoryImpl.filterPath("bim/testdata_211.ttl", "bim/*.docx"));
  }
}