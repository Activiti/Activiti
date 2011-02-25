package org.activiti.cycle.transform;

import org.activiti.cycle.Content;

/**
 * Signifies an exception during a {@link Content}-transformation. Example:
 * SIGNAVIO-BPMN-JSON to BPMN-XML.
 * 
 * @see ContentArtifactTypeTransformation
 * @see ContentMimeTypeTransformation
 * @author daniel.meyer@camunda.com
 */
public class ContentTransformationException extends Exception {

  private static final long serialVersionUID = 1L;

  public ContentTransformationException(String message) {
    super(message);
  }

  public ContentTransformationException(String message, Throwable t) {
    super(message, t);
  }

}
