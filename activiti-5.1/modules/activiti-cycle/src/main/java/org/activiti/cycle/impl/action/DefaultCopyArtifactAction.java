package org.activiti.cycle.impl.action;

import java.util.Set;

import org.activiti.cycle.Content;
import org.activiti.cycle.ContentRepresentation;
import org.activiti.cycle.RepositoryArtifact;
import org.activiti.cycle.RepositoryArtifactType;
import org.activiti.cycle.RepositoryConnector;
import org.activiti.cycle.annotations.CycleComponent;
import org.activiti.cycle.context.CycleContextType;

/**
 * Default copy action applicable to all {@link RepositoryArtifactType}s. Uses
 * the {@link ContentRepresentation} as returned by the
 * {@link RepositoryConnector#getContent(String)}-method.
 * 
 * @author daniel.meyer@camunda.com
 */
@CycleComponent(context = CycleContextType.APPLICATION)
public class DefaultCopyArtifactAction extends AbstractCopyBaseAction {

  private static final long serialVersionUID = 1L;

  public final static String NAME = "Copy Artifact";

  public DefaultCopyArtifactAction() {
    super(NAME);
  }

  public Set<RepositoryArtifactType> getArtifactTypes() {
    // assume this is applicable to all artifact types = 'null'
    return null;
  }

  protected Content getContent(RepositoryArtifact artifact, RepositoryConnector connector) {
    // return the content as provided by the connector
    return connector.getContent(artifact.getNodeId());
  }
}
