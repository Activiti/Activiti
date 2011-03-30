package org.activiti.cycle.impl.processsolution.listener;

import org.activiti.cycle.annotations.CycleComponent;
import org.activiti.cycle.context.CycleContextType;
import org.activiti.cycle.event.CycleCompensatingEventListener;
import org.activiti.cycle.impl.processsolution.event.SpecificationDoneEvent;
import org.activiti.cycle.processsolution.ProcessSolutionState;

/**
 * Listener for {@link ProcessSolutionState} Events.
 * 
 * @author daniel.meyer@camunda.com
 */
@CycleComponent(context = CycleContextType.APPLICATION)
public class SpecificationDoneStateListener extends AbstractProcessSolutionStateListener<SpecificationDoneEvent> implements
        CycleCompensatingEventListener<SpecificationDoneEvent> {

  protected ProcessSolutionState getCurrentState() {
    return ProcessSolutionState.IN_SPECIFICATION;
  }

  protected ProcessSolutionState getNextState() {
    return ProcessSolutionState.IN_IMPLEMENTATION;
  }

}
