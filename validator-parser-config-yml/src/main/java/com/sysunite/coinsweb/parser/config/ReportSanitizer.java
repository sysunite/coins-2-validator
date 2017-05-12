package com.sysunite.coinsweb.parser.config;

import com.fasterxml.jackson.databind.util.StdConverter;
import org.apache.log4j.Logger;

import static com.sysunite.coinsweb.parser.Parser.canCreateFile;
import static com.sysunite.coinsweb.parser.Parser.isNotNull;

/**
 * @author bastbijl, Sysunite 2017
 */
class ReportSanitizer extends StdConverter<Report, Report> {

  private static final Logger log = Logger.getLogger(ReportSanitizer.class);

  @Override
  public Report convert(Report obj) {

    isNotNull(obj.getType());
    if(obj.getLocation() != null && "file".equals(obj.getLocation().getType())) {
      canCreateFile(obj.getLocation().getPath());
    }

    return obj;
  }
}

