package org.activiti.engine.impl.asyncexecutor;

import org.activiti.bpmn.model.TimerEventDefinition;
import org.activiti.engine.impl.persistence.entity.AbstractJobEntity;
import org.activiti.engine.impl.persistence.entity.DeadLetterJobEntity;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.JobEntity;
import org.activiti.engine.impl.persistence.entity.SuspendedJobEntity;
import org.activiti.engine.impl.persistence.entity.TimerJobEntity;
import org.activiti.engine.runtime.Job;

public interface JobManager {

  JobEntity createAsyncJob(ExecutionEntity execution, boolean exclusive);
  
  void scheduleAsyncJob(JobEntity job);
  
  TimerJobEntity createTimerJob(TimerEventDefinition timerEventDefinition, boolean interrupting, 
      ExecutionEntity execution, String timerEventType, String jobHandlerConfiguration);
  
  void scheduleTimerJob(TimerJobEntity timerJob);
  
  JobEntity moveTimerJobToExecutableJob(TimerJobEntity timerJob);
  
  TimerJobEntity moveJobToTimerJob(AbstractJobEntity job);
  
  SuspendedJobEntity moveJobToSuspendedJob(AbstractJobEntity job);
  
  AbstractJobEntity activateSuspendedJob(SuspendedJobEntity job);
  
  DeadLetterJobEntity moveJobToDeadLetterJob(AbstractJobEntity job);
  
  void execute(Job job);
}
