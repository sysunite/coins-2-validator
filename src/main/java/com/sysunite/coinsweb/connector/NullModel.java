package com.sysunite.coinsweb.connector;

import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.impl.AbstractModel;
import org.eclipse.rdf4j.model.impl.SimpleNamespace;
import org.eclipse.rdf4j.model.vocabulary.OWL;

import java.util.*;

import static com.sysunite.coinsweb.rdfutil.Utils.withoutHash;

/**
 * @author bastbijl, Sysunite 2017
 */
@SuppressWarnings("unchecked")
public class NullModel extends AbstractModel {

  private static final long serialVersionUID = -9161104123818983614L;

  Set<Namespace> namespaces = new LinkedHashSet<>();
  Set<Resource> contexts = new LinkedHashSet<>();  //todo this does not work as
  ArrayList<String> imports = new ArrayList<>();

  public NullModel() {
    this(128);
  }

  public NullModel(Model model) {
    this(model.getNamespaces());
    addAll(model);
  }

  public NullModel(Collection<? extends Statement> c) {
    this(c.size());
    addAll(c);
  }

  public NullModel(int size) {
    super();
  }

  public NullModel(Set<Namespace> namespaces, Collection<? extends Statement> c) {
    this(c);
    this.namespaces.addAll(namespaces);
  }

  public NullModel(Set<Namespace> namespaces) {
    this();
    this.namespaces.addAll(namespaces);
  }

  public NullModel(Set<Namespace> namespaces, int size) {
    this(size);
    this.namespaces.addAll(namespaces);
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
  public int size() {
    return 0;
  }

  @Override
  public boolean add(Resource subj, IRI pred, Value obj, Resource... contexts) {

    if(OWL.IMPORTS.equals(pred)) {
      if(!(obj instanceof IRI)) {
        throw new RuntimeException("RDF source contains an owl:import statement that does not point to an IRI");
      }
      imports.add(withoutHash(((IRI)obj).stringValue()));
    }

    if(contexts != null && contexts.length > 0) {
      for(Resource context : contexts) {
        this.contexts.add(context);
      }
    }
    return false;
  }

  public ArrayList<String> getImports() {
    return imports;
  }

  @Override
  public Set<Resource> contexts() {
    return contexts;
  }

  @Override
  public void clear() {
    contexts = new LinkedHashSet<>();
  }

  @Override
  public boolean remove(Object o) {
    return false;
  }

  @Override
  public boolean contains(Object o) {
    return false;
  }

  @Override
  public Iterator iterator() {
    return null;
  }

  @Override
  public boolean contains(Resource subj, IRI pred, Value obj, Resource... contexts) {
    return false;
  }

  @Override
  public boolean remove(Resource subj, IRI pred, Value obj, Resource... contexts) {
    return false;
  }

  @Override
  public Model filter(final Resource subj, final IRI pred, final Value obj, final Resource... contexts) {
    return null;
  }

  @Override
  public void removeTermIteration(Iterator iterator, Resource subj, IRI pred, Value obj, Resource... contexts) {

  }
}
