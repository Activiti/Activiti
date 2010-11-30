package org.activiti.cycle.impl.service;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.cycle.CycleSessionContext;
import org.activiti.cycle.RepositoryException;
import org.activiti.cycle.impl.conf.ConfigurationContainer;
import org.activiti.cycle.impl.conf.RepositoryConfigurationHandler;
import org.activiti.cycle.impl.conf.RepositoryConnectorConfiguration;
import org.activiti.cycle.impl.connector.demo.DemoConnectorConfiguration;
import org.activiti.cycle.impl.connector.fs.FileSystemConnectorConfiguration;
import org.activiti.cycle.impl.connector.signavio.SignavioConnectorConfiguration;
import org.activiti.cycle.impl.db.CycleConfigurationDao;
import org.activiti.cycle.impl.plugin.PluginFinder;
import org.activiti.cycle.service.CycleConfigurationService;
import org.activiti.cycle.service.CycleRepositoryService.RuntimeConnectorList;

/**
 * Default implementation of the {@link CycleConfigurationService}.
 * 
 * @author daniel.meyer@camunda.com
 */
public class CycleConfigurationServiceImpl implements CycleConfigurationService {

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

  protected String getCurrentUserId() {
    return CycleSessionContext.getFromCurrentContext("cuid", String.class);
  }

  public Map<String, String> getRepositoryConnectorConfiguration(String connectorConfigurationId) {
    String currentUserId = getCurrentUserId();
    // check params
    if (currentUserId == null)
      throw new IllegalArgumentException("currentUserId must not be null");
    if (connectorConfigurationId == null)
      throw new IllegalArgumentException("connectorConfigurationId must not be null");

    ConfigurationContainer configuration = getConfigurationContainer();

    List<RepositoryConnectorConfiguration> configurationList = configuration.getConnectorConfigurations();
    RepositoryConnectorConfiguration repoConfiguration = null;
    // look for the connector with id 'connectorConfigurationId'
    for (RepositoryConnectorConfiguration repositoryConnectorConfiguration : configurationList) {
      if (!repositoryConnectorConfiguration.getId().equals(connectorConfigurationId))
        continue;
      repoConfiguration = repositoryConnectorConfiguration;
    }
    if (repoConfiguration == null)
      throw new RepositoryException("Cannot find Connector with id '" + connectorConfigurationId + "' for user '" + currentUserId + "'");

    return RepositoryConfigurationHandler.getValueMap(repoConfiguration);

  }

  /**
   * Loads the configuration for this user. If no configuration exists, a demo
   * configuration is created and stored in the database.
   * 
   * @param currentUserId
   *          the id of the currently logged in user
   */
  public ConfigurationContainer getConfigurationContainer() {
    String currentUserId = getCurrentUserId();
    PluginFinder.checkPluginInitialization();
    ConfigurationContainer configuration;
    try {
      configuration = cycleConfigurationDao.getConfiguration(currentUserId);
    } catch (RepositoryException e) {
      configuration = createDefaultDemoConfiguration(currentUserId);
      cycleConfigurationDao.saveConfiguration(configuration);
    }
    return configuration;
  }

  /**
   * creates a demo-configuration for the current user
   * <p>
   * TODO: move to helper-class?
   */
  private ConfigurationContainer createDefaultDemoConfiguration(String currentUserId) {
    ConfigurationContainer configuration = new ConfigurationContainer(currentUserId);
    configuration.addRepositoryConnectorConfiguration(new DemoConnectorConfiguration("demo"));
    configuration.addRepositoryConnectorConfiguration(new SignavioConnectorConfiguration("signavio", "http://localhost:8080/activiti-modeler/"));
    configuration.addRepositoryConnectorConfiguration(new FileSystemConnectorConfiguration("files", File.listRoots()[0]));
    return configuration;
  }

  public Map<String, List<String>> getConfiguredRepositoryConnectors() {
    // retrieve the container for the current user
    ConfigurationContainer configuration = getConfigurationContainer();

    Map<String, List<String>> result = new HashMap<String, List<String>>();
    List<RepositoryConnectorConfiguration> configurationList = configuration.getConnectorConfigurations();

    // iterate the list of configured connectors
    for (RepositoryConnectorConfiguration repositoryConnectorConfiguration : configurationList) {
      String className = repositoryConnectorConfiguration.getClass().getCanonicalName();
      List<String> configuredConnectorsForThisClass = result.get(className);
      if (configuredConnectorsForThisClass == null) {
        configuredConnectorsForThisClass = new ArrayList<String>();
        result.put(className, configuredConnectorsForThisClass);
      }
      configuredConnectorsForThisClass.add(repositoryConnectorConfiguration.getId());
    }
    return result;

  }

