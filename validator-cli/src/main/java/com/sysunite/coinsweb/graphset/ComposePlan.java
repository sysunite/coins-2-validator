package com.sysunite.coinsweb.graphset;

import com.sysunite.coinsweb.parser.config.pojo.Mapping;
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
  private List<Mapping> varMap;

  private int pointer = 0;


  private boolean failed = false;

  public enum Action { COPY, ADD }

  public void addToStart(Action action, GraphVar from, Resource to) {
    Move move = new Move(action, from, to);
    list.add(pointer, move);
    unFinished.add(move);
  }

  public void addToStart(Action action, Resource from, Resource to) {
    Move move = new Move(action, from, to);
    list.add(pointer, move);
  }

  public void add(Action action, Resource from, Resource to) {
    Move move = new Move(action, from, to);
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

  public void setFailed() {
    failed = true;
  }
  public boolean isFailed() {
    return  failed;
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

  public List<Mapping> getVarMap() {
    return varMap;
  }

  public void setVarMap(List<Mapping> varMap) {
    this.varMap = varMap;
  }

  public class Move {
    public final Action action;
    public Resource from;
    public GraphVar fromPending;
    public final Resource to;
    public Move(Action action, Resource from, Resource to) {
      this.action = action;
      this.from = from;
      this.to = to;
    }
    public Move(Action action, GraphVar fromPending, Resource to) {
      this.action = action;
      this.fromPending = fromPending;
      this.to = to;
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
