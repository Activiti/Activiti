package org.activiti.cycle;

import java.net.URL;

/**
 * 
 * @author ruecker
 */
public class RepositoryArtifactOutgoingLink {

  private String id;
  private URL url;

  public RepositoryArtifactOutgoingLink(String id, URL url) {
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
