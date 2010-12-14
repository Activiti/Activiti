package org.activiti.cycle.transform;

import org.activiti.cycle.Content;
import org.activiti.cycle.MimeType;
import org.activiti.cycle.RepositoryArtifact;

/**
 * A {@link ContentMimeTypeTransformation} transforms the content of a
 * {@link RepositoryArtifact} from one {@link MimeType} to another.
 * <p />
 * Example: TEXT -> XML
 * 
 * @author daniel.meyer@camunda.com
 */
public interface ContentMimeTypeTransformation {

  public MimeType getSourceType();

  public MimeType getTargetType();

  public Content transformContent(Content content) throws ContentTransformationException;

}
