package org.activiti.impl.cycle.connect.api;

import java.io.Serializable;

/**
 * Data structure for link to content, including the URL to the content, the
 * type (see {@link ContentRepresentationType}) and a name (which is shown in
 * the GUI).
 * 
 * @author bernd.ruecker@camunda.com
 */
public class ContentRepresentation implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private RepositoryArtifact artifact;

  private String type;
	
	private String name;
	
	private String clientUrl;
	
	/**
   * the true content as byte array, is not always fetched, so it maybe null!
   */
	private byte[] content;
	
	private boolean contentfetched = false;

  public ContentRepresentation() {
  }
  
  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getClientUrl() {
    return clientUrl;
  }

  public void setClientUrl(String clientUrl) {
    this.clientUrl = clientUrl;
  }

  public byte[] getContent() {
    return content;
  }

  public void setContent(byte[] content) {
    this.content = content;
  }

  public boolean isContentfetched() {
    return contentfetched;
  }

  public void setContentfetched(boolean contentfetched) {
    this.contentfetched = contentfetched;
  }

  
  public RepositoryArtifact getArtifact() {
    return artifact;
  }

  public void setArtifact(RepositoryArtifact artifact) {
    this.artifact = artifact;
  }

}
