package com.sysunite.coinsweb.cli;

import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Properties;

/**
 * @author bastbijl, Sysunite 2017
 */
public class Application {

  private static final Logger log = Logger.getLogger(Application.class);

  public static boolean QUIET = false;

  public static void main(String[] args) {

    CliOptions options;
    try {
      options = new CliOptions(args);
    } catch (ParseException e) {
      Application.printHeader();
      System.out.println("(!)" + e.getMessage() + "\n");
      CliOptions.usage();
      System.exit(1);
      return;
    }

    // Print header
    Application.QUIET = options.quietMode();
    Application.printHeader();

    // Asked for help
    if(options.printHelpOption()) {
      CliOptions.usage();
      System.exit(1);
      return;
    }
  }

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
}
