package org.activiti.cycle;

/**
 * Base class for download content from the webapp
 * 
 * @author ruecker
 */
public class DownloadContentAction extends ArtifactAction {

  private final ContentRepresentationDefinition definiton;

  /**
   * TODO: Think about best way to hand in
   * {@link ContentRepresentationDefinition} link (maybe just the name?)
   */
  public DownloadContentAction(RepositoryArtifact artifact, String contentRepresentationName) {
    super(artifact);
    this.definiton = artifact.getContentRepresentationDefinition(contentRepresentationName);
    if (definiton == null) {
      throw new RepositoryException("Couldn't find content representation definition '" + contentRepresentationName + "' for artifact " + artifact);
    }
  }

  
  public ContentRepresentationDefinition getDefiniton() {
    return definiton;
  }
  
  public String getArtifactId() {
    return getArtifact().getId();
  }

  public String getContentDefinitionName() {
    return getDefiniton().getName();
  }

  public String getContentType() {
    return getDefiniton().getType();
  }

  @Override
  public String getName() {
    return "DOWNLOAD_" + getDefiniton().getName();
  }

  @Override
  public String getLabel() {
    return "Download";
  }
  
}
