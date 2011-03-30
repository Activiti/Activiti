package org.activiti.cycle.impl.db.entity;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.activiti.cycle.RepositoryConnectorConfiguration;
import org.activiti.cycle.impl.conf.RepositoryConnectorConfigurationImpl;
import org.activiti.cycle.impl.util.XmlSerializer;
import org.activiti.engine.impl.db.PersistentObject;

/**
 * Entity for cycle {@link RepositoryConnectorConfiguration}-Entries.
 * 
 * @author daniel.meyer@camunda.com
 */
public class CycleRepositoryConnectorConfigurationEntity implements Serializable, PersistentObject {

  private static final long serialVersionUID = 1L;

  protected String id;
  protected String pluginId;
  protected String instanceId;
  protected String instanceName;
  protected String user;
  protected String group;
  protected Map<String, Object> values;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public Object getPersistentState() {
    Map<String, Object> persistentState = new HashMap<String, Object>();
    persistentState.put("id", id);
    persistentState.put("pluginId", pluginId);
    persistentState.put("instanceId", instanceId);
    persistentState.put("instanceName", instanceName);
    persistentState.put("user", user);
    persistentState.put("group", group);
    persistentState.put("values", serialize(values));
    return persistentState;
  }

  private Object serialize(Map<String, Object> values) {
    return XmlSerializer.serializeObject(values);
  }

  public String getPluginId() {
    return pluginId;
  }

  public void setPluginId(String pluginId) {
    this.pluginId = pluginId;
  }

  public String getInstanceId() {
    return instanceId;
  }

  public void setInstanceId(String instanceId) {
    this.instanceId = instanceId;
  }

  public String getUser() {
    return user;
  }

  public void setUser(String user) {
    this.user = user;
  }

  public String getGroup() {
    return group;
  }

  public void setGroup(String group) {
    this.group = group;
  }

  public Map<String, Object> getValueMap() {
    return values;
  }

  public String getValues() {
    return (String) serialize(values);
  }

  public String getInstanceName() {
    return instanceName;
  }

  public void setInstanceName(String instanceName) {
    this.instanceName = instanceName;
  }

  @SuppressWarnings("unchecked")
  public void setValues(String values) {
    this.values = (Map<String, Object>) XmlSerializer.unSerializeObject(values);
  }

  public void setValueMap(Map<String, Object> values) {
    this.values = values;
  }

  public RepositoryConnectorConfiguration asRepositoryConnectorConfiguration() {
    RepositoryConnectorConfigurationImpl configuration = new RepositoryConnectorConfigurationImpl();
    configuration.setConfigurationValues(values);
    configuration.setConnectorId(instanceId);
    configuration.setEntityId(id);
    configuration.setGroupId(group);
    configuration.setPluginId(pluginId);
    configuration.setInstanceName(instanceName);
    configuration.setUserId(user);
    return configuration;
  }

  public static CycleRepositoryConnectorConfigurationEntity fromRepositoryConnectorConfiguration(RepositoryConnectorConfigurationImpl config) {
    CycleRepositoryConnectorConfigurationEntity entity = new CycleRepositoryConnectorConfigurationEntity();
    entity.setId(config.getEntityId());
    entity.setGroup(config.getGroupId());
    entity.setUser(config.getUserId());
    entity.setValueMap(config.getConfigurationValues());
    entity.setInstanceId(config.getConnectorId());
    entity.setPluginId(config.getPluginId());
    entity.setInstanceName(config.getInstanceName());
    return entity;
  }
}
