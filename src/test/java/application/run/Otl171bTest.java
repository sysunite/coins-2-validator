package application.run;

import com.sysunite.coinsweb.cli.Application;
import com.sysunite.coinsweb.parser.config.factory.ConfigFactory;
import com.sysunite.coinsweb.parser.config.pojo.ConfigFile;
import com.sysunite.coinsweb.parser.config.pojo.StepDeserializer;
import com.sysunite.coinsweb.steps.StepFactoryImpl;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;

/**
 * @author bastbijl, Sysunite 2017
 */
public class Otl171bTest extends HostFiles {
  Logger log = LoggerFactory.getLogger(Otl171bTest.class);

  File file = new File(getClass().getClassLoader().getResource("general-9.85.yml").getFile());
  String userDir = file.getParent() + "/otl-1.7.1b/";

  @Test
  public void run() {

    log.info("Read "+file.getPath());

    System.setProperty("user.dir", userDir);

    String[] args = {"run",
      file.getPath(),
      "-l",
//      "--yml-to-console",
      "A27A1_170522_COINS 3Angle.ccr",
//      "Dataroom_ZuidNL_COINS2_1.3.ccr",
//      "Dataroom_ZuidNL_COINS2.ccr",
//      "VCON Container 1 As-Is.ccr",
//      "VCON Container 2 As-Planned.ccr",
//      "VCON Container 3 As-Designed.ccr",
//      "VCON Container 4 As-Required.ccr",
//      "VCON Container 5 Verification.ccr"
    };

    Application.main(args);
  }

  @Test
  public void testGenerate() {

    ArrayList<File> containers = new ArrayList();
    containers.add(new File(getClass().getClassLoader().getResource("otl-1.7.1b/Dataroom_ZuidNL_COINS2.ccr").getPath()));

    StepDeserializer.factory = new StepFactoryImpl();

    ConfigFile configFile = ConfigFactory.getDefaultConfig(containers);
    String yml = ConfigFactory.toYml(configFile);
    System.out.println(yml);

  }
}
