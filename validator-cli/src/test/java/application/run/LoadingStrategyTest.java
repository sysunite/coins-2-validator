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
public class LoadingStrategyTest {

  Logger log = LoggerFactory.getLogger(LoadingStrategyTest.class);

  File file = new File(getClass().getClassLoader().getResource("loading-strategy/config-9.85-virtuoso.yml").getFile());

  static {
    File profile = new File("/Users/bastiaanbijl/Documents/Sysunite/GitHub/Sysunite/coins-2-validator/validator-cli/src/test/resources/profiles/profile.lite-9.85-virtuoso.xml");
    SimpleHttpServer.serveFile(profile, "application/xml", 9877);
  }

  @Test
  public void run() {

    log.info("Read "+file.getPath());

    System.setProperty("user.dir", file.getParent());

    String[] args = {"run",
    file.getPath(),
    "-l",

    "abc.ccr",
    "abd.ccr",
    };

    Application.main(args);
  }
}