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
public class Otl171bRefOptTest extends HostFiles {

  Logger log = LoggerFactory.getLogger(Otl171bRefOptTest.class);

  File config = new File(getClass().getClassLoader().getResource("src/test/resources/general-9.85.yml").getFile());
//  File config = new File(getClass().getClassLoader().getResource("general-9.84-inmem.yml").getFile());
//  File config = new File(getClass().getClassLoader().getResource("general-9.83.yml").getFile());


  @Test
  public void test() {

    log.info("Read "+config.getPath());

    System.setProperty("user.dir", config.getParent() + "/otl-1.7.1b-ref-opt/");

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

  @Test
  public void testB() {

    log.info("Read "+config.getPath());

    System.setProperty("user.dir", config.getParent() + "/otl-1.7.1b-ref-opt/b/");

    String[] args = {"run", config.getPath(), "-l",
      "VC_CardinalityCheck.ccr",
      "VC_Codelist.ccr",
      "VC_datatypeCheck.ccr",
      "VC_Disjoint.ccr",
      "VC_Expired.ccr",
      "VC_Foute_dir.ccr",
      "VC_Foute_zip.ccr",
      "VC_Geen_OTL.ccr",
      "VC_PropertyCheck.CCR",
      "VC_Restricties.ccr",
      "VC_Transitief.ccr",
    };
    Application.main(args);
  }



  @Test
  public void testGenerate() {

    ArrayList<File> containers = new ArrayList();
    containers.add(new File(getClass().getClassLoader().getResource("otl-1.7.1/VC_CardinalityCheck.ccr").getPath()));
    containers.add(new File(getClass().getClassLoader().getResource("otl-1.7.1/VC_Codelist.ccr").getPath()));
    containers.add(new File(getClass().getClassLoader().getResource("otl-1.7.1/VC_COINS.CCR").getPath()));
    containers.add(new File(getClass().getClassLoader().getResource("otl-1.7.1/VC_datatypeCheck.ccr").getPath()));
    containers.add(new File(getClass().getClassLoader().getResource("otl-1.7.1/VC_Disjoint.ccr").getPath()));
    containers.add(new File(getClass().getClassLoader().getResource("otl-1.7.1/VC_Expired.ccr").getPath()));
    containers.add(new File(getClass().getClassLoader().getResource("otl-1.7.1/VC_P2.ccr").getPath()));
    containers.add(new File(getClass().getClassLoader().getResource("otl-1.7.1/VC_PropertyCheck.CCR").getPath()));
    containers.add(new File(getClass().getClassLoader().getResource("otl-1.7.1/VC_Restricties.ccr").getPath()));
    containers.add(new File(getClass().getClassLoader().getResource("otl-1.7.1/VC_RWSOTL.CCR").getPath()));
    containers.add(new File(getClass().getClassLoader().getResource("otl-1.7.1/VC_Transitief.ccr").getPath()));
    containers.add(new File(getClass().getClassLoader().getResource("otl-1.7.1/VC_units.ccr").getPath()));

    ConfigFile configFile = ConfigFactory.getDefaultConfig(containers);
    String yml = ConfigFactory.toYml(configFile);
    System.out.println(yml);

  }
}
