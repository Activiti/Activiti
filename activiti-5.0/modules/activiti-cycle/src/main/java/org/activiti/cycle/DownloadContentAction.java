package org.activiti.cycle;

/**
 * Base class for download content from the webapp
 * 
 * @author ruecker
 */
public interface DownloadContentAction {

  public String getId();

  public ContentRepresentation getContentRepresentation();
  
}
