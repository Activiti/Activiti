package org.activiti.cycle.impl;

import java.util.Set;

import org.activiti.cycle.ContentRepresentation;
import org.activiti.cycle.RepositoryArtifactType;
import org.activiti.cycle.action.DownloadContentAction;

/**
 * Base class for download content from the webapp
 * 
 * @author ruecker
 */
// not a @CycleComponent, instantiated by the cycle plugin service for each
// ContentRepresentation
public class DownloadContentActionImpl extends AbstractArtifactActionImpl implements DownloadContentAction {

  private static final long serialVersionUID = 1L;

  private final ContentRepresentation contentRepresentation;

  public DownloadContentActionImpl(ContentRepresentation contentRepresentation) {
    this("Download " + contentRepresentation.getId(), contentRepresentation);
  }

  public DownloadContentActionImpl(String actionId, ContentRepresentation contentRepresentation) {
    super(actionId);
    this.contentRepresentation = contentRepresentation;
  }

  public ContentRepresentation getContentRepresentation() {
    return contentRepresentation;
  }

  public Set<RepositoryArtifactType> getArtifactTypes() {
    // null = all types
    return null;
  }

}
