package org.activiti.cycle.impl.processsolution.listener;

import org.activiti.cycle.event.CycleCompensatingEventListener;
import org.activiti.cycle.impl.db.entity.ProcessSolutionEntity;
import org.activiti.cycle.impl.processsolution.event.ProcessSolutionStateEvent;
import org.activiti.cycle.processsolution.ProcessSolutionState;
import org.activiti.cycle.service.CycleProcessSolutionService;
import org.activiti.cycle.service.CycleServiceFactory;

public abstract class AbstractProcessSolutionStateListener<T extends ProcessSolutionStateEvent> implements CycleCompensatingEventListener<T> {

  private CycleProcessSolutionService processService = CycleServiceFactory.getProcessSolutionService();

  public void compensateEvent(T event) {
    ProcessSolutionEntity ps = (ProcessSolutionEntity) processService.getProcessSolutionById(event.getProcessSolution().getId());
    ps.setState(getCurrentState());
    processService.updateProcessSolution(ps);
  }
  public void onEvent(T event) {
    ProcessSolutionEntity ps = (ProcessSolutionEntity) processService.getProcessSolutionById(event.getProcessSolution().getId());
    ps.setState(getNextState());
    processService.updateProcessSolution(ps);
  }
  protected abstract ProcessSolutionState getCurrentState();
  protected abstract ProcessSolutionState getNextState();

}
