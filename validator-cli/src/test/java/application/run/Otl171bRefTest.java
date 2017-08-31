package application.run;

import application.SimpleHttpServer;
import com.sysunite.coinsweb.cli.Application;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * @author bastbijl, Sysunite 2017
 */
public class Otl171bRefTest {

  Logger log = LoggerFactory.getLogger(Otl171bRefTest.class);

  static {
    File profile = new File("/Users/bastiaanbijl/Documents/Sysunite/GitHub/Sysunite/coins-2-validator/validator-cli/src/test/resources/profiles/profile.lite-9.85-generated.xml");
    SimpleHttpServer.serveFile(profile, "application/xml", 9877);
  }
  File file = new File(getClass().getClassLoader().getResource("general-9.85-virtuoso.yml").getFile());
  String userDir = file.getParent() + "/otl-1.7.1b-ref/";


  @Test
  public void run() {

    log.info("Read "+file.getPath());

    System.setProperty("user.dir", userDir);

    String[] args = {"run",
      file.getPath(),
      "-l",
//      "--yml-to-console",
      "VC00.ccr",
      "VC01.ccr",
      "VC02.ccr",
      "VC03.ccr",
      "VC04.ccr",
      "VC05.ccr",
      "VC05a.ccr",
      "VC06.ccr",
      "VC06a.ccr",
      "VC07.ccr",
      "VC08.ccr",
      "VC09.ccr",
      "VC10.ccr",
      "VC12.ccr",
      "VC13.ccr",
      "VC13B.ccr",
      "VC14_CardinalityCheck.ccr",
      "VC15_Codelist.ccr",
      "VC16.ccr",
      "VC17.ccr",
      "VC18.ccr",
      "VC19.ccr",
      "VC20.ccr",
      "VC21.ccr",
      "VC22.ccr",
      "VC23.ccr",
      "VC24.ccr",
      "VC25.ccr",
      "VC26.ccr",
      "VC27.ccr",
      "VC28.ccr",
      "VC29.ccr",
      "VC30.ccr",
      "VC31.ccr",
      "VC99.ccr",
    };

    Application.main(args);
  }




}
