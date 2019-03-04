package com.sysunite.coinsweb.connector;

import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.helpers.StatementCollector;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.Set;

/**
 * @author bastbijl, Sysunite 2017
 */
public class NullModelTest {
  Logger log = LoggerFactory.getLogger(NullModelTest.class);

  @Test
  public void test() {
    File a = new File(getClass().getClassLoader().getResource("com.sysunite.coinsweb/connector/a.ttl").getFile());
    File b = new File(getClass().getClassLoader().getResource("com.sysunite.coinsweb/connector/b.ttl").getFile());
    File c = new File(getClass().getClassLoader().getResource("com.sysunite.coinsweb/connector/c.ttl").getFile());
    File d = new File(getClass().getClassLoader().getResource("com.sysunite.coinsweb/connector/d.ttl").getFile());
    File e = new File(getClass().getClassLoader().getResource("com.sysunite.coinsweb/connector/e.ttl").getFile());
    File f = new File(getClass().getClassLoader().getResource("com.sysunite.coinsweb/connector/f.ttl").getFile());
    File schema = new File(getClass().getClassLoader().getResource("com.sysunite.coinsweb/connector/schema.ttl").getFile());
    File shacl = new File(getClass().getClassLoader().getResource("com.sysunite.coinsweb/connector/shacl.ttl").getFile());
    File sparql = new File(getClass().getClassLoader().getResource("com.sysunite.coinsweb/connector/sparql.ttl").getFile());
    File voiv = new File(getClass().getClassLoader().getResource("com.sysunite.coinsweb/connector/void.ttl").getFile());
    File otl = new File(getClass().getClassLoader().getResource("otl-2.1/otl-2.1.ttl").getFile());

    File nquad = new File(getClass().getClassLoader().getResource("com.sysunite.coinsweb/connector/nquad.nq").getFile());
    File content = new File(getClass().getClassLoader().getResource("com.sysunite.coinsweb/connector/content.rdf").getFile());

    read(a);
    read(b);
    read(c);
    read(d);
    read(e);
    read(f);
    read(schema);
    read(shacl);
    read(sparql);
    read(voiv);
    read(otl);

    read(nquad);
    read(content);
  }

  public void read(File file) {

    String backupNameSpace = "http://example.com/backup";

    try {

      RDFFormat format = Rdf4jUtil.interpretFormat(file);
      if(format == null) {
        throw new RuntimeException("Not able to determine format of file: " + file);
      }

      NullModel model = new NullModel();
      RDFParser rdfParser = Rio.createParser(format);
      rdfParser.setRDFHandler(new StatementCollector(model));
      rdfParser.parse(new BufferedInputStream(new FileInputStream(file)), backupNameSpace);

      ArrayList<String> imports = model.getImports();
      Set<Resource> contexts = model.contexts();
      Set<Namespace> namespaces = model.getNamespaces();

      return;

    } catch (FileNotFoundException e) {
      log.error(e.getMessage(), e);
    } catch (IOException e) {
      log.error(e.getMessage(), e);
    }
    return;
  }

}
