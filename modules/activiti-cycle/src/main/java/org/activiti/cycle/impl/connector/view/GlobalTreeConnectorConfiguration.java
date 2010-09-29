package org.activiti.cycle.impl.connector.view;

import org.activiti.cycle.RepositoryConnector;
import org.activiti.cycle.impl.conf.ConfigurationContainer;
import org.activiti.cycle.impl.conf.RepositoryConnectorConfiguration;

public class GlobalTreeConnectorConfiguration extends RepositoryConnectorConfiguration {

  private ConfigurationContainer configuration;

  public GlobalTreeConnectorConfiguration() {
  }

  /**
   * Easiest way to instantiate is to hand in a {@link ConfigurationContainer}
   * for the current user
   * 
   * @param baseUrl
   *          The base URL to construct a client url (maybe used in the gui
   *          later on)
   */
  public GlobalTreeConnectorConfiguration(ConfigurationContainer configuration) {
    this.configuration = configuration;
  }

  @Override
  public RepositoryConnector createConnector() {
    return new GlobalTreeConnector(this);
  }
  
  public ConfigurationContainer getConfigurationContainer() {
    return configuration;
  }
}
