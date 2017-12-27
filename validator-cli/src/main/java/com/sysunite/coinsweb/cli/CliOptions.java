package com.sysunite.coinsweb.cli;

import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * @author bastbijl, Sysunite 2017
 */
public class CliOptions {

  private static final Logger log = LoggerFactory.getLogger(CliOptions.class);

  public static final String DESCRIBE_FILE_MODE = "describe";
  public static final String DESCRIBE_STORE_MODE = "describe-store";
  public static final String RUN_MODE = "run";
  public static final String CREATE_MODE = "create";

  public static boolean QUIET = false;


  public static String getVersion() {
    // Load version from properties file
    Properties props = new Properties();
    String version = "";
    try {
      props.load(Application.class.getResourceAsStream("/coins-cli.properties"));
      version = props.get("version").toString();
    } catch (IOException e) {
      printOutput("(!) unable to read coins-cli.properties from jar");
    }
    return version;
  }
  public static void printHeader() {

    // Print header
    printOutput("[)}] \uD83D\uDC1A COINS 2.0 validator\ncommand line interface (version " + getVersion() + ")\n");
  }

  public static void printOutput(String message) {

    if(QUIET) {
      return;
    }
    System.out.println(message);
  }

  public static Options getOptions() {

    Options options = new Options();
    options.addOption("a", "absolute", false, "use absolute paths when generating config-generated.yml");
    options.addOption("h", "help", false, "print help");
    options.addOption("l", "log", false, "write log file");
    options.addOption(null, "log-trace", false, "write verbose log file");
    options.addOption(null, "log-port", true, "also send logs to port");
    options.addOption("q", false, "quiet, no output to the console");
    options.addOption(null, "yml-to-console", false, "print the generated config to the console");

    return options;
  }
  public static void usage() {
    if(!CliOptions.QUIET) {
      HelpFormatter formatter = new HelpFormatter();
      formatter.printHelp(
      "\n" +
      "\n coins-validator run [args] container.ccr" +
      "\n coins-validator run [args] config.yml [container.ccr ...]" +
      "\n coins-validator describe [args] container.ccr" +
      "\n" +
      "\nargs:"
      , getOptions());
    }
  }









  // Instance variables
  private CommandLineParser parser = new BasicParser();
  private CommandLine cmd;

  // Constructor
  public CliOptions(String[] args) {
    try {
      cmd = parser.parse( getOptions(), args);
      CliOptions.QUIET = quietMode();
      if(!hasMode()) {
        throw new RuntimeException("No run mode is specified");
      }

    } catch (Exception e) {
      CliOptions.printHeader();
      printOutput("(!) " + e.getMessage() + "\n");
      CliOptions.usage();
      System.exit(1);
      return;
    }
  }



  // External interface methods
  public boolean ymlToConsole() { return cmd.hasOption("yml-to-console"); }
  public boolean absolutePaths() { return cmd.hasOption("a"); }
  public boolean quietMode() { return cmd.hasOption("q") || ymlToConsole(); }
  public boolean printHelpOption() { return cmd.hasOption("h"); }
  public boolean writeLog() { return cmd.hasOption("l") || writeTraceLog() || (writeLogToPort() > -1); }
  public boolean writeTraceLog() { return cmd.hasOption("log-trace"); }
  public int writeLogToPort() {
    if(!cmd.hasOption("log-port")) {
      return -1;
    }
    return Integer.parseInt(cmd.getOptionValue("log-port"));
  }



  public boolean hasMode() {
    if(cmd.getArgs().length < 1) {
      return false;
    }
    if(DESCRIBE_FILE_MODE.equals(cmd.getArgs()[0]) ||
      DESCRIBE_STORE_MODE.equals(cmd.getArgs()[0]) ||
      RUN_MODE.equals(cmd.getArgs()[0]) ||
      CREATE_MODE.equals(cmd.getArgs()[0])) {
      return true;
    }
    return false;
  }
  public boolean describeFileMode() {
    return hasMode() && DESCRIBE_FILE_MODE.equals(cmd.getArgs()[0].trim());
  }
  public boolean describeStoreMode() {
    return hasMode() && DESCRIBE_STORE_MODE.equals(cmd.getArgs()[0].trim());
  }
  public boolean runMode() {
    return hasMode() && RUN_MODE.equals(cmd.getArgs()[0].trim());
  }
  public boolean createMode() {
    return hasMode() && CREATE_MODE.equals(cmd.getArgs()[0].trim());
  }


