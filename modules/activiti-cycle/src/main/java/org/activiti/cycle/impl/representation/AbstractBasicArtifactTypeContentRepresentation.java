package org.activiti.cycle.impl.representation;

import org.activiti.cycle.Content;
import org.activiti.cycle.ContentRepresentation;
import org.activiti.cycle.MimeType;
import org.activiti.cycle.RepositoryArtifact;
import org.activiti.cycle.RepositoryArtifactType;
import org.activiti.cycle.context.CycleApplicationContext;
import org.activiti.cycle.impl.artifacttype.BasicRepositoryArtifactType;
import org.activiti.cycle.service.CycleServiceFactory;

/**
 * Default {@link ContentRepresentation} for {@link BasicRepositoryArtifactType}
 * s.
 * 
 * @author daniel.meyer@camunda.com
 */
public abstract class AbstractBasicArtifactTypeContentRepresentation implements ContentRepresentation {

  private static final long serialVersionUID = 1L;

  public String getId() {
    return getRepresentationMimeType().getName();
  }

  public Content getContent(RepositoryArtifact artifact) {
    Content content = CycleServiceFactory.getRepositoryService().getContent(artifact.getConnectorId(), artifact.getNodeId());
    return content;
  }

  protected abstract Class< ? extends MimeType> getMimeType();

  public RepositoryArtifactType getRepositoryArtifactType() {
    return new BasicRepositoryArtifactType(getRepresentationMimeType());
  }

  public MimeType getRepresentationMimeType() {
    return CycleApplicationContext.get(getMimeType());
  }

  public boolean isDefaultRepresentation() {
    return true;
  }

}
