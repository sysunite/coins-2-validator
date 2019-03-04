package com.sysunite.coinsweb.connector;

import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

import java.util.*;

/**
 * @author bastbijl, Sysunite 2017
 */
@SuppressWarnings("unchecked")
public class HashModel implements Model {

  HashSet<Namespace> namespaces = new HashSet<>();
  int hash = 0;

  public int hashCode() {
    return hash;
  }

  @Override
  public Model unmodifiable() {
    return this;
  }

  @Override
  public Set<Namespace> getNamespaces() {
    return namespaces;
  }

  @Override
  public void setNamespace(Namespace namespace) {
    namespaces.add(namespace);
  }

  @Override
  public Optional<Namespace> removeNamespace(String prefix) {
    for(Namespace namespace : namespaces) {
      if(namespace.getPrefix().equals(prefix)) {
        namespaces.remove(namespace);
        return Optional.of(namespace);
      }
    }
    return Optional.empty();
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

    if(contexts.length < 1) {
      return add(getValueFactory().createStatement(subj, pred, obj));
    }

    boolean response = true;
    for(Resource context : contexts) {
      response &= add(getValueFactory().createStatement(subj, pred, obj, context));
    }
    return response;
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
    hash = hash ^ extensiveHash(statement);
    return true;
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

  public static int extensiveHash(Statement statement) {
    if(statement.getObject() instanceof Literal) {

      int result = Objects.hash(statement.getSubject(), statement.getPredicate(), statement.getContext());

      Literal object = (Literal) statement.getObject();

      int objectHash = Objects.hash(object.stringValue(), object.getLanguage(), object.getDatatype());

      result += 31 * result + objectHash;
      return result;
    } else {
      return statement.hashCode();
    }
  }
}
