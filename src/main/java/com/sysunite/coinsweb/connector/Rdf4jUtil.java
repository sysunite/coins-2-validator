package com.sysunite.coinsweb.connector;

import com.sysunite.coinsweb.filemanager.ContainerFile;
import com.sysunite.coinsweb.filemanager.DescribeFactoryImpl;
import com.sysunite.coinsweb.parser.config.factory.FileFactory;
import com.sysunite.coinsweb.parser.config.pojo.Source;
import org.eclipse.rdf4j.rio.RDFFormat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.Paths;
import java.util.ArrayList;

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

  public static ArrayList<String> getImports(Source source, ContainerFile container) {
    String triedReference = "error interpreting source";
    if (Source.FILE.equals(source.getType())) {

      File file = FileFactory.toFile(source);

      ArrayList<String> namespaces = new ArrayList<>();
      ArrayList<String> imports = new ArrayList<>();
      ArrayList<String> ontologies = new ArrayList<>();
      try {
        DescribeFactoryImpl.contextsInFile(new FileInputStream(file), file.getName(), namespaces, imports, ontologies);
        return imports;
      } catch (FileNotFoundException e) {
        e.printStackTrace();
      }
    } else if (Source.ONLINE.equals(source.getType())) {
      throw new RuntimeException("Not implemented");
    } else if (Source.CONTAINER.equals(source.getType())) {

      triedReference = "in container: "+ Paths.get(source.getPath());
      return container.getFileImports(Paths.get(source.getPath()));
    }
    throw new RuntimeException("Source of type "+source.getType()+" could not be read as inputStream: "+triedReference);
  }
}
