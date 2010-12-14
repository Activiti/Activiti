package org.activiti.cycle.impl.repositoryartifacttype;

import org.activiti.cycle.RepositoryArtifactType;

/**
 * Abstract {@link RepositoryArtifactType} ensures that {@link #hashCode()} and
 * {@link #equals(Object)} are implemented properly.
 * 
 * @author daniel.meyer@camunda.com
 */
public abstract class AbstractRepositoryArtifactType implements RepositoryArtifactType {

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((getMimeType() == null) ? 0 : getMimeType().hashCode());
    result = prime * result + ((getName() == null) ? 0 : getName().hashCode());
    return result;
  }
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    RepositoryArtifactType other = (RepositoryArtifactType) obj;
    if (getMimeType() == null) {
      if (other.getMimeType() != null)
        return false;
    } else if (!getMimeType().equals(other.getMimeType()))
      return false;
    if (getName() == null) {
      if (other.getName() != null)
        return false;
    } else if (!getName().equals(other.getName()))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return getClass().getName() + "(of '" + getMimeType() + "')";
  }

}
