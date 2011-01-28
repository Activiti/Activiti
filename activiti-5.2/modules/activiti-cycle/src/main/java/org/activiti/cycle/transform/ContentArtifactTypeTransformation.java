package org.activiti.cycle.transform;

import org.activiti.cycle.Content;
import org.activiti.cycle.RepositoryArtifactType;

/**
 * A {@link ContentArtifactTypeTransformation} transforms the content of an
 * artifact of one {@link RepositoryArtifactType} to another.
 * 
 * @author daniel.meyer@camunda.com
 */
public interface ContentArtifactTypeTransformation {

  public RepositoryArtifactType getSourceType();

  public RepositoryArtifactType getTargetType();

  public Content transformContent(Content sourceContent);

}
