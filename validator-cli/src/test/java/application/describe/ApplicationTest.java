package application.describe;

import application.SimpleHttpServer;
import com.sysunite.coinsweb.cli.Application;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * @author bastbijl, Sysunite 2017
 */
public class ApplicationTest {

  Logger log = LoggerFactory.getLogger(ApplicationTest.class);

  File container1 = new File(getClass().getClassLoader().getResource("VC_CodelistV2.ccr").getFile());
  File container2 = new File(getClass().getClassLoader().getResource("otl-1.7.1b-ref-opt/VC_P2.ccr").getFile());

  File config = new File(getClass().getClassLoader().getResource("general-9.85.yml").getFile());

  static {
    File profile = new File("/Users/bastiaanbijl/Documents/Sysunite/GitHub/Sysunite/coins-2-validator/validator-cli/src/test/resources/profiles/profile.lite-9.85-virtuoso.xml");
    SimpleHttpServer.serveFile(profile, "application/xml", 9876);
  }


  @Test
  public void describeFile() {
    System.setProperty("user.dir", container1.getParent());
    String[] args = { "describe",
      container1.getPath(),
      container2.getPath(),
      "--yml-to-console",
      "-l",
      "-a"

    };
    Application.main(args);
  }

  @Test
  public void describeStore() {
    System.setProperty("user.dir", config.getParent());
    String[] args = { "describe-store",
      config.getPath(),
      "--yml-to-console",
      "-l"

    };
    Application.main(args);
  }

  @Test
  public void describeStoreWithGraph() {
    System.setProperty("user.dir", config.getParent());
    String[] args = { "describe-store",
      config.getPath(),
      "http://www.coinsweb.nl/uploadedFile-CwPzE6iB",
      "--yml-to-console",
      "-l"

    };
    Application.main(args);
  }

  @Test
  public void createContainerFromGraph() {
    System.setProperty("user.dir", config.getParent());
    String[] args = { "create",
      config.getPath(),
      "http://www.coinsweb.nl/uploadedFile-CwPzE6iB",
      "to.ccr",
//      "--yml-to-console",
      "-l"

    };
    Application.main(args);
  }
}
