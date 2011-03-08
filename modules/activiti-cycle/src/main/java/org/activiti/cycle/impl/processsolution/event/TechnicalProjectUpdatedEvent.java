package org.activiti.cycle.impl.processsolution.event;

import org.activiti.cycle.RepositoryFolder;
import org.activiti.cycle.processsolution.ProcessSolution;

/**
 * Signifies that a new technical project has been updated.
 * 
 * @author daniel.meyer@camunda.com
 */
public class TechnicalProjectUpdatedEvent {

  private ProcessSolution processSolution;

  private RepositoryFolder repositoryFolder;

  public TechnicalProjectUpdatedEvent(ProcessSolution ps, RepositoryFolder folder) {
    this.processSolution = ps;
    this.repositoryFolder = folder;
  }

  public ProcessSolution getProcessSolution() {
    return processSolution;
  }

  public RepositoryFolder getRepositoryFolder() {
    return repositoryFolder;
  }

}
