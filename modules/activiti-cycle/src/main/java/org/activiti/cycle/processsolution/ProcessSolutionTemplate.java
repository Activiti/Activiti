package org.activiti.cycle.processsolution;

import java.util.List;

/**
 * A template for creating new {@link ProcessSolution}s
 * 
 * @author daniel.meyer@camunda.com
 */
public interface ProcessSolutionTemplate {

  public List<VirtualRepositoryFolder> getVirtualRepositoryFolders();

  public ProcessSolutionState getInitialState();

}
