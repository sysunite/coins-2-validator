package com.sysunite.coinsweb.cli;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.net.server.ServerSocketAppender;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.FileAppender;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import com.sysunite.coinsweb.Version;
import com.sysunite.coinsweb.connector.Connector;
import com.sysunite.coinsweb.connector.ConnectorException;
import com.sysunite.coinsweb.connector.ConnectorFactory;
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
      log.info("(accepting configuration yml files version "+ Version.VERSION+")");

      try {
        File temp = File.createTempFile("temp-file-name", ".tmp");
        log.info("Using temp folder: "+temp.getParent());
        temp.delete();
      } catch (IOException e) {
        log.error(e.getMessage(), e);
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

    if(options.describeFileMode()) {
      log.info("\uD83C\uDFC4 Running in describe mode");
      try {

        if(options.hasContainerFile() > 0) {
          String yml = Describe.run(containers, false, options.absolutePaths());
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
            log.error(e.getMessage(), e);
          } catch (UnsupportedEncodingException e) {
            log.error(e.getMessage(), e);
          } catch (JsonParseException e) {
            log.error(e.getMessage(), e);
          } catch (JsonGenerationException e) {
            log.error(e.getMessage(), e);
          } catch (FileNotFoundException e) {
            log.error(e.getMessage(), e);
          } catch (IOException e) {
            log.error(e.getMessage(), e);
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

    if(options.describeStoreMode()) {
      log.info("\uD83C\uDFC4 Running in describe-store mode");
      try {
        if (options.hasConfigFile()) {

          if (options.hasUri() > 0) {

            File file = options.getConfigFile().toFile();
            ConfigFile configFile = getConfigFile(file);
            Connector connector = getConnector(configFile);
            String response = Describe.run(connector, options.getUri(0)); // todo: more than one
            System.out.print(response);

          } else {

            File file = options.getConfigFile().toFile();
            ConfigFile configFile = getConfigFile(file);
            Connector connector = getConnector(configFile);
            String response = Describe.run(connector);
            System.out.print(response);
          }
        }
      } catch (ConnectorException e) {
        log.error("Describe in store mode failed", e);
      }
    }

    if(options.createMode()) {
      log.info("\uD83C\uDFC4 Running in create mode");
      if(options.hasConfigFile()) {

        if(options.hasUri() > 0) {

          if(options.hasContainerFile() != 1) {
            throw new RuntimeException("Please specify precisely one container file as cli argument");
          }

          File file = options.getConfigFile().toFile();
          ConfigFile configFile = getConfigFile(file);
          Path containerFilePath = options.getContainerFile(0);
          Connector connector = getConnector(configFile);
          try {
            Describe.run(connector, containerFilePath, options.getUri(0));
          } catch (ConnectorException e) {
            log.error("Describing container failed", e);
          }

        } else {
          throw new RuntimeException("Please specify the main graph (phi context) as cli argument");
        }
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
            FileUtils.writeStringToFile(file, Describe.run(containers, true, options.absolutePaths()), "UTF-8");
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        }

        // Get config
        ConfigFile configFile = getConfigFile(file);

        if(options.hasConfigFile() && options.hasContainerFile() > 0) {
          ConfigFactory.overrideContainers(configFile, options.hasContainerFile(), options.getContainerFiles());
        }

        Connector connector = getConnector(configFile);

        boolean successful = Validation.run(configFile, connector);

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

  private static ConfigFile getConfigFile(File file) {

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
    return configFile;
  }

  private static Connector getConnector(ConfigFile configFile) {
    log.info("Construct the connector");
    ConnectorFactory factory = new ConnectorFactoryImpl();
    Connector connector = factory.build(configFile.getEnvironment());

    if(!connector.testConnection()) {
      throw new RuntimeException("Failed to connect to the store");
    }
    return connector;
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

      int port = options.writeLogToPort();

      if (port > -1) {
        log.info("Adding server appender to port "+port);
        ServerSocketAppender serverAppender = new ServerSocketAppender();
        serverAppender.setContext(lc);
        serverAppender.setPort(port);
        serverAppender.setIncludeCallerData(false);
        serverAppender.start();
        root.addAppender(serverAppender);
      }

      ch.qos.logback.classic.Logger loggers = lc.getLogger("com.sysunite");
      loggers.addAppender(fileAppender);
      if(options.writeTraceLog()) {
        loggers.setLevel(Level.TRACE);
      } else {
        loggers.setLevel(Level.INFO);
      }
    }
  }

}
