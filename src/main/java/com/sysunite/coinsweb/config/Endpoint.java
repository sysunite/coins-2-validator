package com.sysunite.coinsweb.config;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.util.StdConverter;
import org.apache.log4j.Logger;

import java.util.Map;

import static com.sysunite.coinsweb.config.Parser.isNotNull;
import static com.sysunite.coinsweb.config.Parser.validate;

//import org.apache.jena.query.DatasetAccessor;
//import org.apache.jena.query.DatasetAccessorFactory;
//import org.apache.jena.riot.web.HttpOp;

/**
 * @author bastbijl, Sysunite 2017
 */
@JsonDeserialize(converter=EndpointSanitizer.class)
public class Endpoint {

  private static Logger log = Logger.getLogger(Endpoint.class);

  private String adapter;
//  private String uri;
//  private String user;
//  private String password;

  private Map<String, String> config;

  public String getAdapter() {
    return adapter;
  }
  public Map<String, String> getConfig() {
    return config;
  }
//  public String getUri() {
//    return uri;
//  }
//  public String getUser() {
//    return user;
//  }
//  public String getPassword() {
//    return password;
//  }

  public void setAdapter(String adapter) {
    validate(adapter, "graphdb"); // , "virtuoso", "fuseki"
    this.adapter = adapter;
  }

  public void setConfig(Map<String, String> config) {
    this.config = config;
  }

//  public void setUri(String uri) {
//    this.uri = uri;
//  }
//
//  public void setUser(String user) {
//    this.user = user;
//  }
//
//  public void setPassword(String password) {
//    this.password = password;
//  }




//  public HttpClient getAuthenticatedClient() {
//    if(user == null || user.isEmpty() || password == null || password.isEmpty()) {
//      return null;
//    }
//    CredentialsProvider credsProvider = new BasicCredentialsProvider();
//    Credentials credentials = new UsernamePasswordCredentials(user, password);
//    credsProvider.setCredentials(AuthScope.ANY, credentials);
//    HttpClient httpclient = HttpClients.custom()
//    .setDefaultCredentialsProvider(credsProvider)
//    .build();
//    HttpOp.setDefaultHttpClient(httpclient);
//    return httpclient;
//  }

//  public DatasetAccessor connect() {
//    HttpClient httpclient = getAuthenticatedClient();
//    if(httpclient != null) {
//      return DatasetAccessorFactory.createHTTP(uri, httpclient);
//    } else {
//      return DatasetAccessorFactory.createHTTP(uri);
//    }
//  }
}

class EndpointSanitizer extends StdConverter<Endpoint, Endpoint> {

  private static Logger log = Logger.getLogger(EndpointSanitizer.class);

  @Override
  public Endpoint convert(Endpoint obj) {

    isNotNull(obj.getAdapter());
//    isUri(obj.getUri());

//    // Test if this is a fully available endpoint by temporarily writing a graph to it
//    RuntimeException error = new RuntimeException("The endpoint " + obj.getUri() + " with credentials " + obj.getUser() + "/" + obj.getPassword() + " was not accessable for writing.");
//
//    String namespace = "http://connection-test/ns#" + RandomStringUtils.random(4, true, true);
//    Model model = ModelFactory.createDefaultModel();
//    StringReader content = new StringReader(
//    "<?xml version=\"1.0\"?>\n" +
//    "<rdf:RDF\n" +
//    "  xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n" +
//    "  xmlns:ns=\"http://connection-test/ns#\">\n" +
//    "  <rdf:Description rdf:about=\"http://connection-test/ns#test\">\n" +
//    "    <ns:rel>test</ns:rel>\n" +
//    "  </rdf:Description>\n" +
//    "</rdf:RDF>");
//    model.read(content, namespace, "RDF/XML");
//
//    try {
//      DatasetAccessor connection = obj.connect();
//      connection.add(namespace, model);
//
//      Model reloaded = connection.getModel(namespace);
//      if(reloaded == null && reloaded.isEmpty()) {
//        throw error;
//      }
//
//    } catch(NullPointerException e) {
//      throw error;
//    } catch(HttpException e) {
//      throw error;
//    }

    return obj;
  }
}