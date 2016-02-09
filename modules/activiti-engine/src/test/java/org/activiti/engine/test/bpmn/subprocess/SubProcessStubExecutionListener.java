package org.activiti.engine.test.bpmn.subprocess;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;

public class SubProcessStubExecutionListener implements ExecutionListener {

  private static final long serialVersionUID = 1L;
  
  public static int executionCounter = 0;
  public static int endExecutionCounter = 0;
  
  @Override
  public void notify(DelegateExecution execution) throws Exception {
    executionCounter++;
    if (((ExecutionEntity) execution).isDeleteRoot()) {
      endExecutionCounter++;
    }
  }
}
