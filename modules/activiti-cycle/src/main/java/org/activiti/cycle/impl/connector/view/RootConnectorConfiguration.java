package org.activiti.cycle.impl.connector.view;

import org.activiti.cycle.RepositoryConnector;
import org.activiti.cycle.impl.conf.ConfigurationContainer;
import org.activiti.cycle.impl.conf.RepositoryConnectorConfiguration;

public class RootConnectorConfiguration extends RepositoryConnectorConfiguration {

  private ConfigurationContainer configuration;

  public RootConnectorConfiguration() {
  }

  /**
   * Easiest way to instantiate is to hand in a {@link ConfigurationContainer}
   * for the current user
   * 
   * @param baseUrl
   *          The base URL to construct a client url (maybe used in the gui
   *          later on)
   */
  public RootConnectorConfiguration(ConfigurationContainer configuration) {
    this.configuration = configuration;
  }

  @Override
  public RepositoryConnector createConnector() {
    return new RootConnector(this);
  }
  
  public ConfigurationContainer getConfigurationContainer() {
    return configuration;
  }
}
