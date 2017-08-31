package com.sysunite.coinsweb.rdfutil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * @author bastbijl, Sysunite 2017
 */
public class Utils {

  private static final Logger log = LoggerFactory.getLogger(Utils.class);




  static List<String> rdf4jFileExtensions = Arrays.asList(
    "rdf", "rdfs", "owl", "xml",
    "nt",
    "ttl",
    "n3",
    "trix", //"xml",
    "trig",
    "brf",
    "nq",
    "jsonld",
    "rj"
//    "xhtml", "html"
  );

  static List<String> rdf4jContentTypes = Arrays.asList(
    "application/rdf+xml", "application/xml", "text/xml",
    "application/n-triples", "text/plain",
    "text/turtle", "application/x-turtle",
    "text/n3", "text/rdf+n3",
    "application/trix",
    "application/trig", "application/x-trig",
    "application/x-binary-rdf",
    "application/n-quads", "text/x-nquads", "text/nquads",
    "application/ld+json",
    "application/rdf+json"
//    "application/xhtml+xml", "application/html", "text/html"
  );

  public static boolean isRdfFile(File file) {
    return isRdfFile(file.getName());
  }
  public static boolean isRdfFile(String fileName) {
    int index = fileName.lastIndexOf('.');
    if(index != -1) {
      String extension = fileName.substring(index+1);
      return rdf4jFileExtensions.contains(extension);
    }
    return false;
  }

  public static boolean isRdfContentType(String contentType) {
    return rdf4jContentTypes.contains(contentType);
  }

  public static String withoutHash(String input) {
    if(input == null) {
      return null;
    }
    while(input.endsWith("#")) {
      input = input.substring(0, input.length()-1);
    }
    return input;
  }

  public static String withoutHashOrSlash(String input) {
    if(input == null) {
      return null;
    }
    while(input.endsWith("#") || input.endsWith("/")) {
      input = input.substring(0, input.length()-1);
    }
    return input;
  }

  public static boolean equalNamespace(String ns1, String ns2) {
    if(ns1 == null || ns2 == null || ns1.isEmpty() || ns2.isEmpty()) {
      return false;
    }
    return withoutHashOrSlash(ns1).equals(withoutHashOrSlash(ns2));
  }

  public static boolean containsNamespace(String ns, Iterable<String> set) {
    if(ns == null || ns.isEmpty() || set == null) {
      return false;
    }
    ns = withoutHashOrSlash(ns);
    Iterator<String> iterator = set.iterator();
    while(iterator.hasNext()) {
      if(ns.equals(withoutHashOrSlash(iterator.next()))) {
        return true;
      }
    }
    return false;
  }




}
