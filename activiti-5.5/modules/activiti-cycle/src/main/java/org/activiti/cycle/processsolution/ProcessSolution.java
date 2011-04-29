package org.activiti.cycle.processsolution;

/**
 * A {@link ProcessSolution} is a container for {@link VirtualRepositoryFolder}
 * s. A {@link ProcessSolution} is in a certain {@link ProcessSolutionState}.
 * 
 * 
 * @author daniel.meyer@camunda.com
 */
public interface ProcessSolution {

  /**
   * The id of this {@link ProcessSolution}
   */
  public String getId();

  /**
   * @return the label for this {@link ProcessSolution}.
   */
  public String getLabel();

  /**
   * @return the {@link ProcessSolutionState} this {@link ProcessSolution} is
   *         currently in.
   */
  public ProcessSolutionState getState();

}
