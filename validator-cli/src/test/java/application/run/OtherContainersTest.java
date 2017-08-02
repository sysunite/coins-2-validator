package application.run;

import com.sysunite.coinsweb.cli.Application;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import application.*;

import java.io.File;

/**
 * @author bastbijl, Sysunite 2017
 */
public class OtherContainersTest {

  Logger log = LoggerFactory.getLogger(OtherContainersTest.class);

  File config = new File(getClass().getClassLoader().getResource("general-9.85.yml").getFile());

  static {
    File profile = new File("/Users/bastiaanbijl/Documents/Sysunite/GitHub/Sysunite/coins-2-validator/validator-cli/src/test/resources/profiles/profile.lite-9.85-generated.xml");
    SimpleHttpServer.serveFile(profile, "application/xml", 9876);
  }

  @Test
  public void test() {


    log.info("Read "+config.getPath());

    System.setProperty("user.dir", config.getParent());

    String[] args = {"run", config.getPath(), "-l",
      "VC_CodelistV2.ccr"
    };
    Application.main(args);
  }
}
