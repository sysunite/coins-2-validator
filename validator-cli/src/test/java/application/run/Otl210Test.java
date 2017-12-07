package application.run;

import com.sysunite.coinsweb.cli.Application;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * @author bastbijl, Sysunite 2017
 */
public class Otl210Test extends HostFiles {

  Logger log = LoggerFactory.getLogger(Otl210Test.class);

  File config = new File(getClass().getClassLoader().getResource("general-9.85.yml").getFile());


  @Test
  public void test() {

    log.info("Read "+config.getPath());

    System.setProperty("user.dir", config.getParent() + "/otl-2.1/");

    String[] args = {"run", config.getPath(), "--log-trace",
      "01_NetwerkRuimteVoorbeeld_OTL21.ccr",
      "02_DecompositieWegNetwerk1_OTL21.ccr",
      "03_DecompositieWegNetwerk1_OTL21.ccr",
      "04_PropertiesWegment_OTL21.ccr",
      "05_ConnectedWegmenten_OTL21.ccr",
      "06_RealisedWegvak_OTL21.ccr",
      "07_WegvakEnum_OTL21.ccr",
      "09_ExternalShapeRepresentation_OTL21.ccr",
      "10_DocumentReference_OTL21.ccr",
      "11_AquaductLifecycle_OTL21.ccr",
      "12_LifecycleOTLDocument_OTL21.ccr"
    };
    Application.main(args);
  }
}
