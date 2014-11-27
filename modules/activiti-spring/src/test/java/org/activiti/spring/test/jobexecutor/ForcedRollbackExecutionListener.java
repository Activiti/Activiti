package org.activiti.spring.test.jobexecutor;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;

/**
 * @author Pablo Ganga
 */
public class ForcedRollbackExecutionListener implements ExecutionListener {

    public void notify(DelegateExecution delegateExecution) throws Exception {
        throw new RuntimeException("Forcing transaction rollback");
    }

}
