package org.activiti.cycle.action;

import org.activiti.cycle.ContentRepresentation;

/**
 * Base class for download content from the webapp
 * 
 * @author ruecker
 */
public interface DownloadContentAction extends Action {

  public ContentRepresentation getContentRepresentation();
  
}
