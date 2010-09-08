package org.activiti.cycle.impl;

import org.activiti.cycle.ContentRepresentation;
import org.activiti.cycle.DownloadContentAction;

/**
 * Base class for download content from the webapp
 * 
 * @author ruecker
 */
public class DownloadContentActionImpl extends AbstractArtifactActionImpl implements DownloadContentAction {

  private static final long serialVersionUID = 1L;
  
  private final ContentRepresentation contentRepresentation;

  public DownloadContentActionImpl(ContentRepresentation contentRepresentation) {
    this.contentRepresentation = contentRepresentation;
  }

  public DownloadContentActionImpl(String actionId, ContentRepresentation contentRepresentation) {
    super(actionId);
    this.contentRepresentation = contentRepresentation;
  }
  
  public ContentRepresentation getContentRepresentation() {
    return contentRepresentation;
  }
  
}
