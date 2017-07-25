package com.sysunite.coinsweb.parser.profile;

import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import com.sysunite.coinsweb.parser.profile.pojo.Bundle;
import com.sysunite.coinsweb.parser.profile.pojo.ProfileFile;
import com.sysunite.coinsweb.parser.profile.pojo.Query;
import com.sysunite.coinsweb.parser.profile.pojo.QueryConfiguration;
import com.sysunite.coinsweb.parser.profile.util.IndentedCDATAPrettyPrinter;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;


/**
 * @author bastbijl, Sysunite 2017
 */
public class ProfileFileTest {



  @Test
  public void test960() throws IOException {
    testTemplate("profile.lite-9.60.xml");
  }
  @Test
  public void test979() throws IOException {
    testTemplate("profile.lite-9.79.xml");
  }
  @Test
  public void test982() throws IOException {
    testTemplate("profile.lite-9.82.xml");
    reSave("profile.lite-9.82.xml", "profile.lite-9.82-generated.xml");
  }
  @Test
  public void test983() throws IOException {
    testTemplate("profile.lite-9.83.xml");
    reSave("profile.lite-9.83.xml", "profile.lite-9.83-generated.xml");
  }
  @Test
  public void test984() throws IOException {
    testTemplate("profile.lite-9.84.xml");
    reSave("profile.lite-9.84.xml", "profile.lite-9.84-generated.xml");
    testTemplate("profile.lite-9.84-generated.xml");
  }
  public void testTemplate(String resourceFile) throws IOException {



    InputStream file = getClass().getClassLoader().getResource(resourceFile).openStream();
    ProfileFile profileFile = ProfileFile.parse(file);

    System.out.println();
    System.out.println("name: " + profileFile.getName());
    System.out.println("version: " + profileFile.getVersion());
    System.out.println("author: " + profileFile.getAuthor() + "\n");

    if(profileFile.hasQueryConfiguration()) {
      System.out.println("prefixes:\n>>>>>" + profileFile.getQueryConfiguration().getDefaultPrefixes() + "<<<<<");
    }

    for(Bundle bundle : profileFile.getBundles()) {
      System.out.println("bundle type: " + bundle.getType());
      System.out.println("bundle reference: " + bundle.getReference());
      System.out.println("bundle description: " + bundle.parseDescription());

      for(Query query : bundle.getQueries()) {
        System.out.println("query reference: " + query.parseDescription());
        System.out.println("query format:\n>>>>>" + query.parseResultFormat() + "<<<<<");
        System.out.println("query body:\n>>>>>" + query.getQuery() + "<<<<<");

      }
    }
  }
  public void reSave(String resourceFile, String to) throws IOException {

    XmlMapper objectMapper = new XmlMapper();
    objectMapper.enable(ToXmlGenerator.Feature.WRITE_XML_DECLARATION);
    objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    ObjectWriter xmlWriter = objectMapper.writer(new IndentedCDATAPrettyPrinter());

    InputStream file = getClass().getClassLoader().getResource(resourceFile).openStream();
    ProfileFile profileFile = objectMapper.readValue(file, ProfileFile.class);

    Path folder = Paths.get(new File(getClass().getClassLoader().getResource(resourceFile).getFile()).getParent());
    xmlWriter.writeValue(folder.resolve(to).toFile(), profileFile);
  }

  @Test
  public void writeProfileXml() throws IOException {


    XmlMapper objectMapper = new XmlMapper();
    objectMapper.enable(ToXmlGenerator.Feature.WRITE_XML_DECLARATION);
    objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    ObjectWriter xmlWriter = objectMapper.writer(new IndentedCDATAPrettyPrinter());


    QueryConfiguration queryConfiguration = new QueryConfiguration();
    queryConfiguration.setDefaultPrefixes("PREFIX yo: <http://prefixer#yo>");


    File file = new File(Paths.get(getClass().getClassLoader().getResource("profile.format-demo.xml").getFile()).getParent().getParent().getParent()+"/written-profile.xml");
    ProfileFile profileFile = new ProfileFile();
    profileFile.setName("Generated profile");
    profileFile.setVersion("0.0.1");
    profileFile.setAuthor("Profile tester");
    profileFile.setQueryLanguage("SPARQL-1.1");
    profileFile.setQueryConfiguration(queryConfiguration);


    Bundle bundle = new Bundle();
    bundle.setType(Bundle.VALIDATION);

    Query query = new Query();
    query.setQuery("\n\n\n\n ba\n da\n     dddddd");

    ArrayList<Query> queries = new ArrayList<>();
    queries.add(query);

    bundle.setDescription("The one and only bundle");
    bundle.setReference("one-and-only");
    bundle.setQueries(queries);

    ArrayList<Bundle> bundles = new ArrayList<>();
    bundles.add(bundle);
    profileFile.setBundles(bundles);


    xmlWriter.writeValue(file, profileFile);



  }
}