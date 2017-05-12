package com.sysunite.coinsweb.steps;

import com.sysunite.coinsweb.filemanager.ContainerFile;
import com.sysunite.coinsweb.graphset.ContainerGraphSet;

import java.util.Map;

/**
 * @author bastbijl, Sysunite 2017
 */
public interface ValidationStep {
  Map<String, Object> execute(ContainerFile container, ContainerGraphSet graphSet);
}
