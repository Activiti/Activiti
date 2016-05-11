package org.activiti.engine.impl.asyncexecutor;

import org.activiti.bpmn.model.TimerEventDefinition;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.JobEntity;
import org.activiti.engine.impl.persistence.entity.TimerJobEntity;

public interface JobManager {

  JobEntity createAsyncJob(ExecutionEntity execution, boolean exclusive);
  
  void scheduleAsyncJob(JobEntity job);
  
  TimerJobEntity createTimerJob(TimerEventDefinition timerEventDefinition, boolean interrupting, 
      ExecutionEntity execution, String timerEventType, String jobHandlerConfiguration);
  
  void scheduleTimerJob(TimerJobEntity timerJob);
  
  void execute(JobEntity jobEntity);
}
