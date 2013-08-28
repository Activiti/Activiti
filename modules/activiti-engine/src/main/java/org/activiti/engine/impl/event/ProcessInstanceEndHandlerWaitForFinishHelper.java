package org.activiti.engine.impl.event;

import java.util.concurrent.ConcurrentHashMap;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.impl.InstanceLocks;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;

/**
 * @author Saeid Mirzaei
 */

public class ProcessInstanceEndHandlerWaitForFinishHelper implements ExecutionListener {

  private static final long serialVersionUID = 1L;

  public void notify(DelegateExecution execution) {
    String passedProcessInstanceId = ((ExecutionEntity) execution).getProcessInstance().getId();
    ConcurrentHashMap<String, String> instanceLocks = InstanceLocks.getLocks();

    synchronized (instanceLocks) {
      instanceLocks.put(passedProcessInstanceId, "");
      instanceLocks.notify();
    }

  }
}