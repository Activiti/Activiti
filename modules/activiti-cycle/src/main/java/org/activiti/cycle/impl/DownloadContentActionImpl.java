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
//@CycleComponent(context = CycleContextType.APPLICATION)
public class DownloadContentActionImpl extends AbstractArtifactActionImpl implements DownloadContentAction {

  private static final long serialVersionUID = 1L;

  private final ContentRepresentation contentRepresentation;

  // public DownloadContentActionImpl(ContentRepresentation
  // contentRepresentation) {
  // this.contentRepresentation = contentRepresentation;
  // }

  public DownloadContentActionImpl() {
    super("Download");
    this.contentRepresentation = null;
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
