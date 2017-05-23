package com.sysunite.coinsweb.cli;

import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
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

  public static final String DESCRIBE_MODE = "describe";
  public static final String RUN_MODE = "run";

  public static boolean QUIET = false;


  public static void printHeader() {

    // Load version from properties file
    Properties props = new Properties();
    String version = "";
    try {
      props.load(Application.class.getResourceAsStream("/coins-cli.properties"));
      version = props.get("version").toString();
    } catch (IOException e) {
      printOutput("(!) unable to read coins-cli.properties from jar");
    }

    // Print header
    printOutput(")} \uD83D\uDC1A  COINS 2.0 validator\ncommand line interface (version " + version + ")\n");
  }

  public static void printOutput(String message) {

    if(QUIET) {
      return;
    }
    System.out.println(message);
  }

  public static Options getOptions() {

    Options options = new Options();
    options.addOption("h", "help", false, "print help");
    options.addOption("l", "log", false, "write log file");
    options.addOption("q", false, "quiet, no output to the console");

    return options;
  }
  public static void usage() {
    if(!CliOptions.QUIET) {
      HelpFormatter formatter = new HelpFormatter();
      formatter.printHelp(
      "\n" +
      "\n coins-validator describe [args] container.ccr" +
      "\n coins-validator run [args] config.yml" +
      "\n" +
      "\nor pipe into run:" +
      "\n" +
      "\n cat config.yml | coins-validator run [args]" +
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
  public boolean quietMode() { return cmd.hasOption("q"); }
  public boolean printHelpOption() { return cmd.hasOption("h"); }
  public boolean writeLog() { return cmd.hasOption("l"); }



  public boolean hasMode() {
    if(cmd.getArgs().length < 1) {
      return false;
    }
    if(DESCRIBE_MODE.equals(cmd.getArgs()[0]) || RUN_MODE.equals(cmd.getArgs()[0])) {
      return true;
    }
    return false;
  }
  public boolean describeMode() {
    return hasMode() && DESCRIBE_MODE.equals(cmd.getArgs()[0].trim());
  }
  public boolean runMode() {
    return hasMode() && RUN_MODE.equals(cmd.getArgs()[0].trim());
  }

  public boolean hasFile() {
    if(cmd.getArgs().length < 2) {
      return false;
    }
    Path path = CliOptions.resolvePath(cmd.getArgs()[1]);
    return path.toFile().exists() && path.toFile().isFile();
  }
  public Path getFile() {
    return (!hasFile()) ? null : CliOptions.resolvePath(cmd.getArgs()[1]);
  }



  public static Path resolvePath(String path) {

    try {
      String userDir = System.getProperty("user.dir");
      log.info("Resolving path "+path.toString()+" to user dir: "+userDir);
      Path currentPath = Paths.get(new File(userDir).getCanonicalPath());
      return currentPath.resolve(path);
    } catch (IOException e) {
      log.warn("Failed to locate path "+path+" relative to user dir "+System.getProperty("user.dir"));
      return null;
    }
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

}
