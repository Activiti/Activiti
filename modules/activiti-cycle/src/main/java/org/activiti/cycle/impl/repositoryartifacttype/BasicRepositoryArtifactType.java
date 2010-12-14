package org.activiti.cycle.impl.repositoryartifacttype;

import org.activiti.cycle.MimeType;
import org.activiti.cycle.RepositoryArtifact;
import org.activiti.cycle.RepositoryArtifactType;

/**
 * Basic {@link RepositoryArtifactType} for artifacts of which only the
 * {@link MimeType} is known. Usually, these are {@link RepositoryArtifact}s
 * which play no special role in cycle.
 * 
 * @author daniel.meyer@camunda.com
 */
// NOTE: this is NOT a @CycleComponent.
public class BasicRepositoryArtifactType extends AbstractRepositoryArtifactType {

  private MimeType mimeType;

  public BasicRepositoryArtifactType(MimeType mimeType) {
    this.mimeType = mimeType;
  }

  public String getName() {
    return mimeType.getName();
  }

  public MimeType getMimeType() {
    return mimeType;
  }

  public String[] getCommonFileExtensions() {
    return mimeType.getCommonFileExtensions();
  }

}
