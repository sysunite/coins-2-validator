package com.sysunite.coinsweb.steps;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.sysunite.coinsweb.filemanager.ContainerFile;
import com.sysunite.coinsweb.graphset.ContainerGraphSet;
import com.sysunite.coinsweb.graphset.ContainerGraphSetImpl;
import com.sysunite.coinsweb.parser.config.pojo.ConfigFile;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @author bastbijl, Sysunite 2017
 */
@JsonIgnoreProperties({"type"})
public class DocumentReferenceValidation implements ValidationStep {

  private static final Logger log = LoggerFactory.getLogger(DocumentReferenceValidation.class);

  @Override
  public Map<String, Object> execute(ContainerFile container, ContainerGraphSet graphSet) {

    boolean allReferencesAreSatisfied = true;

    ArrayList<String> ids = new ArrayList();

    if(graphSet.hasContext(ContainerGraphSetImpl.INSTANCE_UNION_GRAPH)) {

      String context = graphSet.contextMap().get(ContainerGraphSetImpl.INSTANCE_UNION_GRAPH);

      String query =

        "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
        "PREFIX cbim: <http://www.coinsweb.nl/cbim-2.0.rdf#> " +
        "FROM NAMED <"+context+"> " +
        "SELECT ?document ?filePath ?value " +
        "WHERE { " +
        "  ?document  rdf:type            cbim:InternalDocumentReference . " +
        "  ?document  cbim:filePath       ?filePath . " +
        "  ?filePath  cbim:datatypeValue  ?value . " +
        "}";

      TupleQueryResult result = (TupleQueryResult)graphSet.select(query);
      while (result.hasNext()) {
        BindingSet row = result.next();
        String document = row.getBinding("document").getValue().stringValue();
        String filePath = row.getBinding("filePath").getValue().stringValue();
        String value = row.getBinding("value").getValue().stringValue();

        log.info("Found InternalDocumentReference "+document+" to file "+value);

        boolean found = false;
        for (String attachment : container.getAttachmentFiles()) {
          if (Paths.get(attachment).toFile().getName().equals(value)) {
            found = true;
            break;
          }
        }
        allReferencesAreSatisfied &= found;
      }
    }


    boolean valid = allReferencesAreSatisfied;

    Map<String, Object> reportItems = new HashMap();

    reportItems.put("valid",                           valid);
    reportItems.put("internalDocumentReferences",      ids);

    return reportItems;
  }

  @JsonIgnore
  private ConfigFile configFile;
  @Override
  public void setParent(Object configFile) {
    this.configFile = (ConfigFile) configFile;
  }
  public ConfigFile getParent() {
    return this.configFile;
  }
}
