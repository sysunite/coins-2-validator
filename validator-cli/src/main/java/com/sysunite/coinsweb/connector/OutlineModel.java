package com.sysunite.coinsweb.connector;

import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.impl.SimpleNamespace;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.eclipse.rdf4j.model.vocabulary.RDF;

import java.util.*;

import static com.sysunite.coinsweb.rdfutil.Utils.withoutHash;

/**
 * @author bastbijl, Sysunite 2017
 */
@SuppressWarnings("unchecked")
public class OutlineModel extends HashModel {


  Set<Namespace> namespaces = new HashSet<>();
  Set<Resource> contexts = new LinkedHashSet<>();
  Set<Resource> ontologies = new LinkedHashSet<>();
  Set<String> imports = new LinkedHashSet<>();




  public Set<Resource> getContexts() {
    return contexts;
  }
  public Set<Resource> getOntologies() {
    return ontologies;
  }
  public Set<String> getImports() {
    return imports;
  }


  @Override
  public Model unmodifiable() {
    return this;
  }








  @Override
  public Optional<Namespace> getNamespace(String prefix) {
    for (Namespace nextNamespace : namespaces) {
      if (prefix.equals(nextNamespace.getPrefix())) {
        return Optional.of(nextNamespace);
      }
    }
    return Optional.empty();
  }

  @Override
  public Set<Namespace> getNamespaces() {
    return namespaces;
  }

  @Override
  public Namespace setNamespace(String prefix, String name) {
    removeNamespace(prefix);
    Namespace result = new SimpleNamespace(prefix, name);
    namespaces.add(result);
    return result;
  }

  @Override
  public void setNamespace(Namespace namespace) {
    removeNamespace(namespace.getPrefix());
    namespaces.add(namespace);
  }

  @Override
  public Optional<Namespace> removeNamespace(String prefix) {
    Optional<Namespace> result = getNamespace(prefix);
    if (result.isPresent()) {
      namespaces.remove(result.get());
    }
    return result;
  }

  @Override
  public boolean contains(Resource subj, IRI pred, Value obj, Resource... contexts) {
    throw new RuntimeException("Not able to reproduce content of model");
  }

  @Override
  public ValueFactory getValueFactory() {
    return SimpleValueFactory.getInstance();
  }

  @Override
  public boolean add(Resource subj, IRI pred, Value obj, Resource... contexts) {

    if(OWL.ONTOLOGY.equals(obj) && RDF.TYPE.equals(pred)) {
      this.ontologies.add(subj);
    }

    if(OWL.IMPORTS.equals(pred)) {
      if(!(obj instanceof IRI)) {
        throw new RuntimeException("RDF source contains an owl:import statement that does not point to an IRI");
      }
      this.imports.add(withoutHash(obj.stringValue()));
    }

    if(contexts != null && contexts.length > 0) {
      for(Resource context : contexts) {
        this.contexts.add(context);
      }
    }
    return true;
  }

  @Override
  public Iterator<Statement> match(Resource subj, IRI pred, Value obj, Resource... contexts) {
    throw new RuntimeException("Not able to reproduce content of model");
  }

  @Override
  public boolean clear(Resource... context) {
    throw new RuntimeException("Removing not allowed");
  }

  @Override
  public boolean remove(Resource subj, IRI pred, Value obj, Resource... contexts) {
    throw new RuntimeException("Removing not allowed");
  }

  @Override
  public Model filter(Resource subj, IRI pred, Value obj, Resource... contexts) {
    throw new RuntimeException("Not able to reproduce content of model");
  }

  @Override
  public Set<Resource> subjects() {
    throw new RuntimeException("Not able to reproduce content of model");
  }

  @Override
  public Set<IRI> predicates() {
    throw new RuntimeException("Not able to reproduce content of model");
  }

  @Override
  public Set<Value> objects() {
    throw new RuntimeException("Not able to reproduce content of model");
  }

  @Override
  public int size() {
    throw new RuntimeException("Not able to reproduce content of model");
  }

  @Override
  public boolean isEmpty() {
    throw new RuntimeException("Not able to reproduce content of model");
  }

  @Override
  public boolean contains(Object o) {
    throw new RuntimeException("Not able to reproduce content of model");
  }

  @Override
  public Iterator<Statement> iterator() {
    throw new RuntimeException("Not able to reproduce content of model");
  }

  @Override
  public Object[] toArray() {
    throw new RuntimeException("Not able to reproduce content of model");
  }

  @Override
  public <T> T[] toArray(T[] a) {
    throw new RuntimeException("Not able to reproduce content of model");
  }

  @Override
  public boolean add(Statement statement) {
    if(statement.getContext() != null) {
      return add(statement.getSubject(), statement.getPredicate(), statement.getObject(), statement.getContext());
    } else {
      return add(statement.getSubject(), statement.getPredicate(), statement.getObject());
    }
  }

  @Override
  public boolean remove(Object o) {
    throw new RuntimeException("Removing not allowed");
  }

  @Override
  public boolean containsAll(Collection<?> c) {
    throw new RuntimeException("Not able to reproduce content of model");
  }

  @Override
  public boolean addAll(Collection<? extends Statement> c) {
    boolean response = true;
    for(Statement statement : c) {
      response &= add(statement);
    }
    return response;
  }

  @Override
  public boolean removeAll(Collection<?> c) {
    throw new RuntimeException("Removing not allowed");
  }

  @Override
  public boolean retainAll(Collection<?> c) {
    throw new RuntimeException("Removing not allowed");
  }

  @Override
  public void clear() {
    throw new RuntimeException("Removing not allowed");
  }
}
