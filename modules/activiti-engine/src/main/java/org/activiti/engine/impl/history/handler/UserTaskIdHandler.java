package org.activiti.engine.impl.history.handler;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.HistoricActivityInstanceEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntity;


public class UserTaskIdHandler implements TaskListener {

  public void notify(DelegateTask task) {
    TaskEntity t = (TaskEntity) task;
    ExecutionEntity execution = t.getExecution();
    if (execution != null) {
      HistoricActivityInstanceEntity historicActivityInstance = ActivityInstanceEndHandler.findActivityInstance(execution);
      historicActivityInstance.setTaskId(t.getId());
    }
  }
  
}
