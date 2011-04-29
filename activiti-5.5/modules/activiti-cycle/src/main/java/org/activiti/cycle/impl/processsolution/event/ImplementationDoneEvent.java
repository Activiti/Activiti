package org.activiti.cycle.impl.processsolution.event;

import org.activiti.cycle.processsolution.ProcessSolution;

/**
 * Signifies that the implementation-phase is completed for a given
 * {@link ProcessSolution}
 * 
 * @author daniel.meyer@camunda.com
 */
public class ImplementationDoneEvent extends ProcessSolutionStateEvent {

  public ImplementationDoneEvent(ProcessSolution processSolution) {
    super(processSolution);
  }

}
