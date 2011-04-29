package org.activiti.cycle.impl.processsolution.listener;

import org.activiti.cycle.annotations.CycleComponent;
import org.activiti.cycle.context.CycleContextType;
import org.activiti.cycle.event.CycleCompensatingEventListener;
import org.activiti.cycle.impl.processsolution.event.ImplementationDoneEvent;
import org.activiti.cycle.impl.processsolution.event.TestingDoneEvent;
import org.activiti.cycle.processsolution.ProcessSolutionState;

@CycleComponent(context = CycleContextType.APPLICATION)
public class TestingDoneStateListener extends AbstractProcessSolutionStateListener<TestingDoneEvent> implements
        CycleCompensatingEventListener<TestingDoneEvent> {

  protected ProcessSolutionState getCurrentState() {
    return ProcessSolutionState.IN_TESTING;
  }

  protected ProcessSolutionState getNextState() {
    return ProcessSolutionState.IN_SPECIFICATION;
  }

}
