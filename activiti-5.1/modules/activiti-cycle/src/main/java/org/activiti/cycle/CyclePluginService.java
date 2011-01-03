package org.activiti.cycle;

import java.util.List;

import org.activiti.cycle.impl.conf.RepositoryConnectorConfiguration;

/**
 * 
 * @author ruecker
 */
public interface CyclePluginService {
  
  public List<Class< ? extends RepositoryConnectorConfiguration>> getKnownConnectorConfigurationTypes();
  
  public List<String> getPossibleArtifactTypeIds(String connectorId);
}
