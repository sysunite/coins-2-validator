package com.sysunite.coinsweb.steps;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.sysunite.coinsweb.filemanager.ContainerFile;
import com.sysunite.coinsweb.graphset.ContainerGraphSet;
import com.sysunite.coinsweb.rdfutil.Utils;
import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
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

    if(!container.getContentFiles().isEmpty()) {
      File file = container.getContentFile(container.getContentFiles().iterator().next());

      Model model = Utils.load(file);
      ValueFactory factory = SimpleValueFactory.getInstance();
      IRI InternalDocumentReference = factory.createIRI("http://www.coinsweb.nl/cbim-2.0.rdf#InternalDocumentReference");
      IRI filePath = factory.createIRI("http://www.coinsweb.nl/cbim-2.0.rdf#filePath");
      IRI datatypeValue = factory.createIRI("http://www.coinsweb.nl/cbim-2.0.rdf#datatypeValue");

      for (Resource subject : model.filter(null, RDF.TYPE, InternalDocumentReference).subjects()) {
        IRI instance = (IRI) subject;
        ids.add(instance.toString());
        for (Value object : model.filter(subject, filePath, null).objects()) {
          if (object instanceof IRI) {
            for (Value value : model.filter((IRI) object, datatypeValue, null).objects()) {
              String fileName = value.stringValue();

              boolean found = false;
              for (String attachment : container.getAttachmentFiles()) {
                if (Paths.get(attachment).toFile().getName().equals(fileName)) {
                  found = true;
                  break;
                }
              }
              allReferencesAreSatisfied &= found;
            }
          }
        }
      }
    }


    boolean valid = allReferencesAreSatisfied;

    Map<String, Object> reportItems = new HashMap();

    reportItems.put("valid",                           valid);
    reportItems.put("internalDocumentReferences",      ids);

    return reportItems;
  }
}