  public boolean hasProfileFile() {
    if(cmd.getArgs().length < 2) {
      return false;
    }
    Path path = CliOptions.resolvePath(cmd.getArgs()[1]);
    return isProfileFile(path); //path.toFile().exists() && path.toFile().isFile() &&
  }
  public Path getProfileFile() {
    return (!hasProfileFile()) ? null : CliOptions.resolvePath(cmd.getArgs()[1]);
  }

  public boolean hasConfigFile() {
    if(cmd.getArgs().length < 2) {
      return false;
    }
    Path path = CliOptions.resolvePath(cmd.getArgs()[1]);
    return isConfigFile(path); // path.toFile().exists() && path.toFile().isFile() &&
  }
  public Path getConfigFile() {
    return (!hasConfigFile()) ? null : CliOptions.resolvePath(cmd.getArgs()[1]);
  }

  public int hasContainerFile() {
    return getContainerFiles().length;
  }
  public Path getContainerFile(int i) {
    Path[] containerPaths = getContainerFiles();
    if(i >= containerPaths.length) {
      return null;
    }
    return containerPaths[i];
  }
  public Path[] getContainerFiles() {
    ArrayList<Path> containerPaths = new ArrayList();
    for(int i = 1; i < cmd.getArgs().length; i++) {
      try {
        Path path = CliOptions.resolvePath(cmd.getArgs()[i]);
        if(isContainerFile(path)) { //path.toFile().exists() && path.toFile().isFile() &&
          containerPaths.add(path);
        }
      } catch (Exception e) {}
    }
    return containerPaths.toArray(new Path[0]);
  }

  public int hasUri() {
    return getUris().length;
  }
  public String getUri(int i) {
    String[] uris = getUris();
    if(i >= uris.length) {
      return null;
    }
    return uris[i];
  }
  public String[] getUris() {
    ArrayList<String> uris = new ArrayList();
    for(int i = 1; i < cmd.getArgs().length; i++) {
      try {
        String argument = cmd.getArgs()[i];
        if(isUri(argument)) {
          uris.add(argument);
        }
      } catch (Exception e) {}
    }
    return uris.toArray(new String[0]);
  }



  public static Path resolvePath(String path) {

    try {
      String userDir = System.getProperty("user.dir");
      Path currentPath = Paths.get(new File(userDir).getCanonicalPath());
      return currentPath.resolve(path);
    } catch (IOException e) {
      log.warn("Failed to locate path "+path+" relative to user dir "+System.getProperty("user.dir"));
      return null;
    }
  }

  public static Path makeUnique(Path path) {
    String filePath = path.toString();
    String filePathBase = filePath.substring(0, filePath.lastIndexOf("."));
    String fileExtension = filePath.substring(filePath.lastIndexOf("."));
    int i = 1;
    while(path.toFile().exists()) {
      filePath = filePathBase + "."+ (i++) + fileExtension;
      path = resolvePath(filePath);
    }
    return path;
  }


  public static List<Path> resolvePaths(List<String> paths) {
    List<Path> result = new ArrayList<>();
    try {
      for(String path : paths) {
        result.add(Paths.get(new File(".").getCanonicalPath()).resolve(path));
      }
    } catch (IOException e) {
    }
    return result;
  }



  public static boolean isUri(String uri) {
    if(!uri.toString().startsWith("http")) {
      return false;
    }
    try {
      URI parsed = new URI(uri);
    } catch (URISyntaxException e) {
      return false;
    }
    return true;
  }

  public static boolean isContainerFile(Path path) {
    return path.toString().toLowerCase().endsWith(".ccr") || path.toString().toLowerCase().endsWith(".zip");
  }

  public static boolean isConfigFile(Path path) {
    return path.toString().endsWith(".yml") || path.toString().endsWith(".yaml");
  }

  public static boolean isProfileFile(Path path) {
    return path.toString().endsWith(".xml");
  }

}
