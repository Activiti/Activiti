package org.activiti.cycle;

/**
 * The {@link RepositoryArtifactType} represents the "semantic" type of a
 * {@link RepositoryArtifact}.
 * 
 * @author daniel.meyer@camunda.com
 */
public interface RepositoryArtifactType {

  public String getName();

  public MimeType getMimeType();

  public String[] getCommonFileExtensions();

//  public ContentRepresentation getDefaultRepresentation();

}
