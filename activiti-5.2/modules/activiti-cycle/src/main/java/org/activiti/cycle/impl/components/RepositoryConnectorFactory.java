package org.activiti.cycle.impl.components;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.activiti.cycle.CycleComponentFactory;
import org.activiti.cycle.RepositoryConnector;
import org.activiti.cycle.RepositoryConnectorConfiguration;
import org.activiti.cycle.annotations.CycleComponent;
import org.activiti.cycle.context.CycleContextType;
import org.activiti.cycle.service.CycleConfigurationService;
import org.activiti.cycle.service.CycleServiceFactory;

/**
 * 
 * @author daniel.meyer@camunda.com
 */
@CycleComponent(context = CycleContextType.APPLICATION)
public class RepositoryConnectorFactory {

  private CycleConfigurationService configurationService = CycleServiceFactory.getConfigurationService();

  public List<RepositoryConnector> getConnectors() {
    List<RepositoryConnector> resultList = new ArrayList<RepositoryConnector>();

    List<RepositoryConnectorConfiguration> configurations = configurationService.getConnectorConfigurations();

    Map<String, List<RepositoryConnectorConfiguration>> configurationMap = new HashMap<String, List<RepositoryConnectorConfiguration>>();

    for (RepositoryConnectorConfiguration repositoryConnectorConfiguration : configurations) {
      List<RepositoryConnectorConfiguration> configurationListForThisInstanceId = configurationMap.get(repositoryConnectorConfiguration.getConnectorId());
      if (configurationListForThisInstanceId == null) {
        configurationListForThisInstanceId = new ArrayList<RepositoryConnectorConfiguration>();
        configurationMap.put(repositoryConnectorConfiguration.getConnectorId(), configurationListForThisInstanceId);
      }
      configurationListForThisInstanceId.add(repositoryConnectorConfiguration);
    }
    validateConfiguration(configurationMap);
    // create connectors and set values:
    for (Entry<String, List<RepositoryConnectorConfiguration>> entry : configurationMap.entrySet()) {
      String connectorId = entry.getKey();
      String pluginId = entry.getValue().get(0).getPluginId();
      RepositoryConnector connector;
      try {
        connector = (RepositoryConnector) CycleComponentFactory.getCycleComponentInstance(pluginId);
      } catch (Exception e) {
        throw new RuntimeException("Could not initialize connector for pluginId '" + pluginId + "': " + e.getMessage(), e);
      }
      // set the connectorId
      connector.setId(connectorId);
      connector.setName(entry.getValue().get(0).getInstanceName());
      connector.startConfiguration();
      for (RepositoryConnectorConfiguration repositoryConnectorConfiguration : entry.getValue()) {
        connector.addConfiguration(repositoryConnectorConfiguration.getConfigurationValues());
      }
      connector.configurationFinished();
      resultList.add(connector);
    }
    return resultList;
  }

  private void validateConfiguration(Map<String, List<RepositoryConnectorConfiguration>> configurationMap) {
    String pluginId = null;
    for (List<RepositoryConnectorConfiguration> configurationList : configurationMap.values()) {
      for (int i = 0; i < configurationList.size(); i++) {
        if (i == 0) {
          pluginId = configurationList.get(0).getPluginId();
        } else {
          if (configurationList.get(i).getPluginId() == null || !configurationList.get(i).getPluginId().equals(pluginId)) {
            throw new RuntimeException("The pluginId of all connectors with the same 'connectorId' must be the same for a given user.");
          }
        }
      }
    }

  }

}
