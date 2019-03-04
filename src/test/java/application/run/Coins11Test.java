package application.run;

import com.sysunite.coinsweb.cli.Application;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * @author bastbijl, Sysunite 2017
 */
public class Coins11Test extends HostFiles {
  Logger log = LoggerFactory.getLogger(Coins11Test.class);

  File file = new File(getClass().getClassLoader().getResource("coins-1.1/coins11-9.85-generated.yml").getFile());

  @Test
  public void run() {

    log.info("Read "+file.getPath());

    System.setProperty("user.dir", file.getParent());

    String[] args = {"run",
      file.getPath(),
      "-l",
//      "--yml-to-console",
      "VC1.ccr",
      "VC2.ccr",
      "VC3.ccr",
      "VC4.ccr",
      "VC5.ccr",
      "VC6.ccr",
      "VC7.ccr",
      "VC8.ccr",
      "VC9.ccr",
      "VC10.ccr",
      "VC11.ccr",
    };

    Application.main(args);
  }
}
