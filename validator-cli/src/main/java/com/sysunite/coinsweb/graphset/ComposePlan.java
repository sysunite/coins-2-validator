package com.sysunite.coinsweb.graphset;

import org.eclipse.rdf4j.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author bastbijl, Sysunite 2017
 */
public class ComposePlan {

  private static final Logger log = LoggerFactory.getLogger(ComposePlan.class);

  private List<Move> list = new ArrayList<>();
  private List<Move> unFinished = new ArrayList<>();

  private int pointer = 0;



  public enum Action { COPY, ADD }

  public void addReversed(Action action, GraphVar from, Resource to, boolean onlyUpdate) {
    Move move = new Move(action, from, to, onlyUpdate);
    list.add(pointer, move);
    unFinished.add(move);
  }

  public void addReversed(Action action, Resource from, Resource to, boolean onlyUpdate) {
    Move move = new Move(action, from, to, onlyUpdate);
    list.add(pointer, move);
  }

  public void add(Action action, Resource from, Resource to, boolean onlyUpdate) {
    Move move = new Move(action, from, to, onlyUpdate);
    list.add(move);
    pointer = list.size();
  }

  public void updateFroms(GraphVar graphVar, Resource context) {
    for(int i = 0; i < unFinished.size(); i++) {
      Move move = unFinished.get(i);
      if(graphVar.equals(move.getFrom())) {
        move.setFrom(context);
        unFinished.remove(i--);
      }
    }
  }



  public String toString() {
    String result = "";
    for(Move move : list) {
      result += "\n" + move.toString();
    }
    return result;
  }

  public List<Move> get() {
    if(!unFinished.isEmpty()) {
      throw new RuntimeException("ComposePlan still consists of unFinished Moves");
    }
    return list;
  }

  public class Move {
    public final Action action;
    public Resource from;
    public GraphVar fromPending;
    public final Resource to;
    public boolean onlyUpdate;
    public Move(Action action, Resource from, Resource to, boolean onlyUpdate) {
      this.action = action;
      this.from = from;
      this.to = to;
      this.onlyUpdate = onlyUpdate;
    }
    public Move(Action action, GraphVar fromPending, Resource to, boolean onlyUpdate) {
      this.action = action;
      this.fromPending = fromPending;
      this.to = to;
      this.onlyUpdate = onlyUpdate;
    }
    public GraphVar getFrom() {
      return fromPending;
    }
    public void setFrom(Resource from) {
      this.from = from;
      this.fromPending = null;
    }
    public String toString() {
      if(action == Action.COPY) {
        return "<"+from+"> to <"+to+">";
      }
      if(action == Action.ADD) {
        return "<"+from+"> to <"+to+">";
      }
      return null;
    }
  }
}
