package org.activiti.spring.test.jobexecutor;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;

/**

 */
public class ForcedRollbackExecutionListener implements ExecutionListener {

  public void notify(DelegateExecution delegateExecution) {
    throw new RuntimeException("Forcing transaction rollback");
  }

}
