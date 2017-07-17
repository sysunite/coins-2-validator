package com.sysunite.coinsweb.parser.config.factory;

import com.sysunite.coinsweb.parser.config.pojo.Graph;
import com.sysunite.coinsweb.parser.config.pojo.GraphVarImpl;

import java.io.File;
import java.util.ArrayList;

/**
 * @author bastbijl, Sysunite 2017
 */
public interface DescribeFactory {

  ArrayList<Graph> graphsInContainer(File containerFile, ArrayList<GraphVarImpl> dataGraphs, ArrayList<GraphVarImpl> schemaGraphs);
}
