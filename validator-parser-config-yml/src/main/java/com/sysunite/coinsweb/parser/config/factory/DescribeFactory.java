package com.sysunite.coinsweb.parser.config.factory;

import com.sysunite.coinsweb.parser.config.pojo.Graph;

import java.io.File;
import java.util.ArrayList;

/**
 * @author bastbijl, Sysunite 2017
 */
public interface DescribeFactory {

  ArrayList<Graph> graphsInContainer(File containerFile);
}
