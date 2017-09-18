package application.run;

import com.sysunite.coinsweb.cli.Application;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * @author bastbijl, Sysunite 2017
 */
public class OtherContainersTest extends HostFiles{

  Logger log = LoggerFactory.getLogger(OtherContainersTest.class);

  File config = new File(getClass().getClassLoader().getResource("general-9.85.yml").getFile());



  @Test
  public void test() {


    log.info("Read "+config.getPath());

    System.setProperty("user.dir", config.getParent());

    String[] args = {"run", config.getPath(), "--log-trace",
      "/Users/bastiaanbijl/Documents/Sysunite/GitHub/Sysunite/coins-2-validator/validator-cli/src/test/resources/dataroom-1.43/Dataroom-1.43.ccr"
//      "VC_CodelistV2.ccr"
    };
    Application.main(args);
  }
}
