package org.activiti.cycle.impl.connector;

import org.activiti.cycle.ContentRepresentation;
import org.activiti.cycle.MimeType;
import org.activiti.cycle.RepositoryArtifact;
import org.activiti.cycle.RepositoryArtifactType;
import org.activiti.cycle.RepositoryNodeNotFoundException;
import org.activiti.cycle.annotations.CycleComponent;
import org.activiti.cycle.impl.RepositoryArtifactImpl;
import org.activiti.cycle.impl.artifacttype.BasicRepositoryArtifactType;
import org.activiti.cycle.impl.connector.util.ConnectorPathUtils;
import org.activiti.cycle.impl.mimetype.UnknownMimeType;
import org.activiti.cycle.impl.representation.AbstractBasicArtifactTypeContentRepresentation;

/**
 * Abstract base-class for filesystem-based connectors. Handles {@link MimeType}
 * and {@link RepositoryArtifact}-related aspects. Uses the
 * {@link BasicRepositoryArtifactType}.
 * <p />
 * Implementors can use this class if they want to reuse cycle's basic
 * {@link MimeType} / {@link BasicRepositoryArtifactType} and
 * {@link AbstractBasicArtifactTypeContentRepresentation}-infrastructure.
 * 
 * @author daniel.meyer@camunda.com
 */
public abstract class AbstractFileSystemBasedRepositoryConnector extends AbstractRepositoryConnector {

  /**
   * Tries to resolve am {@link MimeType} for the provided filename. If no
   * {@link MimeType} is found, {@link UnknownMimeType} is returned.
   * 
   * <p />
   * Plugin writers can provide their own mimetypes (classes implementing
   * {@link MimeType} and annotated as {@link CycleComponent}).
   */
  protected MimeType getMimeType(String filename) {
    return ConnectorPathUtils.getMimeType(filename);
  }

  /**
   * Returns a {@link RepositoryArtifactType} for the provided {@link MimeType}.
   * Uses the {@link BasicRepositoryArtifactType}. If a filesystem-based
   * connector needs to treat some artifacts differently, (i.e. assign a
   * different {@link RepositoryArtifactType} to process models), this method
   * can be overridden, providing a different {@link RepositoryArtifactType}. In
   * that case, think about overriding
   * {@link #getDefaultContentRepresentation(String)} as well.
   */
  protected RepositoryArtifactType getRepositoryArtifactType(MimeType mimeType, String filename) {
    return new BasicRepositoryArtifactType(mimeType);
  }

  /**
   * Returns an initialized {@link RepositoryArtifact} for the provided filename
   * and the providing nodeId. The implementation tries to resolve a
   * {@link MimeType} and the corresponding {@link BasicRepositoryArtifactType}
   * for the provided Filename.
   * 
   * <p/>
   * Implementors might think about providing additional metadata using
   * {@link RepositoryArtifact#getMetadata()}.
   * 
   * @param filename
   *          the filename of the corresponding file in the repository
   * @param nodeId
   *          the id of the node in the repository represented by this
   *          {@link RepositoryArtifact}
   * @return an initialized {@link RepositoryArtifact}
   * 
   */
  protected RepositoryArtifactImpl getRepositoryArtifactForFileName(String filename, String nodeId) {
    MimeType mimeType = getMimeType(filename);
    RepositoryArtifactType artifactType = getRepositoryArtifactType(mimeType, filename);
    RepositoryArtifactImpl artifact = new RepositoryArtifactImpl(getId(), nodeId, artifactType, this);
    artifact.getMetadata().setName(filename);
    return artifact;
  }

  /**
   * Returns the default content representation for the provided artifact.
   * Assumes that {@link RepositoryArtifact#getArtifactType()} returns a
   * {@link BasicRepositoryArtifactType}.
   */
  public ContentRepresentation getDefaultContentRepresentation(String artifactId) throws RepositoryNodeNotFoundException {
    RepositoryArtifact artifact = getRepositoryArtifact(artifactId);
    return BasicRepositoryArtifactType.getDefaultContentRepresentation((BasicRepositoryArtifactType) artifact.getArtifactType());
  }
}
