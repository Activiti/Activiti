package org.activiti.cycle.service;

import java.util.List;
import java.util.Set;

import org.activiti.cycle.Content;
import org.activiti.cycle.ContentRepresentation;
import org.activiti.cycle.MimeType;
import org.activiti.cycle.RepositoryArtifact;
import org.activiti.cycle.RepositoryArtifactType;
import org.activiti.cycle.impl.artifacttype.RepositoryArtifactTypes;
import org.activiti.cycle.transform.ContentTransformationException;

/**
 * The {@link CycleContentService} manages Artifact-{@link Content}. It acts
 * both as a registry for {@link MimeType}s, {@link RepositoryArtifactType}s as
 * well as a ContentTransformation Factory.
 * 
 * @author daniel.meyer@camunda.com
 */
public interface CycleContentService {

  /**
   * Returns a set of all available {@link MimeType}s
   */
  public Set<MimeType> getAvailableMimeTypes();

  /**
   * Returns a set of all available {@link RepositoryArtifactTypes}
   */
  public Set<RepositoryArtifactType> getAvailableArtifactTypes();

  /**
   * Returns a set of available content representations for the provided
   * {@link RepositoryArtifact}
   * 
   * @param type
   * @return a {@link Set} of {@link ContentRepresentation}s for the provided
   *         {@link RepositoryArtifact}. Returns an empty {@link Set} if no
   *         {@link ContentRepresentation}s are available.
   */
  public List<ContentRepresentation> getcontentRepresentations(RepositoryArtifact artifact);

  /**
   * Return the {@link ContentRepresentation} for a provided {@link RepositoryArtifact} and a
   * contentRepresentationId.
   * 
   * @param artifact
   *          the {@link RepositoryArtifact} to retrieve the {@link ContentRepresentation} for
   * @param contentRepresentationId
   *          the string of the corresponding {@link ContentRepresentation}
   * @return {@link ContentRepresentation}
   */
  public ContentRepresentation getContentRepresentation(RepositoryArtifact artifact, String contentRepresentationId);

  /**
   * Returns a {@link Set} of {@link RepositoryArtifactType}s, the content of
   * the provided {@link RepositoryArtifactType} can be transformed to.
   */
  Set<RepositoryArtifactType> getAvailableTransformations(RepositoryArtifactType type);

  /**
   * Returns a {@link Set} of {@link MimeType}s, the content of the provided
   * {@link MimeType} can be transformed to.
   */
  Set<MimeType> getAvailableTransformations(MimeType mimeType);

  /**
   * Transforms the content of a repository artifact of the provided type to
   * another provided type.
   * 
   * @param content
   *          the content to transform
   * @param fromType
   *          the {@link RepositoryArtifactType} to transform from
   * @param toType
   *          the {@link RepositoryArtifactType} to transform to
   * @return the transformed content
   * @throws ContentTransformationException
   *           if no transformation offering the required functionality is
   *           available or, if an error occurs during the transformation
   */
  Content transformContent(Content content, RepositoryArtifactType fromType, RepositoryArtifactType toType) throws ContentTransformationException;

  /**
   * Transforms the content of a repository artifact of the provided type to
   * another provided type.
   * 
   * @param content
   *          the content to transform
   * @param fromType
   *          the {@link MimeType} to transform from
   * @param toType
   *          the {@link MimeType} to transform to
   * @return the transformed content
   * @throws ContentTransformationException
   *           if no transformation offering the required functionality is
   *           available or, if an error occurs during the transformation
   */
  Content transformContent(Content content, MimeType fromType, MimeType toType) throws ContentTransformationException;

}
