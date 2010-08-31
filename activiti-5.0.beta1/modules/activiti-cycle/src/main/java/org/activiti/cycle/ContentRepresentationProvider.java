package org.activiti.cycle;

import java.util.logging.Logger;

import org.activiti.cycle.impl.plugin.ActivitiCyclePluginRegistry;

/**
 * A {@link ContentRepresentationProvider} is responsible to create
 * {@link ContentRepresentationDefinition} objects for certain
 * {@link RepositoryArtifact} s. It is registered via the
 * {@link ActivitiCyclePluginRegistry} and new providers can be added on the fly.
 * 
 * @author bernd.ruecker
 */
public abstract class ContentRepresentationProvider {

  protected Logger log = Logger.getLogger(this.getClass().getName());

  private final String contentRepresentationName;
  private final String contentRepresentationType;
  private final boolean downloadable;

  public ContentRepresentationProvider(String contentRepresentationName, String contentRepresentationType, boolean contentDownloadable) {
    this.contentRepresentationName = contentRepresentationName;
    this.contentRepresentationType = contentRepresentationType;
    this.downloadable = contentDownloadable;
  }

  /**
   * creates the {@link ContentRepresentationDefinition} object for the given
   * artifact
   */
  public ContentRepresentationDefinition createContentRepresentationDefinition(RepositoryArtifact artifact) {
    ContentRepresentationDefinition contentRepresentation = new ContentRepresentationDefinition();
    // contentRepresentation.setArtifact(artifact);
    contentRepresentation.setName(contentRepresentationName);
    contentRepresentation.setType(contentRepresentationType);
    contentRepresentation.setDownloadable(downloadable);
    return contentRepresentation;
  }

  public abstract void addValueToContent(Content content, RepositoryArtifact artifact);

  public Content createContent(RepositoryArtifact artifact) {
    Content c = new Content();

    addValueToContent(c, artifact);
    if (c.isNull()) {
      throw new RepositoryException("No content created for artifact " + artifact.getId() + " ' from provider '" + getContentRepresentationName()
              + "' (was null). Please check provider or artifact.");
    }

    return c;
  }

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
  
  public boolean isContentDownloadable() {
    return downloadable;
  }
}
