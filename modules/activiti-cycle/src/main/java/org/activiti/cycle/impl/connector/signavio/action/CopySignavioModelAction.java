package org.activiti.cycle.impl.connector.signavio.action;

import java.util.HashSet;
import java.util.Set;

import org.activiti.cycle.Content;
import org.activiti.cycle.RepositoryArtifact;
import org.activiti.cycle.RepositoryArtifactType;
import org.activiti.cycle.RepositoryConnector;
import org.activiti.cycle.annotations.CycleComponent;
import org.activiti.cycle.annotations.ExcludesCycleComponents;
import org.activiti.cycle.context.CycleApplicationContext;
import org.activiti.cycle.context.CycleContextType;
import org.activiti.cycle.impl.action.AbstractCopyBaseAction;
import org.activiti.cycle.impl.connector.signavio.provider.JsonProvider;
import org.activiti.cycle.impl.connector.signavio.repositoryartifacttype.SignavioBpmn20ArtifactType;

/**
 * This action copies a Signavio model to another repository (ideally another
 * Signavio).
 * 
 * @author bernd.ruecker@camunda.com
 */
@CycleComponent(context = CycleContextType.APPLICATION)
@ExcludesCycleComponents("org.activiti.cycle.impl.action.DefaultCopyArtifactAction")
public class CopySignavioModelAction extends AbstractCopyBaseAction {

  private static final long serialVersionUID = 1L;

  private Set<RepositoryArtifactType> types = new HashSet<RepositoryArtifactType>();

  public CopySignavioModelAction() {
    // TODO: remove when real labels are introduced in the GUI
    super("Copy Signavio Model");
    types.add(CycleApplicationContext.get(SignavioBpmn20ArtifactType.class));
  }

  protected Content getContent(RepositoryArtifact artifact, RepositoryConnector connector) {
    // use JSON representation:
    return CycleApplicationContext.get(JsonProvider.class).getContent(artifact);
  }

  public Set<RepositoryArtifactType> getArtifactTypes() {
    return types;
  }

  @Override
  protected String getName(String paramName) {
    if (paramName.endsWith(".json")) {
      return paramName;
    }
    return paramName + ".json";
  }

}
