package com.sysunite.coinsweb.steps;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sysunite.coinsweb.filemanager.ContainerFile;
import com.sysunite.coinsweb.graphset.ContainerGraphSet;
import com.sysunite.coinsweb.parser.config.pojo.ConfigPart;
import com.sysunite.coinsweb.parser.config.pojo.GraphVarImpl;
import org.eclipse.rdf4j.query.BindingSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.sysunite.coinsweb.parser.Parser.isNotNull;

/**
 * @author bastbijl, Sysunite 2017
 */
@JsonInclude(JsonInclude.Include.NON_NULL) // todo wrong this line?
public class DocumentReferenceValidation extends ConfigPart implements ValidationStep {

  private static final Logger log = LoggerFactory.getLogger(DocumentReferenceValidation.class);


  // Configuration items
  private String type = "DocumentReferenceValidation";
  public String getType() {
    return type;
  }
  public void setType(String type) {
    this.type = type;
  }

  private GraphVarImpl lookIn;
  public GraphVarImpl getLookIn() {
    return lookIn;
  }
  public void setLookIn(GraphVarImpl lookIn) {
    this.lookIn = lookIn;
  }


  // Result items
  private boolean failed = true;
  public boolean getFailed() {
    return failed;
  }

  private boolean valid = false;
  public boolean getValid() {
    return valid;
  }

  private List<String> internalDocumentReferences;
  public List<String> getInternalDocumentReferences() {
    return internalDocumentReferences;
  }


  public void checkConfig() {
    isNotNull(lookIn);
  }

  @Override
  public Map<String, Object> execute(ContainerFile container, ContainerGraphSet graphSet) {

    try {

      boolean allReferencesAreSatisfied = true;

      ArrayList<String> ids = new ArrayList();

      if (graphSet.hasContext(getLookIn())) {

        String context = graphSet.contextMap().get(getLookIn());

        String query =

        "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
        "PREFIX cbim: <http://www.coinsweb.nl/cbim-2.0.rdf#> " +
        "SELECT ?document ?filePath ?value " +
        "FROM NAMED <" + context + "> " +
        "WHERE { graph ?g { " +
        "  ?document  rdf:type            cbim:InternalDocumentReference . " +
        "  ?document  cbim:filePath       ?filePath . " +
        "  ?filePath  cbim:datatypeValue  ?value . " +
        "}}";

        int logCount = 0;
        final int MAX_LOG_COUNT = 10;
        List<Object> result = graphSet.select(query);
        for (Object bindingSet : result) {
          String document = ((BindingSet) bindingSet).getBinding("document").getValue().stringValue();
          String filePath = ((BindingSet) bindingSet).getBinding("filePath").getValue().stringValue();
          String value = ((BindingSet) bindingSet).getBinding("value").getValue().stringValue();

          if(++logCount < MAX_LOG_COUNT)
            log.info("Found InternalDocumentReference " + document + " to file " + value);
          else if(logCount == MAX_LOG_COUNT)
            log.info("Found InternalDocumentReference ...");

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


      valid = allReferencesAreSatisfied;



      this.internalDocumentReferences = ids;

    } catch (RuntimeException e) {
      log.warn("Executing failed validationStep of type "+getType());
      log.warn(e.getMessage());
      failed = true;
    }

    // Prepare data to transfer to the template
    if (getValid()) {
      log.info("\uD83E\uDD47 valid");
    } else {
      log.info("\uD83E\uDD48 invalid");
    }

    Map<String, Object> reportItems = new HashMap();

    reportItems.put("failed",                          getFailed());
    reportItems.put("valid",                           getValid());
    reportItems.put("internalDocumentReferences",      getInternalDocumentReferences());

    return reportItems;
  }



}
