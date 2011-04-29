package org.activiti.cycle.impl.processsolution.event;

import org.activiti.cycle.processsolution.ProcessSolution;

/**
 * Signifies that a {@link ProcessSolution} is transitioning to a new state.
 * 
 * @author daniel.meyer@camunda.com
 */
public class ProcessSolutionStateEvent {

  private ProcessSolution processSolution;

  public ProcessSolutionStateEvent(ProcessSolution processSolution) {
    this.processSolution = processSolution;
  }

  public ProcessSolution getProcessSolution() {
    return processSolution;
  }

}
