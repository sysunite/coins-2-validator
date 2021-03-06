package com.sysunite.coinsweb.parser.config.pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.util.StdConverter;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.sysunite.coinsweb.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.sysunite.coinsweb.parser.Parser.isNotNull;

/**
 * @author bastbijl, Sysunite 2017
 */
@JsonInclude(Include.NON_NULL)
@JsonDeserialize(converter=ConfigFileSanitizer.class)
public class ConfigFile {
  private static final Logger log = LoggerFactory.getLogger(ConfigFile.class);

  private Environment environment;
  private Run run;
  private String version;

  @JsonIgnore
  private Path localizeTo;

  public ConfigFile() {
  }
  public ConfigFile(Path localizeTo) {
    this.localizeTo = localizeTo;
  }

  public static ConfigFile parse(File file) {
    return parse(file, Paths.get(file.getParent()));
  }
  public static ConfigFile parse(File file, Path basePath) {
    ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
    String message = "";
    try {
      ConfigFile configFile = mapper.readValue(file, ConfigFile.class);
      if(basePath != null) {
        configFile.localizeTo(basePath);
      }
      return configFile;
    } catch (Exception e) {
      message = e.getMessage();
    }
    throw new RuntimeException("Was not able to parse config file: "+message);
  }

  public static ConfigFile parse(InputStream inputStream) {
    ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
    String message = "";
    try {
      return mapper.readValue(inputStream, ConfigFile.class);
    } catch (Exception e) {
      message = e.getMessage();
    }
    throw new RuntimeException("Was not able to parse config file: "+message);
  }

  public Run getRun() {
    return run;
  }
  public Environment getEnvironment() {
    return environment;
  }
  public String getVersion() {
    return version;
  }

  public void setRun(Run run) {
    this.run = run;
    this.run.setParent(this);
  }

  public void setEnvironment(Environment environment) {
    this.environment = environment;
    this.environment.setParent(this);
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public void localizeTo(Path path) {
    this.localizeTo = path;
  }
  public Path resolve(String path) {
    if(this.localizeTo == null) {
      return Paths.get(path);
    } else {
      return this.localizeTo.resolve(Paths.get(path));
    }
  }
  public Path relativize(String path) {
    if(this.localizeTo == null) {
      return Paths.get(path);
    } else {
      try {
        return this.localizeTo.relativize(Paths.get(path));
      } catch(IllegalArgumentException e) {
        return Paths.get(path);
      }
    }
  }
}

class ConfigFileSanitizer extends StdConverter<ConfigFile, ConfigFile> {
  private static final Logger log = LoggerFactory.getLogger(ConfigFileSanitizer.class);

  @Override
  public ConfigFile convert(ConfigFile obj) {

    isNotNull(obj.getEnvironment());
    isNotNull(obj.getRun());

    isNotNull(obj.getVersion());
    if(!Version.VERSION.equals(obj.getVersion())) {
      throw new RuntimeException("The config yml is not suitable for this version of the coins-validator");
    }
    return obj;
  }
}
