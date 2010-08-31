package org.activiti.cycle.impl.connector;

import java.util.logging.Logger;

import org.activiti.cycle.RepositoryConnector;
import org.activiti.cycle.impl.conf.RepositoryConnectorConfiguration;

public abstract class AbstractRepositoryConnector<T extends RepositoryConnectorConfiguration> implements RepositoryConnector {

  protected Logger log = Logger.getLogger(this.getClass().getName());

  private T configuration;

  public AbstractRepositoryConnector(T configuration) {
    this.configuration = configuration;
  }

  public T getConfiguration() {
    return configuration;
  }

}
