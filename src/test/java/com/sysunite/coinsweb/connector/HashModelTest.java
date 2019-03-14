package com.sysunite.coinsweb.connector;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.helpers.StatementCollector;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.math.BigInteger;
import java.nio.ByteBuffer;

/**
 * @author bastbijl, Sysunite 2017
 */
public class HashModelTest {
  Logger log = LoggerFactory.getLogger(HashModelTest.class);

  @Test
  public void test() {
    File a = new File(getClass().getResource("a.ttl").getFile());
    File b = new File(getClass().getResource("b.ttl").getFile());
    File c = new File(getClass().getResource("c.ttl").getFile());
    File d = new File(getClass().getResource("d.ttl").getFile());
    File d2 = new File(getClass().getResource("d2.ttl").getFile());
    File d3 = new File(getClass().getResource("d3.ttl").getFile());
    File e = new File(getClass().getResource("e.ttl").getFile());
    File f = new File(getClass().getResource("f.ttl").getFile());
    File schema = new File(getClass().getResource("schema.ttl").getFile());
    File shacl = new File(getClass().getResource("shacl.ttl").getFile());
    File sparql = new File(getClass().getResource("sparql.ttl").getFile());
    File voiv = new File(getClass().getResource("void.ttl").getFile());
    File otl = new File(getClass().getClassLoader().getResource("otl-2.1/otl-2.1.ttl").getFile());

    File nquad = new File(getClass().getResource("nquad.nq").getFile());
    File content = new File(getClass().getResource("content.rdf").getFile());

    log.info(StringUtils.leftPad(new BigInteger(1, getHash(a)).toString(16), 8, '0'));
    log.info(StringUtils.leftPad(new BigInteger(1, getHash(b)).toString(16), 8, '0'));
    log.info(StringUtils.leftPad(new BigInteger(1, getHash(c)).toString(16), 8, '0'));
    log.info(StringUtils.leftPad(new BigInteger(1, getHash(d)).toString(16), 8, '0'));
    log.info(StringUtils.leftPad(new BigInteger(1, getHash(d2)).toString(16), 8, '0'));
    log.info(StringUtils.leftPad(new BigInteger(1, getHash(d3)).toString(16), 8, '0'));
    log.info(StringUtils.leftPad(new BigInteger(1, getHash(e)).toString(16), 8, '0'));
    log.info(StringUtils.leftPad(new BigInteger(1, getHash(f)).toString(16), 8, '0'));
    log.info(StringUtils.leftPad(new BigInteger(1, getHash(schema)).toString(16), 8, '0'));
    log.info(StringUtils.leftPad(new BigInteger(1, getHash(shacl)).toString(16), 8, '0'));
    log.info(StringUtils.leftPad(new BigInteger(1, getHash(sparql)).toString(16), 8, '0'));
    log.info(StringUtils.leftPad(new BigInteger(1, getHash(voiv)).toString(16), 8, '0'));
    log.info(StringUtils.leftPad(new BigInteger(1, getHash(otl)).toString(16), 8, '0'));

    log.info(StringUtils.leftPad(new BigInteger(1, getHash(nquad)).toString(16), 8, '0'));
    log.info(StringUtils.leftPad(new BigInteger(1, getHash(content)).toString(16), 8, '0'));
  }

  public byte[] getHash(File file) {

    String backupNameSpace = "http://example.com/backup";

    try {

      RDFFormat format = Rdf4jUtil.interpretFormat(file);
      if(format == null) {
        throw new RuntimeException("Not able to determine format of file: " + file);
      }

      HashModel model = new HashModel();
      RDFParser rdfParser = Rio.createParser(format);
      rdfParser.setRDFHandler(new StatementCollector(model));
      rdfParser.parse(new BufferedInputStream(new FileInputStream(file)), backupNameSpace);

      byte[] result = ByteBuffer.allocate(4).putInt(model.hashCode()).array();

      return result;
    } catch (FileNotFoundException e) {
      log.error(e.getMessage(), e);
    } catch (IOException e) {
      log.error(e.getMessage(), e);
    }
    return null;
  }
}
