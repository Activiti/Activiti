package org.activiti.cycle.action;

import org.activiti.cycle.RepositoryArtifact;

/**
 * {@link ParameterizedAction} allowing indicating whether it is applicable to a
 * certain artifact via the {@link #isApplicable(RepositoryArtifact)}-method.
 * <p/>
 * TODO: merge into {@link ParameterizedAction} ?
 * 
 * @author daniel.meyer@camunda.com
 */
public interface ArtifactAwareParameterizedAction extends ParameterizedAction {

  /**
   * returns true if the action is applicable to the provided
   * {@link RepositoryArtifact}. return false otherwise.
   */
  public boolean isApplicable(RepositoryArtifact toArtifact);
}
