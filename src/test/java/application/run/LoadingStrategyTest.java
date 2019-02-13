package application.run;

import com.sysunite.coinsweb.cli.Application;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;


/**
 * @author bastbijl, Sysunite 2017
 */
public class LoadingStrategyTest extends HostFiles {

  Logger log = LoggerFactory.getLogger(LoadingStrategyTest.class);

  File file = new File(getClass().getClassLoader().getResource("loading-strategy/config-9.85-virtuoso.yml").getFile());

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