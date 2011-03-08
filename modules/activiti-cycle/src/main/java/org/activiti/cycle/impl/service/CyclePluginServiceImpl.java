package org.activiti.cycle.impl.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.activiti.cycle.ContentRepresentation;
import org.activiti.cycle.CycleComponentFactory;
import org.activiti.cycle.RepositoryArtifact;
import org.activiti.cycle.RepositoryArtifactType;
import org.activiti.cycle.RepositoryConnector;
import org.activiti.cycle.action.ArtifactAwareParameterizedAction;
import org.activiti.cycle.action.CreateUrlAction;
import org.activiti.cycle.action.DownloadContentAction;
import org.activiti.cycle.action.ParameterizedAction;
import org.activiti.cycle.action.RepositoryArtifactOpenLinkAction;
import org.activiti.cycle.context.CycleApplicationContext;
import org.activiti.cycle.context.CycleSessionContext;
import org.activiti.cycle.impl.CycleComponentComparator;
import org.activiti.cycle.impl.DownloadContentActionImpl;
import org.activiti.cycle.impl.action.Actions;
import org.activiti.cycle.impl.components.RuntimeConnectorList;
import org.activiti.cycle.service.CyclePluginService;
import org.activiti.cycle.service.CycleServiceFactory;

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

  public List<DownloadContentAction> getDownloadContentActions(RepositoryArtifactType type) {
    List<DownloadContentAction> actions = new ArrayList<DownloadContentAction>();
    List<ContentRepresentation> contentRepresentations = CycleServiceFactory.getContentService().getContentRepresentations(type);

    // will be sorted according to the sort on the represenations
    for (ContentRepresentation representation : contentRepresentations) {
      if (representation.isForDownload()) {
        actions.add(new DownloadContentActionImpl(representation));
      }
    }
    return actions;
  }

  public List<ParameterizedAction> getParameterizedActions(RepositoryArtifact artifact) {
    Set<ParameterizedAction> actions = getParameterizedActions(artifact.getArtifactType());
    removeNonApplicableActions(actions, artifact);
    removeExcludedActions(actions);
    return sortActions(actions, artifact);
  }

  public List<CreateUrlAction> getCreateUrlActions(RepositoryArtifact artifact) {
    Set<CreateUrlAction> actions = getCreateUrlActions(artifact.getArtifactType());
    removeExcludedActions(actions);
    return sortActions(actions, artifact);
  }

  public List<DownloadContentAction> getDownloadContentActions(RepositoryArtifact artifact) {
    List<DownloadContentAction> actions = getDownloadContentActions(artifact.getArtifactType());
    return actions;
  }

  private void removeNonApplicableActions(Set<ParameterizedAction> actions, RepositoryArtifact forArtifact) {
    Set<ParameterizedAction> nonApplicableActions = new HashSet<ParameterizedAction>();
    for (ParameterizedAction parameterizedAction : actions) {
      if (parameterizedAction instanceof ArtifactAwareParameterizedAction) {
        ArtifactAwareParameterizedAction artifactAwareAction = (ArtifactAwareParameterizedAction) parameterizedAction;
        if (artifactAwareAction.isApplicable(forArtifact) == false) {
          nonApplicableActions.add(parameterizedAction);
        }
      }
    }
    actions.removeAll(nonApplicableActions);
  }

  @SuppressWarnings("unchecked")
  private <T> List<T> sortActions(Collection<T> actions, RepositoryArtifact artifact) {
    ArrayList<T> sortedList = new ArrayList<T>(actions);
    Collections.sort(sortedList, new CycleComponentComparator());
    return sortedList;
  }

  public ParameterizedAction getParameterizedActionById(String actionId) {
    Actions actions = CycleComponentFactory.getCycleComponentInstance(Actions.class, Actions.class);
    return actions.getParameterizedActionById(actionId);
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
      link.setWarning(action.getWarning(connector, artifact));
      list.add(link);
    }
    return list;
  }
}
