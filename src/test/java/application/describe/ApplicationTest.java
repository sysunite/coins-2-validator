package application.describe;

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
  File container2 = new File(getClass().getClassLoader().getResource("coins-1.1/VC5.ccr").getFile());

  File config = new File(getClass().getClassLoader().getResource("general-9.85.yml").getFile());

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
