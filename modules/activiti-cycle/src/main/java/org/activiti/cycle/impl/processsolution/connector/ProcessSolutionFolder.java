package org.activiti.cycle.impl.processsolution.connector;

import org.activiti.cycle.RepositoryFolder;
import org.activiti.cycle.processsolution.ProcessSolution;
import org.activiti.cycle.processsolution.VirtualRepositoryFolder;

/**
 * A {@link RepositoryFolder}-implementation for virtual folders
 * 
 * @author Daniel Meyer
 */
public class ProcessSolutionFolder extends ProcessSolutionRepositoryNode implements RepositoryFolder {

  public ProcessSolutionFolder(String connectorId, String nodeId, VirtualRepositoryFolder virtualFolder, ProcessSolution processSolution,
          RepositoryFolder wrappedFolder) {
    super(connectorId, nodeId, virtualFolder, processSolution, wrappedFolder);
  }

}
