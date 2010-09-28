package org.activiti.cycle.impl.connector.demo;

import org.activiti.cycle.RepositoryConnector;
import org.activiti.cycle.impl.conf.RepositoryConnectorConfiguration;

public class DemoConnectorConfiguration extends RepositoryConnectorConfiguration {
  
  public DemoConnectorConfiguration(String name) {
    setName(name);
  }

  @Override
  public RepositoryConnector createConnector() {
    return new DemoConnector(this);
  }

}
