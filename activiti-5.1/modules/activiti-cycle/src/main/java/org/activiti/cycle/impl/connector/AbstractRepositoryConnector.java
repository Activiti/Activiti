package org.activiti.cycle.impl.connector;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.activiti.cycle.Content;
import org.activiti.cycle.RepositoryArtifact;
import org.activiti.cycle.RepositoryConnector;
import org.activiti.cycle.RepositoryNodeNotFoundException;
import org.activiti.cycle.action.ParameterizedAction;
import org.activiti.cycle.impl.conf.RepositoryConnectorConfiguration;
import org.activiti.cycle.service.CyclePluginService;
import org.activiti.cycle.service.CycleServiceFactory;

public abstract class AbstractRepositoryConnector<T extends RepositoryConnectorConfiguration> implements RepositoryConnector {

  protected Logger log = Logger.getLogger(this.getClass().getName());

  private T configuration;

  public AbstractRepositoryConnector(T configuration) {
    this.configuration = configuration;
  }

  public AbstractRepositoryConnector() {
  }

  public T getConfiguration() {
    return configuration;
  }

  public void setConfiguration(RepositoryConnectorConfiguration configuration) {
    this.configuration = (T) configuration;
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
