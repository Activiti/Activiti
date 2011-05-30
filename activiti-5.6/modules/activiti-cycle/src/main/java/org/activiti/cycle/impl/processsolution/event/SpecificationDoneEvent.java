package org.activiti.cycle.impl.processsolution.event;

import org.activiti.cycle.processsolution.ProcessSolution;

/**
 * Signifies that the specification-phase is completed for a given
 * {@link ProcessSolution}
 * 
 * @author daniel.meyer@camunda.com
 */
public class SpecificationDoneEvent extends ProcessSolutionStateEvent {

  public SpecificationDoneEvent(ProcessSolution processSolution) {
    super(processSolution);
  }

}
