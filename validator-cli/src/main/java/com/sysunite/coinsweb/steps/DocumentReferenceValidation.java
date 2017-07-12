package com.sysunite.coinsweb.steps;

import com.sysunite.coinsweb.filemanager.ContainerFile;
import com.sysunite.coinsweb.graphset.ContainerGraphSet;
import com.sysunite.coinsweb.parser.config.pojo.ConfigPart;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.sysunite.coinsweb.parser.Parser.isNotNull;

/**
 * @author bastbijl, Sysunite 2017
 */

public class DocumentReferenceValidation extends ConfigPart implements ValidationStep {

  private static final Logger log = LoggerFactory.getLogger(DocumentReferenceValidation.class);

  private String type = "DocumentReferenceValidation";
  public String getType() {
    return type;
  }
  public void setType(String type) {
    this.type = type;
  }

  private String lookIn;
  public String getLookIn() {
    return lookIn;
  }
  public void setLookIn(String lookIn) {
    this.lookIn = lookIn;
  }

  public void checkConfig() {
    isNotNull(lookIn);
  }

  @Override
  public Map<String, Object> execute(ContainerFile container, ContainerGraphSet graphSet) {

    boolean allReferencesAreSatisfied = true;

    ArrayList<String> ids = new ArrayList();

    if(graphSet.hasContext(getLookIn())) {

      String context = graphSet.contextMap().get(getLookIn());

      String query =

        "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
        "PREFIX cbim: <http://www.coinsweb.nl/cbim-2.0.rdf#> " +
        "SELECT ?document ?filePath ?value " +
        "FROM NAMED <"+context+"> " +
        "WHERE { graph ?g { " +
        "  ?document  rdf:type            cbim:InternalDocumentReference . " +
        "  ?document  cbim:filePath       ?filePath . " +
        "  ?filePath  cbim:datatypeValue  ?value . " +
        "}}";

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



}
