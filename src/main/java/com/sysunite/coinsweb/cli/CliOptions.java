package com.sysunite.coinsweb.cli;

import org.apache.commons.cli.*;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author bastbijl, Sysunite 2017
 */
public class CliOptions {

  private static final Logger log = Logger.getLogger(CliOptions.class);



  public static Options getOptions() {

    Options options = new Options();
    options.addOption("b", "both", false, "generate both html and xml report");
    options.addOption("c", "custom-profile", true, "custom profile file to use (e.g.: custom.profile)");
    options.addOption("e", false, "run on empty container, for debugging profile files");
    options.addOption("f", "fuseki", true, "address of fuseki service (e.g.: http://localhost:3030), please create a dataset named 'coins'");
    options.addOption("h", "help", false, "print help");
    options.addOption("o", true, "output file (default: report.html)");
    options.addOption("p", "profile", true, "profile to use (default: \"COINS 2.0 Lite\")");
    options.addOption("v", "profileversion", true, "profileversion to use (default: \"0.9.60-Original\")");
    options.addOption("q", false, "quiet, no output to the console");
    options.addOption("x", "xml", false, "generate xml report (html is default)");

    return options;
  }
  public static void usage() {
    if(!Application.QUIET) {
      HelpFormatter formatter = new HelpFormatter();
      formatter.printHelp("coins-cli map", getOptions());
    }
  }









  // Instance variables
  private CommandLineParser parser = new DefaultParser();
  private CommandLine cmd;

  // Constructor
  public CliOptions(String[] args) throws ParseException {
    cmd = parser.parse( getOptions(), args);
  }



  // External interface methods
  public boolean emptyRun() { return cmd.hasOption("e"); }
  public boolean quietMode() { return cmd.hasOption("q"); }
  public boolean printHelpOption() { return cmd.hasOption("h"); }

  public boolean generateXml() { return cmd.hasOption("b") || cmd.hasOption("x"); }
  public boolean generateHtml() { return cmd.hasOption("b") || !cmd.hasOption("x"); }

  public boolean hasFusekiAddress() { return cmd.hasOption("f"); }
  public String getFusekiAddress() { return (!hasFusekiAddress()) ? null : cmd.getOptionValue("f"); }

  public boolean hasProfile() { return cmd.hasOption("p"); }
  public String getProfile() { return (!hasProfile()) ? null : cmd.getOptionValue("p"); }

  public boolean hasProfileVersion() { return cmd.hasOption("v"); }
  public String getProfileVersion() { return (!hasProfileVersion()) ? null : cmd.getOptionValue("v"); }

  public boolean hasCustomProfile() { return cmd.hasOption("c"); }
  public Path getCustomProfile() { return (!hasCustomProfile()) ? null : CliOptions.resolvePath(cmd.getOptionValue("c")); }

  public boolean hasInputOption() { return cmd.getArgList().size() == 2; }
  public Path getInputOption() { return (!hasInputOption()) ? null : CliOptions.resolvePath(cmd.getArgList().get(1)); }

  public boolean hasOutputOption() { return cmd.hasOption("o"); }
  public Path getOutputOption() { return CliOptions.resolvePath(cmd.getOptionValue("o")); }

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
  public static List<String> breakSemicolonSeparated(String path) {
    return Arrays.asList(path.split(";"));
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
