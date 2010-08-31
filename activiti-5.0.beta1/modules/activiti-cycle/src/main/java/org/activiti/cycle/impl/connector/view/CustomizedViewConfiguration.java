package org.activiti.cycle.impl.connector.view;

import org.activiti.cycle.RepositoryConnector;
import org.activiti.cycle.impl.conf.ConfigurationContainer;
import org.activiti.cycle.impl.conf.RepositoryConnectorConfiguration;

public class CustomizedViewConfiguration extends RepositoryConnectorConfiguration {

  private String baseUrl;
  private ConfigurationContainer configuration;

  public CustomizedViewConfiguration() {
  }

  /**
   * Easiest way to instantiate is to hand in a {@link ConfigurationContainer}
   * for the current user
   * 
   * @param baseUrl
   *          The base URL to construct a client url (maybe used in the gui
   *          later on)
   */
  public CustomizedViewConfiguration(String baseUrl, ConfigurationContainer configuration) {
    if (baseUrl.endsWith("/")) {
      this.baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
    } else {
      this.baseUrl = baseUrl;
    }
    this.configuration = configuration;
  }

  @Override
  public RepositoryConnector createConnector() {
    return new CustomizedViewConnector(this);
  }

  public String getBaseUrl() {
    return baseUrl;
  }

  // public String getBaseUrlWithoutSlashAtTheEnd() {
  // if (baseUrl.endsWith("/")) {
  // return baseUrl.substring(0, baseUrl.length() - 1);
  // } else {
  // return baseUrl;
  // }
  // }
  
  public ConfigurationContainer getConfigurationContainer() {
    return configuration;
  }
}
