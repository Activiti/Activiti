package org.activiti.cycle.impl.service;

import java.util.HashSet;
import java.util.Set;

import org.activiti.cycle.CycleComponentFactory;
import org.activiti.cycle.RepositoryArtifact;
import org.activiti.cycle.RepositoryArtifactType;
import org.activiti.cycle.RepositoryConnector;
import org.activiti.cycle.action.Action;
import org.activiti.cycle.action.CreateUrlAction;
import org.activiti.cycle.action.DownloadContentAction;
import org.activiti.cycle.action.ParameterizedAction;
import org.activiti.cycle.action.RepositoryArtifactOpenLinkAction;
import org.activiti.cycle.annotations.ExcludesCycleComponents;
import org.activiti.cycle.components.RuntimeConnectorList;
import org.activiti.cycle.context.CycleApplicationContext;
import org.activiti.cycle.context.CycleSessionContext;
import org.activiti.cycle.impl.action.Actions;
import org.activiti.cycle.service.CyclePluginService;

import com.sun.xml.xsom.impl.scd.Axis;

/**
 * Default Implementation of the {@link CyclePluginService}
 * 
 * @author daniel.meyer@camunda.com
 */
public class CyclePluginServiceImpl implements CyclePluginService {

  public Set<ParameterizedAction> getParameterizedActions(RepositoryArtifactType type) {
    return CycleApplicationContext.get(Actions.class).getParameterizedActions(type);
  }

  public Set<CreateUrlAction> getCreateUrlActions(RepositoryArtifactType type) {
    return CycleApplicationContext.get(Actions.class).getCreateUrlActions(type);

  }

  public Set<DownloadContentAction> getDownloadContentActions(RepositoryArtifactType type) {
    return CycleApplicationContext.get(Actions.class).getDownloadContentActions(type);
  }

  public Set<ParameterizedAction> getParameterizedActions(RepositoryArtifact artifact) {
    Set<ParameterizedAction> actions = getParameterizedActions(artifact.getArtifactType());
    removeExcludedActions(actions);
    sortActions(actions, artifact);
    return actions;
  }

  public Set<CreateUrlAction> getCreateUrlActions(RepositoryArtifact artifact) {
    Set<CreateUrlAction> actions = getCreateUrlActions(artifact.getArtifactType());
    removeExcludedActions(actions);
    sortActions(actions, artifact);
    return actions;
  }

  public Set<DownloadContentAction> getDownloadContentActions(RepositoryArtifact artifact) {
    Set<DownloadContentAction> actions = getDownloadContentActions(artifact.getArtifactType());
    removeExcludedActions(actions);
    sortActions(actions, artifact);
    return actions;
  }

  private void sortActions(Set actions, RepositoryArtifact artifact) {
    // TODO
  }

  private void removeExcludedActions(Set actions) {
    CycleComponentFactory.removeExcludedComponents(actions);
  }

  public Set<RepositoryArtifactOpenLinkAction> getArtifactOpenLinkActions(RepositoryArtifact artifact) {
    // TODO: cache this somewhere?
    // TODO: refactor the RepositoryArtifactOpenLinkActions altogether?
    Set<RepositoryArtifactOpenLinkAction> list = new HashSet<RepositoryArtifactOpenLinkAction>();
    RepositoryConnector connector = CycleSessionContext.get(RuntimeConnectorList.class).getConnectorById(artifact.getConnectorId());
    for (CreateUrlAction action : getCreateUrlActions(artifact)) {
      // TODO: Think about id
      RepositoryArtifactOpenLinkAction link = new RepositoryArtifactOpenLinkAction("Open " + action.getId(), action.getUrl(connector, artifact));
      list.add(link);
    }
    return list;
  }
}
