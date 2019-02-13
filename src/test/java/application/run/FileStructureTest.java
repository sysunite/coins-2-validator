package application.run;

import com.sysunite.coinsweb.cli.Application;
import com.sysunite.coinsweb.parser.config.factory.ConfigFactory;
import com.sysunite.coinsweb.parser.config.pojo.ConfigFile;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;

/**
 * @author bastbijl, Sysunite 2017
 */
public class FileStructureTest extends HostFiles {

  Logger log = LoggerFactory.getLogger(FileStructureTest.class);

  File config = new File(getClass().getClassLoader().getResource("general-9.85.yml").getFile());

  @Test
  public void runConfigSingle() {

    log.info("Read "+config.getPath());

    System.setProperty("user.dir", config.getParent() + "/otl-1.7.1/");

    String[] args = {"run", config.getPath(), "-l",
      "VC_CardinalityCheck.ccr",
      "VC_Codelist.ccr",
      "VC_COINS.CCR",
      "VC_datatypeCheck.ccr",
      "VC_Disjoint.ccr",
      "VC_Expired.ccr",
      "VC_P2.ccr",
      "VC_PropertyCheck.CCR",
      "VC_Restricties.ccr",
      "VC_RWSOTL.CCR",
      "VC_Transitief.ccr",
      "VC_units.ccr"
    };
    Application.main(args);
  }
}
