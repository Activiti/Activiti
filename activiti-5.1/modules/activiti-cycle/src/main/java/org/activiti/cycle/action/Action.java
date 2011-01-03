package org.activiti.cycle.action;

import java.util.Set;

import org.activiti.cycle.RepositoryArtifactType;

/**
 * Interface for cycle actions.
 * 
 * @author daniel.meyer@camunda.com
 */
public interface Action {

  /**
   * @return the id of this action
   */
  public String getId();

  /**
   * @return a set of {@link RepositoryArtifactType}s this action is applicable
   *         to. Returns null if this action is applicable to all
   *         {@link RepositoryArtifactType}s.
   */
  public Set<RepositoryArtifactType> getArtifactTypes();

}
