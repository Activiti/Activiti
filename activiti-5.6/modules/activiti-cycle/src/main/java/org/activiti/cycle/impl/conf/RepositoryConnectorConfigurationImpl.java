package org.activiti.cycle.impl.conf;

import java.util.HashMap;
import java.util.Map;

import org.activiti.cycle.RepositoryConnectorConfiguration;

/**
 * Default {@link RepositoryConnectorConfiguration} implementation
 * 
 * @author daniel.meyer@camunda.com
 */
public class RepositoryConnectorConfigurationImpl implements RepositoryConnectorConfiguration {

  private String entityId;

  private String pluginId;

  private Map<String, Object> configurationValues = new HashMap<String, Object>();

  private String connectorId;

  private String userId;

  private String instanceName;
  
  private String groupId;

  // default constructor
  public RepositoryConnectorConfigurationImpl() {
  }

  public String getPluginId() {
    return pluginId;
  }

  public Map<String, Object> getConfigurationValues() {
    return configurationValues;
  }

  public Object getConfigurationValue(String name) {
    return configurationValues.get(name);
  }

  public void setConfigurationValue(String name, Object value) {
    configurationValues.put(name, value);
  }

  public String getConnectorId() {
    return connectorId;
  }

  public String getUserId() {
    return userId;
  }

  public String getGroupId() {
    return groupId;
  }

  public void setConfigurationValues(Map<String, Object> configurationValues) {
    this.configurationValues = configurationValues;
  }

  public void setConnectorId(String connectorId) {
    this.connectorId = connectorId;
  }

  public void setGroupId(String groupId) {
    this.groupId = groupId;
    this.userId = null;
  }

  public void setPluginId(String pluginId) {
    this.pluginId = pluginId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
    this.groupId = null;
  }

  public String getEntityId() {
    return entityId;
  }

  public void setEntityId(String entityId) {
    this.entityId = entityId;
  }

  
  public String getInstanceName() {
    return instanceName;
  }

  
  public void setInstanceName(String instanceName) {
    this.instanceName = instanceName;
  }

  
}
