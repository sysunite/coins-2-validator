package com.sysunite.coinsweb.parser.profile;

import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import com.sysunite.coinsweb.parser.IndentedCDATAPrettyPrinter;
import org.junit.Test;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.ArrayList;


/**
 * @author bastbijl, Sysunite 2017
 */
public class ProfileFileTest {





  @Test
  public void testSmall() {
    testTemplate("template-test.xml");
  }
  @Test
  public void test960() {
    testTemplate("profile.lite-9.60.xml");
  }
  public void testTemplate(String resourceFile) {

    XmlMapper objectMapper = new XmlMapper();
    try {
      InputStream file = getClass().getClassLoader().getResource(resourceFile).openStream();
      ProfileFile profileFile = objectMapper.readValue(file, ProfileFile.class);

      System.out.println();
      System.out.println("name: " + profileFile.getName());
      System.out.println("version: " + profileFile.getVersion());
      System.out.println("author: " + profileFile.getAuthor() + "\n");

      System.out.println("prefixes:\n" + profileFile.getQueryConfiguration().cleanDefaultPrefixes() + "\n");

      for(Bundle bundle : profileFile.getBundles()) {
        System.out.println("requirement: " + bundle.getReference());
        System.out.println("requirement: " + bundle.getDescription());

        for(Query query : bundle.getQueries()) {
          System.out.println("query type: " + query.getType());
          System.out.println("query reference: " + query.getDescription());
          System.out.println("query format:\n" + query.cleanFormat());
          System.out.println("query body:\n" + query.cleanQuery() + "\n");

        }
      }



    } catch (Exception e) {
      System.out.println(e.getLocalizedMessage());
    }
  }

  @Test
  public void writeProfileXml() {


    XmlMapper objectMapper = new XmlMapper();
    objectMapper.enable(ToXmlGenerator.Feature.WRITE_XML_DECLARATION);
    objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

    ObjectWriter xmlWriter = objectMapper.writer(new IndentedCDATAPrettyPrinter());



    try {

      QueryConfiguration queryConfiguration = new QueryConfiguration();
      queryConfiguration.setDefaultPrefixes("PREFIX yo: <http://prefixer#yo>");


      File file = new File(Paths.get(getClass().getClassLoader().getResource("template-test.xml").getFile()).getParent().getParent().getParent()+"/written-profile.xml");
      ProfileFile profileFile = new ProfileFile();
      profileFile.setName("Generated profile");
      profileFile.setVersion("0.0.1");
      profileFile.setAuthor("Profile tester");
      profileFile.setQueryLanguage("SPARQL-1.1");
      profileFile.setQueryConfiguration(queryConfiguration);


      Bundle bundle = new Bundle();

      Query query = new Query();
      query.setType(Query.NO_RESULT);
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





    } catch (Exception e) {
      System.out.println(e.getLocalizedMessage());
    }
  }
}