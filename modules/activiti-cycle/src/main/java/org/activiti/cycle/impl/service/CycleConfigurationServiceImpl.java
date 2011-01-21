package org.activiti.cycle.impl.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.text.html.parser.Entity;

import org.activiti.cycle.CycleComponentFactory;
import org.activiti.cycle.RepositoryConnector;
import org.activiti.cycle.RepositoryConnectorConfiguration;
import org.activiti.cycle.RepositoryException;
import org.activiti.cycle.context.CycleSessionContext;
import org.activiti.cycle.impl.components.RuntimeConnectorList;
import org.activiti.cycle.impl.conf.RepositoryConfigurationHandler;
import org.activiti.cycle.impl.conf.RepositoryConnectorConfigurationImpl;
import org.activiti.cycle.impl.db.CycleConfigurationDao;
import org.activiti.cycle.impl.db.CycleRepositoryConnectorConfigurationDao;
import org.activiti.cycle.impl.db.entity.CycleConfigEntity;
import org.activiti.cycle.service.CycleConfigurationService;

/**
 * Default implementation of the {@link CycleConfigurationService}.
 * 
 * @author daniel.meyer@camunda.com
 */
public class CycleConfigurationServiceImpl implements CycleConfigurationService {

  private CycleRepositoryConnectorConfigurationDao cycleRepositoryConnectorConfigurationDao;

  private CycleConfigurationDao cycleConfigurationDao;

  private CycleServiceConfiguration cycleServiceConfiguration;

  public CycleConfigurationServiceImpl() {
  }

  /**
   * perform initialization after dependencies are set.
   */
  public void initialize() {
    // perform initialization
  }

  public void setCycleServiceConfiguration(CycleServiceConfiguration cycleServiceConfiguration) {
    this.cycleServiceConfiguration = cycleServiceConfiguration;
  }

  public void setCycleConfigurationDao(CycleConfigurationDao cycleConfigurationDao) {
    this.cycleConfigurationDao = cycleConfigurationDao;
  }

  public void setCycleRepositoryConnectorConfigurationDao(CycleRepositoryConnectorConfigurationDao cycleRepositoryConnectorConfigurationDao) {
    this.cycleRepositoryConnectorConfigurationDao = cycleRepositoryConnectorConfigurationDao;
  }

  protected String getCurrentUserId() {
    return CycleSessionContext.get("cuid", String.class);
  }

  public Map<String, String> getRepositoryConnectorConfiguration(String connectorConfigurationId) {

    if (connectorConfigurationId == null) {
      throw new IllegalArgumentException("connectorConfigurationId must not be null");
    }

    String currentUserId = getCurrentUserId();
    // check params
    if (currentUserId == null) {
      throw new IllegalArgumentException("currentUserId must not be null. Set 'cuid' in Cycle Session Context.");
    }

    List<RepositoryConnectorConfiguration> configurations = getConnectorConfigurations();
    RepositoryConnectorConfiguration configuration = null;
    for (RepositoryConnectorConfiguration repositoryConnectorConfiguration : configurations) {
      if (currentUserId.equals(repositoryConnectorConfiguration.getUserId())
              && connectorConfigurationId.equals(repositoryConnectorConfiguration.getConnectorId())) {
        configuration = repositoryConnectorConfiguration;
      }
    }

    if (configuration == null) {
      throw new RuntimeException("Could not find connector configuration with id '" + connectorConfigurationId + "' for user '" + currentUserId + "'.");
    }

    return RepositoryConfigurationHandler.getValueMap(configuration, configuration.getPluginId());
  }

  public List<RepositoryConnectorConfiguration> getConnectorConfigurations() {
    String currentUserId = getCurrentUserId();
    // check params
    if (currentUserId == null) {
      throw new IllegalArgumentException("currentUserId must not be null. Set 'cuid' in Cycle Session Context.");
    }
    List<RepositoryConnectorConfiguration> configurations = cycleRepositoryConnectorConfigurationDao.getRepositoryConnectorConfigurationsForUser(currentUserId);
    return configurations;
  }

