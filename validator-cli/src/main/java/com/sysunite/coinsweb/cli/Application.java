package com.sysunite.coinsweb.cli;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.FileAppender;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import com.sysunite.coinsweb.connector.ConnectorFactoryImpl;
import com.sysunite.coinsweb.filemanager.ContainerFileImpl;
import com.sysunite.coinsweb.filemanager.DescribeFactoryImpl;
import com.sysunite.coinsweb.parser.config.factory.ConfigFactory;
import com.sysunite.coinsweb.parser.config.pojo.ConfigFile;
import com.sysunite.coinsweb.parser.config.pojo.StepDeserializer;
import com.sysunite.coinsweb.parser.config.pojo.Store;
import com.sysunite.coinsweb.parser.profile.pojo.ProfileFile;
import com.sysunite.coinsweb.parser.profile.util.IndentedCDATAPrettyPrinter;
import com.sysunite.coinsweb.runner.Describe;
import com.sysunite.coinsweb.runner.Validation;
import com.sysunite.coinsweb.steps.StepFactoryImpl;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

/**
 * @author bastbijl, Sysunite 2017
 */
public class Application {

  private static final Logger log = LoggerFactory.getLogger(Application.class);


  private static CliOptions options;

  public static void main(String[] args) {

    options = new CliOptions(args);

    // Print header
    CliOptions.printHeader();

    // Asked for help
    if (options.printHelpOption()) {
      CliOptions.usage();
      System.exit(1);
      return;
    }

    if (options.writeLog()) {
      setLoggers("validator.log");
      log.info(")} COINS 2.0 validator - version " + CliOptions.getVersion());

      try {
        File temp = File.createTempFile("temp-file-name", ".tmp");
        log.info("Using temp folder: "+temp.getParent());
        temp.delete();
      } catch (IOException e) {
        e.printStackTrace();
      }

    } else {
      setLoggers(null);
    }




    Store.factory = new ConnectorFactoryImpl();
    StepDeserializer.factory = new StepFactoryImpl();
    ConfigFactory.setDescribeFactory(new DescribeFactoryImpl());


    // Get containers
    ArrayList<File> containers = new ArrayList();
    for(int i = 0; i < options.hasContainerFile(); i++) {
      File containerFile = new ContainerFileImpl(options.getContainerFile(i).toFile().toString());
      containers.add(containerFile);
    }
    log.info("Gathered "+containers.size()+" container files");

    if(options.describeMode()) {
      log.info("\uD83C\uDFC4\uD83C\uDFFB Running in describe mode");
      try {

        if(options.hasContainerFile() > 0) {
          String yml = Describe.run(options.absolutePaths(), containers);
          if (options.ymlToConsole()) {
            System.out.print(yml);
          }
        } else if(options.hasProfileFile()) {
          try {
            XmlMapper objectMapper = new XmlMapper();
            objectMapper.enable(ToXmlGenerator.Feature.WRITE_XML_DECLARATION);
            objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
            ObjectWriter xmlWriter = objectMapper.writer(new IndentedCDATAPrettyPrinter());

            InputStream file = new FileInputStream(options.getProfileFile().toFile());
            ProfileFile profileFile = objectMapper.readValue(file, ProfileFile.class);

            ByteArrayOutputStream bufferStream = new ByteArrayOutputStream();
            xmlWriter.writeValue(bufferStream, profileFile);
            String xml = bufferStream.toString("UTF-8");
            System.out.println(xml);
          } catch (JsonMappingException e) {
            e.printStackTrace();
          } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
          } catch (JsonParseException e) {
            e.printStackTrace();
          } catch (JsonGenerationException e) {
            e.printStackTrace();
          } catch (FileNotFoundException e) {
            e.printStackTrace();
          } catch (IOException e) {
            e.printStackTrace();
          }
        }

        log.info("Finished successfully, quitting");
        CliOptions.printOutput("Finished successfully");
        System.exit(0);

      } catch (RuntimeException e) {
        log.error(e.getMessage(), e);
        CliOptions.printOutput("Stopped with error");
        System.exit(1);
      }
    }

    if(options.runMode()) {
      log.info("\uD83C\uDFC4 Running in run mode");
      try {

        File file;
        if (options.hasConfigFile()) {
          file = options.getConfigFile().toFile();
        } else {
          file = CliOptions.resolvePath("config-generated.yml").toFile();
          try {
            FileUtils.writeStringToFile(file, Describe.run(options.absolutePaths(), containers), "UTF-8");
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        }

        // Get config
        ConfigFile configFile = null;

        try {
          if (file != null) {
            log.info("Try to read config.yml from file");
            configFile = ConfigFile.parse(file);
            log.info("Done reading config.yml from file");
          } else {
            log.info("Try to read config.yml from pipe");
            configFile = ConfigFile.parse(System.in);
            log.info("Done reading config.yml from pipe");
          }
        } catch(RuntimeException e) {
          log.error("Unable to read config.yml: "+e.getLocalizedMessage());
          CliOptions.printOutput("Stopped with error");
          System.exit(1);
        }

        if(options.hasConfigFile() && options.hasContainerFile() > 0) {
          ConfigFactory.overrideContainers(configFile, options.hasContainerFile(), options.getContainerFiles());
        }

        boolean successful = Validation.run(configFile);

        // Optionally print the config.yml that was executed eventually
        if(options.ymlToConsole()) {
          System.out.print(ConfigFactory.getDefaultConfigString(configFile));
        }

        if(successful) {

          log.info("Finished successfully, quitting");
          CliOptions.printOutput("Finished successfully");
          System.exit(0);
        } else {

          log.info("Finished with errors, quitting");
          CliOptions.printOutput("Finished with errors");
          System.exit(1);
        }

      } catch (RuntimeException e) {
        log.error(e.getMessage(), e);
        CliOptions.printOutput("Stopped with error");
        System.exit(1);
      }
    }
  }


  private static void setLoggers(String filePath) {

    LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();

    ch.qos.logback.classic.Logger root = lc.getLogger(Logger.ROOT_LOGGER_NAME);
    root.detachAppender("console");

    if(filePath != null) {

      Path fullPath = CliOptions.resolvePath(filePath);
      String filePathBase = filePath.substring(0, filePath.lastIndexOf("."));
      int i = 1;
      while(fullPath.toFile().exists()) {
        filePath = filePathBase + "."+ (i++) + ".log";
        fullPath = CliOptions.resolvePath(filePath);
      }

      Path userDir = Paths.get(System.getProperty("user.dir"));
      Path localizedPath = userDir.relativize(fullPath);
      CliOptions.printOutput("Writing log to "+localizedPath);


      PatternLayoutEncoder ple = new PatternLayoutEncoder();
      ple.setPattern("%date{yyyy-MM-dd HH:mm:ss} %level [%file:%line] %msg%n");
      ple.setContext(lc);
      ple.start();

      FileAppender<ILoggingEvent> fileAppender = new FileAppender();
      fileAppender.setFile(fullPath.toString());
      fileAppender.setEncoder(ple);
      fileAppender.setContext(lc);
      fileAppender.start();
      fileAppender.setAppend(false);


      root.addAppender(fileAppender);
      root.setLevel(Level.INFO);
    }
  }

}
