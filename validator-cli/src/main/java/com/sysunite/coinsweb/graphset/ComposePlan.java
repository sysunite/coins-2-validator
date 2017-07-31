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
  private List<Mapping> varMap;

  public enum Action { COPY, ADD }

  public void add(Action action, Resource from, Resource to) {
    Move move = new Move(action, from, to);
    log.info(move.toString());
    list.add(move);
  }

  public String toString() {
    String result = "";
    for(Move move : list) {
      result += "\n" + move.toString();
    }
    return result;
  }

  public List<Move> get() {
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
    public final Resource from;
    public final Resource to;
    public Move(Action action, Resource from, Resource to) {
      this.action = action;
      this.from = from;
      this.to = to;
    }
    public String toString() {
      if(action == Action.COPY) {
        return "COPY <"+from+"> to <"+to+">";
      }
      if(action == Action.ADD) {
        return "ADD <"+from+"> to <"+to+">";
      }
      return null;
    }
  }
}
