package com.sysunite.coinsweb.connector;

import org.eclipse.rdf4j.rio.RDFFormat;

import java.io.File;

/**
 * @author bastbijl, Sysunite 2017
 */
public class Rdf4jUtil {



  public static RDFFormat interpretFormat(File file) {
    return interpretFormat(file.getName());
  }




  public static RDFFormat interpretFormat(String fileName) {

    if(fileName == null || fileName.isEmpty()) {
      return null;
    }

    if(fileName.endsWith("rdf") ||
      fileName.endsWith("rdfs") ||
      fileName.endsWith("owl") ||
      fileName.endsWith("xml")) {
      return RDFFormat.RDFXML;
    }

    if(fileName.endsWith("nt")) {
      return RDFFormat.NTRIPLES;
    }

    if(fileName.endsWith("ttl")) {
      return RDFFormat.TURTLE;
    }

    if(fileName.endsWith("n3")) {
      return RDFFormat.N3;
    }

    if(fileName.endsWith("trix")) {
      return RDFFormat.TRIX;
    }

    if(fileName.endsWith("trig")) {
      return RDFFormat.TRIG;
    }

    if(fileName.endsWith("brf")) {
      return RDFFormat.BINARY;
    }

    if(fileName.endsWith("nq")) {
      return RDFFormat.NQUADS;
    }

    if(fileName.endsWith("jsonld")) {
      return RDFFormat.JSONLD;
    }

    if(fileName.endsWith("rj")) {
      return RDFFormat.RDFJSON;
    }

    return null;
  }
}