  public Map<String, String> getConfigurationFields(String configurationClazzName) {
    return RepositoryConfigurationHandler.getConfigurationFields(configurationClazzName);
  }

  public void updateRepositoryConnectorConfiguration(String configurationClass, String configurationId, Map<String, String> values) {
    // check params
    if (configurationClass == null)
      throw new IllegalArgumentException("configurationClass must not be null");
    if (configurationId == null)
      throw new IllegalArgumentException("configurationId must not be null");
    if (values == null)
      throw new IllegalArgumentException("values must not be null");

    try {
      // Retrieve the configuration container for the current user
      ConfigurationContainer configurationContainer = getConfigurationContainer();

      // look for the configuration with the id 'configurationId'
      RepositoryConnectorConfiguration repositoryConnectorConfiguration = null;
      for (RepositoryConnectorConfiguration thisConfiguration : configurationContainer.getConnectorConfigurations()) {
        if (!configurationId.equals(thisConfiguration.getId()))
          continue;
        repositoryConnectorConfiguration = thisConfiguration;
        break;
      }

      // if we found a configuration but it is of the wrong type, throw
      // exception:
      if (repositoryConnectorConfiguration != null && !repositoryConnectorConfiguration.getClass().getCanonicalName().equals(configurationClass)) {
        throw new RepositoryException("Cannot store connectorconfiguration of type '" + configurationClass + "' with id '" + configurationId
                + "'. A connector with this id and of type '" + repositoryConnectorConfiguration.getClass().getCanonicalName().equals(configurationClass)
                + "' already exists.");
      }

      // if no configuration is found, create a new one:
      if (repositoryConnectorConfiguration == null) {
        @SuppressWarnings("unchecked")
        Class< ? extends RepositoryConnectorConfiguration> clazz = (Class< ? extends RepositoryConnectorConfiguration>) Class.forName(configurationClass);
        repositoryConnectorConfiguration = clazz.newInstance();
        // add configuration to the configuration container
        configurationContainer.addRepositoryConnectorConfiguration(repositoryConnectorConfiguration);
      }

      // update configuration:
      RepositoryConfigurationHandler.setConfigurationfields(values, repositoryConnectorConfiguration);

      // store configuration container
      cycleConfigurationDao.saveConfiguration(configurationContainer);

      // update runtime connectors:
      RuntimeConnectorList runtimeConnectorList = CycleSessionContext.getFromCurrentContext(RuntimeConnectorList.class);
      runtimeConnectorList.connectors = null;

    } catch (Exception e) {
      throw new RepositoryException("Error while storing config for user " + e.getMessage(), e);
    }
  }

  public void deleteRepositoryConnectorConfiguration(String connectorConfigurationId) {
    String currentUserId = getCurrentUserId();
    // check params
    if (connectorConfigurationId == null)
      throw new IllegalArgumentException("values must not be null");
    if (currentUserId == null)
      throw new IllegalArgumentException("currentUserId must not be null");

    try {
      // Retrieve the configuration container for the current user
      ConfigurationContainer configurationContainer = getConfigurationContainer();

      // look for the configuration with the id 'configurationId'
      RepositoryConnectorConfiguration repositoryConnectorConfiguration = null;
      for (RepositoryConnectorConfiguration thisConfiguration : configurationContainer.getConnectorConfigurations()) {
        if (!connectorConfigurationId.equals(thisConfiguration.getId()))
          continue;
        repositoryConnectorConfiguration = thisConfiguration;
        break;
      }

      // if no configuration is found, throw exception
      if (repositoryConnectorConfiguration == null) {
        throw new RepositoryException("Could not locate connectorConfiguration with id '" + connectorConfigurationId + "' for user '" + currentUserId + "'.");
      }

      // remove configuration from container:
      configurationContainer.removeRepositoryConnectorConfiguration(repositoryConnectorConfiguration);

      // store the updated configuration container
      cycleConfigurationDao.saveConfiguration(configurationContainer);

      // update runtime connectors:
      RuntimeConnectorList runtimeConnectorList = CycleSessionContext.getFromCurrentContext(RuntimeConnectorList.class);
      runtimeConnectorList.connectors = null;

    } catch (Exception e) {
      throw new RepositoryException("Error while deleting config for user " + e.getMessage(), e);
    }
  }

  public Map<String, String> getAvailableRepositoryConnectorConfiguatationClasses() {
    return PluginFinder.getInstance().getAvailableConnectorConfigurations();
  }

}
