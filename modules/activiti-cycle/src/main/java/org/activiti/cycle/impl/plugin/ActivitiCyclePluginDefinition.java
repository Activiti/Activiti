package org.activiti.cycle.impl.plugin;

import java.util.List;

import org.activiti.cycle.ArtifactType;
import org.activiti.cycle.impl.conf.RepositoryConnectorConfiguration;

/**
 * Interface for any plugin provider. Each plugin needs a definition class
 * implementing that interface to provide informations about the internals
 * 
 * @author ruecker
 */
public interface ActivitiCyclePluginDefinition {
  
  public void addArtifactTypes(List<ArtifactType> types);
  
  /**
   * TODO: Move to annotation?
   */
  public Class< ? extends RepositoryConnectorConfiguration> getRepositoryConnectorConfigurationType();
  
}
