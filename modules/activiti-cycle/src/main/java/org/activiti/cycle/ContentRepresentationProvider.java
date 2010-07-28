package org.activiti.cycle;

import org.activiti.cycle.impl.RepositoryRegistry;

/**
 * A {@link ContentRepresentationProvider} is responsible to create
 * {@link ContentRepresentation} objects for certain {@link RepositoryArtifact}
 * s. It is registered via the {@link RepositoryRegistry} and new providers can
 * be added on the fly.
 * 
 * @author ruecker
 */
public abstract class ContentRepresentationProvider {

  private String contentRepresentationName;
  private String contentRepresentationType;
  
  public ContentRepresentationProvider(String contentRepresentationName, String contentRepresentationType) {
    this.contentRepresentationName = contentRepresentationName;
    this.contentRepresentationType = contentRepresentationType;
  }
  
  /**
   * creates the {@link ContentRepresentation} object for the given artifact
   */
  public ContentRepresentation createContentRepresentation(RepositoryArtifact artifact, boolean includeBinaryContent) {
    ContentRepresentation contentRepresentation = new ContentRepresentation();
    contentRepresentation.setArtifact(artifact);
    contentRepresentation.setName(contentRepresentationName);
    contentRepresentation.setType(contentRepresentationType);
    if (includeBinaryContent) {
      contentRepresentation.setContent(getContent(artifact));
      contentRepresentation.setContentFetched(true);
    }
    return contentRepresentation;
  }
  
  public byte[] toBytes(String result) {
    return result.getBytes();
  }  
    
  public abstract byte[] getContent(RepositoryArtifact artifact);

  /**
   * key for name in properties for GUI
   * 
   * TODO: I18n
   */
  public String getContentRepresentationName() {
    return contentRepresentationName;
  }

  public String getContentRepresentationType() {
    return contentRepresentationType;
  }
}
