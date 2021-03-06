package com.sysunite.coinsweb.graphset;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.sysunite.coinsweb.parser.profile.pojo.ProfileFile;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

/**
 * @author bastbijl, Sysunite 2017
 */
public class ContainerGraphSetFactoryTest {
  Logger log = LoggerFactory.getLogger(ContainerGraphSetFactoryTest.class);

  @Test
  public void test() throws IOException {
    XmlMapper objectMapper = new XmlMapper();

    InputStream file = getClass().getClassLoader().getResource("profile.lite-9.85.xml").openStream();
    ProfileFile profileFile = objectMapper.readValue(file, ProfileFile.class);
    Set<GraphVar> set = QueryFactory.usedVars(profileFile);

    for(GraphVar var : set) {
      log.info(var.toString());
    }
  }
}
