package org.activiti.cycle.impl.connector;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.activiti.cycle.Content;
import org.activiti.cycle.RepositoryArtifact;
import org.activiti.cycle.RepositoryConnector;
import org.activiti.cycle.RepositoryNodeNotFoundException;
import org.activiti.cycle.action.ParameterizedAction;
import org.activiti.cycle.service.CyclePluginService;
import org.activiti.cycle.service.CycleServiceFactory;

public abstract class AbstractRepositoryConnector implements RepositoryConnector {

  protected Logger log = Logger.getLogger(this.getClass().getName());

  private String id;

  private String name;

  private Map<String, Object> configurationValues;

  public AbstractRepositoryConnector() {

  }

  public void startConfiguration() {
    configurationValues = new HashMap<String, Object>();
  }

  public void addConfiguration(Map<String, Object> configurationValues) {
    if (configurationValues == null) {
      throw new IllegalStateException("Call 'startConfiguration() first.");
    }
    // add all configuration values to the map. Possibly overwrites
    // configuration values which are already present.
    if (this.configurationValues != null) {
      this.configurationValues.putAll(configurationValues);
    }
  }

  public void addConfigurationEntry(String key, Object value) {
    if (this.configurationValues != null) {
      this.configurationValues.put(key, value);
    }
  }

  public void configurationFinished() {
    validateConfiguration();
  }

  /**
   * Validate the connector configuration. Throw exception if the configuration
   * is invalid.
   */
  protected abstract void validateConfiguration();

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  protected Object getConfigValue(String key) {
    return configurationValues.get(key);
  }

  @SuppressWarnings("unchecked")
  protected <T> T getConfigValue(String key, Class<T> castTo) {
    Object value = configurationValues.get(key);
    if (value == null) {
      return null;
    }
    if (value instanceof String) {
      if (String.class.equals(castTo)) {
        return (T) value;
      }
      if (Boolean.class.equals(castTo)) {
        return (T) Boolean.valueOf((String) value);
      }
      if (Integer.class.equals(castTo)) {
        return (T) Integer.valueOf((String) value);
      }
      if (Float.class.equals(castTo)) {
        return (T) Float.valueOf((String) value);
      }
      if (Long.class.equals(castTo)) {
        return (T) Long.valueOf((String) value);
      }
      throw new RuntimeException("Cannot cast connector configuration value of type 'String' for key '" + key + "' to class '" + castTo);
    } else {
      return (T) value;
    }
  }

  protected void setConfigValue(String key, String value) {
    configurationValues.put(key, value);
  }

  /**
   * As a default a connector doesn't provide any preview picture
   */
  public Content getRepositoryArtifactPreview(String artifactId) throws RepositoryNodeNotFoundException {
    return null;
  }

  /**
   * Typical basic implementation for execute a {@link ParameterizedAction} by
   * loading the {@link RepositoryArtifact}, query the action via the
   * {@link ArtifactType} and execute it by handing over "this" as parameter for
   * the connector
   */
  public void executeParameterizedAction(String artifactId, String actionId, Map<String, Object> parameters) throws Exception {
    RepositoryArtifact artifact = getRepositoryArtifact(artifactId);
    CyclePluginService pluginService = CycleServiceFactory.getCyclePluginService();
    List<ParameterizedAction> actions = pluginService.getParameterizedActions(artifact);
    for (ParameterizedAction parameterizedAction : actions) {
      if (!parameterizedAction.getId().equals(actionId)) {
        continue;
      }
      parameterizedAction.execute(this, artifact, parameters);
    }
  }

}
