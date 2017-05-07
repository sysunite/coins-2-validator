package com.sysunite.coinsweb.graphset;

import com.sysunite.coinsweb.connector.Connector;
import com.sysunite.coinsweb.filemanager.ContainerFile;
import com.sysunite.coinsweb.parser.config.Container;

/**
 * @author bastbijl, Sysunite 2017
 */
public class GraphSetFactory {
  public static ContainerGraphSet loadContainer(ContainerFile container, Connector connector, Container containerConfig) {
    return new ContainerGraphSet();
  }
}
