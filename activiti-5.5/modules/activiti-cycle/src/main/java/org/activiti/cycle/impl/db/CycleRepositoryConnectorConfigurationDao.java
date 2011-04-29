package org.activiti.cycle.impl.db;

import java.util.List;

import org.activiti.cycle.RepositoryConnectorConfiguration;

public interface CycleRepositoryConnectorConfigurationDao {

  /**
   * Retrieves the cycle-configuration with the specified name from the
   * database.
   * 
   * @param name
   *          the name of the requested cycle configuration
   */
  public List<RepositoryConnectorConfiguration> getRepositoryConnectorConfigurationsForUser(String user);

  /**
   * Persists the provided cycle configuration.
   * 
   * @param container
   *          the cycle configuration to persist
   */
  public void saveConfiguration(RepositoryConnectorConfiguration configuration);

}
