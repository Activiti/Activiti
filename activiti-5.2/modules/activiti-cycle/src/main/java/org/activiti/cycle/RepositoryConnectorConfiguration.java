package org.activiti.cycle;

import java.util.Map;

import org.activiti.cycle.annotations.CycleComponent;

/**
 * Configuration for {@link RepositoryConnector}s. Can be assigned either to a
 * single user or a user-group.
 * 
 * @author daniel.meyer@camunda.com
 */
public interface RepositoryConnectorConfiguration {

  /**
   * @return the plugin-name (see {@link CycleComponent#name()}) of the
   *         {@link RepositoryConnector} configured by this configuration
   *         object.
   * 
   */
  public String getPluginId();
  
  /**
   * The name of the connector instance
   */
  public String getInstanceName();

  /**
   * @return The configuration values for this
   *         {@link RepositoryConnectorInstanceConfiguration}
   */
  public Map<String, Object> getConfigurationValues();

  /**
   * @return get a single configuration value
   */
  public Object getConfigurationValue(String name);

  /**
   * @return set a single configuration value
   */
  public void setConfigurationValue(String name, Object value);

  /**
   * If a configuration returns a connector-id, it is a configuration for a
   * specific connector-instance.
   * 
   */
  public String getConnectorId();

  /**
   * Returns the userId for this
   * {@link RepositoryConnectorInstanceConfiguration}. If this method returns a
   * userId, it is a configuration for a specific connector-instance and a
   * specific user. If this method returns null, the configuration is an
   * abstract configuration (i.e. incomplete) for a group of connector
   * instances.
   * 
   */
  public String getUserId();

  /**
   * Returns an Id for the group this connector configuration is associated to.
   */
  public String getGroupId();

}
