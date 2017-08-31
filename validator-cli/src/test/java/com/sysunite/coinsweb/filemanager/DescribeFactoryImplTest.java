package com.sysunite.coinsweb.filemanager;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;


/**
 * @author bastbijl, Sysunite 2017
 */
public class DescribeFactoryImplTest {

  Logger log = LoggerFactory.getLogger(DescribeFactoryImplTest.class);

  @Test
  public void test() {
    File dataroom = new File(getClass().getClassLoader().getResource("dataroom-1.43/bim/Dataroom-1.3-coins2-otl-2.1.ttl").getFile());
    try {
      ArrayList<String> namespaces = new ArrayList<>();
      ArrayList<String> imports = new ArrayList<>();
      DescribeFactoryImpl.namespacesForFile(new FileInputStream(dataroom), dataroom.getName(), namespaces, imports);
      for(String item : namespaces) {
        log.info(item);
      }
      assert(namespaces.size() == 1);
      assert(namespaces.contains("http://dataroom.otl.rws.nl/otl171#"));

    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }



    File nquad = new File(getClass().getClassLoader().getResource("nquad.nq").getFile());
    try {
      ArrayList<String> namespaces = new ArrayList<>();
      ArrayList<String> imports = new ArrayList<>();
      DescribeFactoryImpl.namespacesForFile(new FileInputStream(nquad), nquad.getName(), namespaces, imports);
      for(String item : namespaces) {
        log.info(item);
      }
      assert(namespaces.size() == 2);
      assert(namespaces.contains("http://example.org/graphs/spiderman"));
      assert(namespaces.contains("http://example.org/graphs/superman"));

    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }
}