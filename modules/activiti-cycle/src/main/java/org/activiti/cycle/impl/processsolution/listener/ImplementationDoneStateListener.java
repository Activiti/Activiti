package org.activiti.cycle.impl.processsolution.listener;

import org.activiti.cycle.annotations.CycleComponent;
import org.activiti.cycle.context.CycleContextType;
import org.activiti.cycle.event.CycleCompensatingEventListener;
import org.activiti.cycle.impl.processsolution.event.ImplementationDoneEvent;
import org.activiti.cycle.processsolution.ProcessSolutionState;

@CycleComponent(context = CycleContextType.APPLICATION)
public class ImplementationDoneStateListener extends AbstractProcessSolutionStateListener<ImplementationDoneEvent> implements
        CycleCompensatingEventListener<ImplementationDoneEvent> {

  protected ProcessSolutionState getCurrentState() {
    return ProcessSolutionState.IN_IMPLEMENTATION;
  }

  protected ProcessSolutionState getNextState() {
    return ProcessSolutionState.IN_TESTING;
  }

}
