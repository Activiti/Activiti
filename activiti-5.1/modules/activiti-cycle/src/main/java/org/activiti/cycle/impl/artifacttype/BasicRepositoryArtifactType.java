package org.activiti.cycle.impl.artifacttype;

import java.util.Set;

import org.activiti.cycle.ContentRepresentation;
import org.activiti.cycle.MimeType;
import org.activiti.cycle.RepositoryArtifact;
import org.activiti.cycle.RepositoryArtifactType;
import org.activiti.cycle.context.CycleApplicationContext;
import org.activiti.cycle.impl.representation.AbstractBasicArtifactTypeContentRepresentation;
import org.activiti.cycle.impl.representation.ContentRepresentations;

/**
 * Basic {@link RepositoryArtifactType}, representing a
 * {@link RepositoryArtifact} of which only the {@link MimeType} is known. Use
 * this for {@link RepositoryArtifact}s, which represent files and/or which play
 * no "special" role in cycle.
 * 
 * @author daniel.meyer@camunda.com
 */
// not a @CycleComponent
public class BasicRepositoryArtifactType extends AbstractRepositoryArtifactType {

  private MimeType mimeType;

  public BasicRepositoryArtifactType(MimeType mimeType) {
    this.mimeType = mimeType;
  }

  public String getName() {
    return mimeType.getContentType();
  }

  public MimeType getMimeType() {
    return mimeType;
  }

  /**
   * helper-method, can be used to retrieve the default-
   * {@link ContentRepresentation} for a given
   * {@link BasicRepositoryArtifactType}.
   */
  public static ContentRepresentation getDefaultContentRepresentation(BasicRepositoryArtifactType type) {
    ContentRepresentations representations = CycleApplicationContext.get(ContentRepresentations.class);
    Set<ContentRepresentation> representationSet = representations.getContentRepresentations(type);
    for (ContentRepresentation contentRepresentation : representationSet) {
      if (contentRepresentation instanceof AbstractBasicArtifactTypeContentRepresentation) {
        AbstractBasicArtifactTypeContentRepresentation basicArtifactTypeContentRepresentation = (AbstractBasicArtifactTypeContentRepresentation) contentRepresentation;
        if (basicArtifactTypeContentRepresentation.isDefaultRepresentation()) {
          return basicArtifactTypeContentRepresentation;
        }
      }
    }
    return null;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((mimeType == null) ? 0 : mimeType.hashCode());
    return result;
  }

  /**
   * two {@link BasicRepositoryArtifactType}s are equal if they represent the
   * same {@link MimeType}.
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (!super.equals(obj))
      return false;
    if (getClass() != obj.getClass())
      return false;
    BasicRepositoryArtifactType other = (BasicRepositoryArtifactType) obj;
    if (mimeType == null) {
      if (other.mimeType != null)
        return false;
    } else if (!mimeType.equals(other.mimeType))
      return false;
    return true;
  }

}