  public Map<String, List<String>> getConfiguredRepositoryConnectors() {
    String currentUserId = getCurrentUserId();
    Map<String, List<String>> result = new HashMap<String, List<String>>();
    List<RepositoryConnectorConfiguration> configurationList = getConnectorConfigurations();

    // iterate the list of configured connectors
    for (RepositoryConnectorConfiguration repositoryConnectorConfiguration : configurationList) {
      String pluginId = repositoryConnectorConfiguration.getPluginId();
      if (currentUserId.equals(repositoryConnectorConfiguration.getUserId())) {
        List<String> configuredConnectorsForThisPluginId = result.get(pluginId);
        if (configuredConnectorsForThisPluginId == null) {
          configuredConnectorsForThisPluginId = new ArrayList<String>();
          result.put(pluginId, configuredConnectorsForThisPluginId);
        }
        configuredConnectorsForThisPluginId.add(repositoryConnectorConfiguration.getConnectorId());
      }
    }
    return result;

  }

  public Map<String, String> getConfigurationFields(String pluginId) {
    return RepositoryConfigurationHandler.getConfigurationFields(pluginId);
  }

  public void updateRepositoryConnectorConfiguration(String configurationClass, String configurationId, Map<String, String> values) {
    // check params
    if (configurationClass == null) {
      throw new IllegalArgumentException("configurationClass must not be null");
    }
    if (configurationId == null) {
      throw new IllegalArgumentException("configurationId must not be null");
    }
    if (values == null) {
      throw new IllegalArgumentException("values must not be null");
    }
    String currentUserId = getCurrentUserId();

    try {

      List<RepositoryConnectorConfiguration> configurations = getConnectorConfigurations();
      // look for the configuration with the id 'configurationId'
      RepositoryConnectorConfiguration repositoryConnectorConfiguration = null;
      for (RepositoryConnectorConfiguration thisConfiguration : configurations) {
        if (!configurationId.equals(thisConfiguration.getConnectorId())) {
          continue;
        }
        if (!currentUserId.equals(thisConfiguration.getUserId())) {
          continue;
        }

        repositoryConnectorConfiguration = thisConfiguration;
        break;
      }

      // if no configuration is found, create a new one:
      if (repositoryConnectorConfiguration == null) {
        RepositoryConnectorConfigurationImpl newConfig = new RepositoryConnectorConfigurationImpl();
        newConfig.setUserId(currentUserId);
        newConfig.setPluginId(configurationClass);
        repositoryConnectorConfiguration = newConfig;
      }

      // update configuration:
      RepositoryConfigurationHandler.setConfigurationfields(values, repositoryConnectorConfiguration);

      // store configuration container
      cycleRepositoryConnectorConfigurationDao.saveConfiguration(repositoryConnectorConfiguration);

      // update runtime connectors:
      RuntimeConnectorList runtimeConnectorList = CycleSessionContext.get(RuntimeConnectorList.class);
      runtimeConnectorList.discardConnectors();

    } catch (Exception e) {
      throw new RepositoryException("Error while storing config for user " + e.getMessage(), e);
    }
  }

  public void deleteRepositoryConnectorConfiguration(String connectorConfigurationId) {
    throw new RuntimeException("Deleting Configurations is bot yet implemented");
  }

  public Map<String, String> getAvailableRepositoryConnectorConfiguatationClasses() {
    String[] availableConnectorClasses = CycleComponentFactory.getAvailableComponentsForType(RepositoryConnector.class);
    Map<String, String> result = new HashMap<String, String>();
    for (String componentName : availableConnectorClasses) {
      String name = componentName;
      if (componentName.contains(".")) {
        name = componentName.substring(componentName.lastIndexOf(".") + 1);
      }
      result.put(name, componentName);
    }
    return result;
  }

  public String getConfigurationValue(String groupId, String key) {
    CycleConfigEntity entity = cycleConfigurationDao.selectCycleConfigByGroupAndKey(groupId, key);
    if (entity == null) {
      return null;
    }
    return entity.getValue();
  }

  public void setConfigurationValue(String groupId, String key, String value) {
    CycleConfigEntity entity = cycleConfigurationDao.selectCycleConfigByGroupAndKey(groupId, key);
    if (entity == null) {
      entity = new CycleConfigEntity();
    }
    entity.setValue(value);
    cycleConfigurationDao.saveCycleConfig(entity);
  }

  public String getConfigurationValue(String groupId, String key, String defaultValue) {
    String value = getConfigurationValue(groupId, key);
    if (value == null) {
      return defaultValue;
    }
    return value;
  }

}
