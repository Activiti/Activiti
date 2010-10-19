package org.activiti.cycle;

import java.net.URL;

/**
 * Action to open an external URl for an {@link RepositoryArtifact}, for example
 * opening the Signavio modeler for a BPMN model
 * 
 * @author ruecker
 */
public class RepositoryArtifactOpenLinkAction {

  private String id;
  private URL url;

  public RepositoryArtifactOpenLinkAction(String id, URL url) {
    this.id = id;
    this.url = url;
  }

  public String getId() {
    return id;
  }

  public URL getUrl() {
    return url;
  }

}
