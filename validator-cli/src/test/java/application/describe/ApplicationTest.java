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

  @Test
  public void describeFile() {
    File file = new File(getClass().getClassLoader().getResource("VC_CodelistV2.ccr").getFile());
    System.setProperty("user.dir", file.getParent());
    String[] args = { "describe",
      file.getPath(),
      "--yml-to-console",
      "-l"

    };
    Application.main(args);
  }
}
