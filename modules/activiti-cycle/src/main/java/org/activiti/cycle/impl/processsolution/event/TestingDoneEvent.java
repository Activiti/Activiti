package org.activiti.cycle.impl.processsolution.event;

import org.activiti.cycle.processsolution.ProcessSolution;

/**
 * Signifies that the testing-phase is completed for a given
 * {@link ProcessSolution}
 * 
 * @author daniel.meyer@camunda.com
 */
public class TestingDoneEvent extends ProcessSolutionStateEvent {

  public TestingDoneEvent(ProcessSolution processSolution) {
    super(processSolution);
  }

}
