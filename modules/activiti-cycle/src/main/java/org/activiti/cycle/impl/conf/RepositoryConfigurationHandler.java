package org.activiti.cycle.impl.conf;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.activiti.cycle.CycleComponentFactory;
import org.activiti.cycle.RepositoryConnector;
import org.activiti.cycle.RepositoryConnectorConfiguration;

/**
 * Helper-class for handling {@link RepositoryConnectorConfiguration}s in a
 * generic way. Capabilities:
 * <ul>
 * <li>extracting available fields from a configuration class
 * {@link #getConfigurationFields(String)}</li>
 * <li>extracting the current values for a configuration instance
 * {@link #getValueMap(RepositoryConnectorConfiguration)}</li>
 * <li>setting new values for a given configuration instance
 * {@link #setConfigurationfields(Map, Object)}</li>
 * </ul>
 * 
 * @author daniel.meyer@camunda.com
 */
public class RepositoryConfigurationHandler {

  static Logger log = Logger.getLogger(RepositoryConfigurationHandler.class.getCanonicalName());

  private static String KEY_NAME = "name";
  private static String KEY_ID = "id";

  /**
   * Sets the values of 'fields' on 'repositoryConnectorInstance'.
   * 
   * @param fields
   *          a map of configuration values where the keys are field-names and
   *          the values are the configuration values.
   * @param repositoryConnectorInstance
   *          the {@link RepositoryConnectorConfiguration} instance to set the
   *          values on.
   */
  public static void setConfigurationfields(Map<String, String> fields, RepositoryConnectorConfiguration configuration) {
    try {

      RepositoryConnectorConfigurationImpl configurationImpl = (RepositoryConnectorConfigurationImpl) configuration;
      configurationImpl.setConnectorId(fields.get(KEY_ID));
      configurationImpl.setInstanceName(fields.get(KEY_NAME));
      fields.remove(KEY_ID);
      fields.remove(KEY_NAME);
      
      configuration.getConfigurationValues().putAll(fields);

    } catch (Exception e) {
      log.log(Level.WARNING, "could not set fieldvalues " + e.getMessage(), e);
      throw new RuntimeException(e);
    }
  }

  /**
   * @see CycleService#getConfigurationFields(String)
   */
  public static Map<String, String> getConfigurationFields(String connectorPluginName) {
    Map<String, String> result = new HashMap<String, String>();
    RepositoryConnector connector = CycleComponentFactory.getCycleComponentInstance(connectorPluginName, RepositoryConnector.class);
    for (String key : connector.getConfigurationKeys()) {
      result.put(key, "");
    }
    result.put(KEY_NAME, "");
    result.put(KEY_ID, "");
    return result;
  }

  /**
   * @see CycleService#getRepositoryConnectorConfiguration(String, String)
   */
  public static Map<String, String> getValueMap(RepositoryConnectorConfiguration repositoryConnectorConfiguration, String connectorPluginName) {
    try {
      Map<String, String> result = getConfigurationFields(connectorPluginName);
      for (Entry<String, Object> entry : repositoryConnectorConfiguration.getConfigurationValues().entrySet()) {
        if (entry.getValue() != null) {
          result.put(entry.getKey(), entry.getValue().toString());
        } else {
          result.put(entry.getKey(), "");
        }
      }
      result.put(KEY_NAME, repositoryConnectorConfiguration.getInstanceName());
      result.put(KEY_ID, repositoryConnectorConfiguration.getConnectorId());
      return result;
    } catch (Exception e) {
      log.log(Level.WARNING, "could not set fieldvalues " + e.getMessage(), e);
      throw new RuntimeException(e);
    }
  }
}
