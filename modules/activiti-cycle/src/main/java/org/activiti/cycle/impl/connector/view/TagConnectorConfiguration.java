package org.activiti.cycle.impl.connector.view;

import org.activiti.cycle.RepositoryConnector;
import org.activiti.cycle.impl.conf.ConfigurationContainer;
import org.activiti.cycle.impl.conf.RepositoryConnectorConfiguration;

public class TagConnectorConfiguration extends RepositoryConnectorConfiguration {

  private ConfigurationContainer configuration;

  public TagConnectorConfiguration() {
  }

  /**
   * Easiest way to instantiate is to hand in a {@link ConfigurationContainer}
   * for the current user
   */
  public TagConnectorConfiguration(ConfigurationContainer configuration, String tagName) {
    this.configuration = configuration;
  }

  @Override
  public RepositoryConnector createConnector() {
    return new TagConnector(this);
  }
  
  public ConfigurationContainer getConfigurationContainer() {
    return configuration;
  }
}
