package com.sysunite.coinsweb.steps;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.sysunite.coinsweb.filemanager.ContainerFile;
import com.sysunite.coinsweb.graphset.ContainerGraphSet;
import com.sysunite.coinsweb.parser.config.pojo.ConfigPart;
import com.sysunite.coinsweb.parser.config.pojo.GraphVarImpl;
import org.eclipse.rdf4j.query.BindingSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;

import static com.sysunite.coinsweb.parser.Parser.isNotNull;

/**
 * @author bastbijl, Sysunite 2017
 */
@JsonInclude(Include.NON_NULL)
public class DocumentReferenceValidation extends ConfigPart implements ValidationStep {

  private static final Logger log = LoggerFactory.getLogger(DocumentReferenceValidation.class);

  public static final String REFERENCE = "DocumentReferenceValidation";


  // Configuration items
  private String type = REFERENCE;
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
  public void setFailed(boolean failed) {
    this.failed = failed;
  }

  private boolean valid = false;
  public boolean getValid() {
    return valid;
  }
  public void setValid(boolean valid) {
    this.valid = valid;
  }

  @JsonSerialize(using = DocumentReferenceSerializer.class)
  private HashMap<String, String> internalDocumentReferences = new HashMap<>();
  public HashMap<String, String> getInternalDocumentReferences() {
    return internalDocumentReferences;
  }
  public void setInternalDocumentReferences(HashMap<String, String> internalDocumentReferences) {
    this.internalDocumentReferences = internalDocumentReferences;
  }

  @JsonSerialize(using = DocumentReferenceSerializer.class)
  private HashMap<String, String> unmatchedInternalDocumentReferences = new HashMap<>();
  public HashMap<String, String> getUnmatchedInternalDocumentReferences() {
    return unmatchedInternalDocumentReferences;
  }
  public void setUnmatchedInternalDocumentReferences(HashMap<String, String> unmatchedInternalDocumentReferences) {
    this.unmatchedInternalDocumentReferences = unmatchedInternalDocumentReferences;
  }


  public void checkConfig() {
    isNotNull(lookIn);
  }

  @Override
  public void execute(ContainerFile container, ContainerGraphSet graphSet) {

    try {

      boolean allReferencesAreSatisfied = true;

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
          if(found) {
            internalDocumentReferences.put(document, value);
          } else {
            unmatchedInternalDocumentReferences.put(document, value);
          }
          allReferencesAreSatisfied &= found;
        }
      }


      valid = allReferencesAreSatisfied;
      failed = false;




    } catch (RuntimeException e) {
      log.warn("Executing failed validationStep of type "+getType());
      log.warn(e.getMessage());
      failed = true;
    }

    // Prepare data to transfer to the template
    if(getFailed()) {
      log.info("\uD83E\uDD49 failed");
    } else {
      if (getValid()) {
        log.info("\uD83E\uDD47 valid");
      } else {
        log.info("\uD83E\uDD48 invalid");
      }
    }
  }

  @JsonIgnore
  public DocumentReferenceValidation clone() {
    DocumentReferenceValidation clone = new DocumentReferenceValidation();

    // Configuration
    clone.setType(this.getType());
    clone.setLookIn(this.getLookIn());
    clone.setParent(this.getParent());

    // Results
//    clone.setInternalDocumentReferences(this.getInternalDocumentReferences());
//    clone.setValid(this.getValid());
//    clone.setFailed(this.getFailed());
    return clone;
  }

}
class DocumentReferenceSerializer extends StdSerializer<HashMap<String, String>> {

  public DocumentReferenceSerializer() {
    this(null);
  }

  public DocumentReferenceSerializer(Class<HashMap<String, String>> t) {
    super(t);
  }

  @Override
  public void serialize(HashMap<String, String> map, JsonGenerator gen, SerializerProvider provider) throws IOException {



//    if(gen instanceof YAMLGenerator) {

//      YAMLGenerator ymlGen = (YAMLGenerator) gen;
      gen.writeStartArray();
      for (String key : map.keySet()) {
        gen.writeStartObject();
        gen.writeStringField("uri", key);
        gen.writeStringField("fileName", map.get(key));
        gen.writeEndObject();
      }
      gen.writeEndArray();


//    } else {
//
//      gen.writeRawValue("");
//
//      for (String key : map.keySet()) {
//        gen.writeFieldName("document");
//        gen.writeStartObject();
//        gen.writeStringField("uri", key);
//        gen.writeStringField("fileName", map.get(key));
//        gen.writeEndObject();
//      }
//    }


  }
}