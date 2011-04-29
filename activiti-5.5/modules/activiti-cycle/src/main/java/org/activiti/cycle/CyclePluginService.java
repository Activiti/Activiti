package org.activiti.cycle;

import java.util.List;

/**
 * 
 * @author ruecker
 */
public interface CyclePluginService {

  public List<Class< ? extends RepositoryConnectorConfiguration>> getKnownConnectorConfigurationTypes();

  public List<String> getPossibleArtifactTypeIds(String connectorId);
}
