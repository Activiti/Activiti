package org.activiti.cycle.impl.db;

import org.activiti.cycle.impl.conf.ConfigurationContainer;


public interface CycleConfigurationDao {

  /**
   * Retrieves the cycle-configuration with the specified name from the
   * database.
   * 
   * @param name the name of the requested cycle configuration
   */
  public ConfigurationContainer getConfiguration(String name);

  /**
   * Persists the provided cycle configuration.
   * 
   * @param container the cycle configuration to persist
   */
  public void saveConfiguration(ConfigurationContainer container);

}
