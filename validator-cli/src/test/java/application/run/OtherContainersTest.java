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
public class OtherContainersTest {

  Logger log = LoggerFactory.getLogger(OtherContainersTest.class);

//  File config = new File(getClass().getClassLoader().getResource("general-9.85.yml").getFile());
  File config = new File(getClass().getClassLoader().getResource("general-9.85-virtuoso.yml").getFile());

  static {
    File profile = new File("/Users/bastiaanbijl/Documents/Sysunite/GitHub/Sysunite/coins-2-validator/validator-cli/src/test/resources/profiles/profile.lite-9.85-virtuoso.xml");
    SimpleHttpServer.serveFile(profile, "application/xml", 9877);
  }

  @Test
  public void test() {


    log.info("Read "+config.getPath());

    System.setProperty("user.dir", config.getParent());

    String[] args = {"run", config.getPath(), "-l",
      "/Users/bastiaanbijl/Documents/Sysunite/GitHub/Sysunite/coins-2-validator/validator-cli/src/test/resources/dataroom-1.43/Dataroom-1.43_.ccr"
//      "VC_CodelistV2.ccr"
    };
    Application.main(args);
  }
}
