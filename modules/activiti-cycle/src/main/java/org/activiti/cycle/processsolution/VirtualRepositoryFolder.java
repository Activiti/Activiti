package org.activiti.cycle.processsolution;

import org.activiti.cycle.RepositoryFolder;

/**
 * A {@link VirtualRepositoryFolder} is a virtual folder, referencing a concrete
 * {@link RepositoryFolder}.
 * 
 * @author daniel.meyer@camunda.com
 */
public interface VirtualRepositoryFolder {

  /**
   * @return the id of the {@link ProcessSolution}, this folder is associated
   *         to, as returned by {@link ProcessSolution#getId()}
   */
  public String getProcessSolutionId();

  /**
   * @return the type of a repository folder.
   */
  public String getType();

  public String getReferencedNodeId();

  public String getConnectorId();

  public String getLabel();

  public String getId();

}
