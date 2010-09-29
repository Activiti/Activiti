package org.activiti.cycle.impl.connector.view;

import org.activiti.cycle.RepositoryConnector;
import org.activiti.cycle.impl.conf.ConfigurationContainer;
import org.activiti.cycle.impl.conf.RepositoryConnectorConfiguration;

public class TagTreeConnectorConfiguration extends RepositoryConnectorConfiguration {

  private ConfigurationContainer configuration;

  public TagTreeConnectorConfiguration() {
  }

  /**
   * Easiest way to instantiate is to hand in a {@link ConfigurationContainer}
   * for the current user
   */
  public TagTreeConnectorConfiguration(ConfigurationContainer configuration, String tagName) {
    this.configuration = configuration;
  }

  @Override
  public RepositoryConnector createConnector() {
    return new TagTreeConnector(this);
  }
  
  public ConfigurationContainer getConfigurationContainer() {
    return configuration;
  }
}
