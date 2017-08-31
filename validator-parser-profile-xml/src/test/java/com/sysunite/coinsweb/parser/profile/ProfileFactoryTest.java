package com.sysunite.coinsweb.parser.profile;

import com.sysunite.coinsweb.parser.profile.factory.ProfileFactory;
import com.sysunite.coinsweb.parser.profile.pojo.ProfileFile;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;


/**
 * @author bastbijl, Sysunite 2017
 */
public class ProfileFactoryTest {



  @Test
  public void testTemplate() throws IOException {



    InputStream file = getClass().getClassLoader().getResource("profile.lite-9.85-virtuoso.xml").openStream();
    ProfileFile profileFile = ProfileFile.parse(file);

    Map<String, Set<String>> map = ProfileFactory.inferencesOverVars(profileFile);
    System.out.println();
  }

}