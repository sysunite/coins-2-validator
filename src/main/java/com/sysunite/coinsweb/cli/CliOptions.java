package com.sysunite.coinsweb.cli;

import org.apache.commons.cli.*;
import org.apache.log4j.Logger;

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

  private static final Logger log = Logger.getLogger(CliOptions.class);

  public static boolean QUIET = false;

  public static void printHeader() {

    if(QUIET) {
      return;
    }

    // Load version from properties file
    Properties props = new Properties();
    String version = "";
    try {
      props.load(Application.class.getResourceAsStream("/coins-cli.properties"));
      version = props.get("version").toString();
    } catch (IOException e) {
      System.out.println("(!) unable to read coins-cli.properties from jar");
    }

    // Print header
    System.out.println(")} \uD83D\uDC1A  COINS 2.0 validator\ncommand line interface (version "+version+")\n");
  }

  public static Options getOptions() {

    Options options = new Options();
    options.addOption("h", "help", false, "print help");
    options.addOption("q", false, "quiet, no output to the console");

    return options;
  }
  public static void usage() {
    if(!CliOptions.QUIET) {
      HelpFormatter formatter = new HelpFormatter();
      formatter.printHelp(
      "\n" +
      "\ncoins-validator describe [args] container.ccr" +
      "\ncoins-validator run [args] config.yml" +
      "\ncat config.yml | coins-validator run [args]" +
      "\n" +
      "\nargs:"
      , getOptions());
    }
  }









  // Instance variables
  private CommandLineParser parser = new DefaultParser();
  private CommandLine cmd;

  // Constructor
  public CliOptions(String[] args) {
    try {
      cmd = parser.parse( getOptions(), args);
      CliOptions.QUIET = quietMode();

    } catch (ParseException e) {
      CliOptions.printHeader();
      System.out.println("(!) " + e.getMessage() + "\n");
      CliOptions.usage();
      System.exit(1);
      return;
    }
  }



  // External interface methods
  public boolean quietMode() { return cmd.hasOption("q"); }
  public boolean printHelpOption() { return cmd.hasOption("h"); }
  public boolean writeLog() { return cmd.hasOption("l"); }



  public boolean hasConfig() { return cmd.getArgs().length > 0; }
  public Path getConfig() { return (!hasConfig()) ? null : CliOptions.resolvePath(cmd.getArgs()[0]); }



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
