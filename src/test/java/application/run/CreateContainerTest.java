package application.run;

import com.sysunite.coinsweb.cli.Application;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * @author bastbijl, Sysunite 2017
 */
public class CreateContainerTest extends HostFiles {

  Logger log = LoggerFactory.getLogger(CreateContainerTest.class);

  File config = new File(getClass().getClassLoader().getResource("create-container/config.yml").getFile());

  @Test
  public void test() {

    log.info("Read "+config.getPath());

    System.setProperty("user.dir", config.getParent());

    String[] args = {
      "run",
      config.getPath(),
      "-l",
      "--yml-to-console"
    };
    Application.main(args);
  }
}
