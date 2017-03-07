package com.sysunite.coinsweb.config;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.util.StdConverter;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClients;
import org.apache.jena.atlas.web.HttpException;
import org.apache.jena.query.DatasetAccessor;
import org.apache.jena.query.DatasetAccessorFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.web.HttpOp;
import org.apache.log4j.Logger;

import java.io.StringReader;

import static com.sysunite.coinsweb.config.Parser.isUri;

/**
 * @author bastbijl, Sysunite 2017
 */
@JsonDeserialize(converter=EndpointSanitizer.class)
public class Endpoint {

  private Logger log = Logger.getLogger(Endpoint.class);

  private String uri;
  private String user;
  private String password;

  public String getUri() {
    return uri;
  }

  public void setUri(String uri) {
    this.uri = uri;
  }

  public String getUser() {
    return user;
  }

  public void setUser(String user) {
    this.user = user;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }




  public HttpClient getAuthenticatedClient() {
    if(user == null || user.isEmpty() || password == null || password.isEmpty()) {
      return null;
    }
    CredentialsProvider credsProvider = new BasicCredentialsProvider();
    Credentials credentials = new UsernamePasswordCredentials(user, password);
    credsProvider.setCredentials(AuthScope.ANY, credentials);
    HttpClient httpclient = HttpClients.custom()
    .setDefaultCredentialsProvider(credsProvider)
    .build();
    HttpOp.setDefaultHttpClient(httpclient);
    return httpclient;
  }

  public DatasetAccessor connect() {
    HttpClient httpclient = getAuthenticatedClient();
    if(httpclient != null) {
      return DatasetAccessorFactory.createHTTP(uri, httpclient);
    } else {
      return DatasetAccessorFactory.createHTTP(uri);
    }
  }
}

class EndpointSanitizer extends StdConverter<Endpoint, Endpoint> {

  private Logger log = Logger.getLogger(EndpointSanitizer.class);

  @Override
  public Endpoint convert(Endpoint obj) {

    isUri(obj.getUri());

    // Test if this is a fully available endpoint by temporarily writing a graph to it
    RuntimeException error = new RuntimeException("The endpoint " + obj.getUri() + " with credentials " + obj.getUser() + "/" + obj.getPassword() + " was not accessable for writing.");

    String namespace = "http://connection-test/ns#" + RandomStringUtils.random(4, true, true);
    Model model = ModelFactory.createDefaultModel();
    StringReader content = new StringReader(
    "<?xml version=\"1.0\"?>\n" +
    "<rdf:RDF\n" +
    "  xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n" +
    "  xmlns:ns=\"http://connection-test/ns#\">\n" +
    "  <rdf:Description rdf:about=\"http://connection-test/ns#test\">\n" +
    "    <ns:rel>test</ns:rel>\n" +
    "  </rdf:Description>\n" +
    "</rdf:RDF>");
    model.read(content, namespace, "RDF/XML");

    try {
      DatasetAccessor connection = obj.connect();
      connection.add(namespace, model);

      Model reloaded = connection.getModel(namespace);
      if(reloaded == null && reloaded.isEmpty()) {
        throw error;
      }

    } catch(NullPointerException e) {
      throw error;
    } catch(HttpException e) {
      throw error;
    }

    return obj;
  }
}