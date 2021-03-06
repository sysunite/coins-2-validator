package application.run;

import application.SimpleHttpServer;
import org.junit.Before;
import org.junit.BeforeClass;

import java.io.File;
import java.util.ArrayList;

/**
 * @author bastbijl, Sysunite 2017
 */
public class HostFiles {

  @BeforeClass
  public static void startFileServer() {
    ArrayList<File> files = new ArrayList<>();
    files.add(new File(HostFiles.class.getClassLoader().getResource("profiles/profile.lite-9.83-generated.xml").getFile()));
    files.add(new File(HostFiles.class.getClassLoader().getResource("profiles/profile.lite-9.84-generated.xml").getFile()));
    files.add(new File(HostFiles.class.getClassLoader().getResource("profiles/profile.lite-9.85-generated.xml").getFile()));
    files.add(new File(HostFiles.class.getClassLoader().getResource("profiles/profile.lite-1.01-generated.xml").getFile()));

    SimpleHttpServer.serveFiles(files, "application/xml", 9877);
  }
}
