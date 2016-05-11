package org.activiti.engine.impl.asyncexecutor;

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.activiti.bpmn.model.TimerEventDefinition;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.cfg.TransactionListener;
import org.activiti.engine.impl.cfg.TransactionState;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.jobexecutor.AsyncContinuationJobHandler;
import org.activiti.engine.impl.jobexecutor.AsyncJobAddedNotification;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.JobEntity;
import org.activiti.engine.impl.persistence.entity.LockedJobEntity;
import org.activiti.engine.impl.persistence.entity.TimerJobEntity;
import org.activiti.engine.impl.util.TimerUtil;
import org.activiti.engine.runtime.Clock;

public class DefaultJobManager implements JobManager {
  
  protected ProcessEngineConfigurationImpl processEngineConfiguration;
  
  public DefaultJobManager(ProcessEngineConfigurationImpl processEngineConfiguration) {
    this.processEngineConfiguration = processEngineConfiguration;
  }
  
  @Override
  public JobEntity createAsyncJob(ExecutionEntity execution, boolean exclusive) {
    CommandContext commandContext = Context.getCommandContext();
    
    JobEntity jobEntity = null;
    // When the async executor is activated, the job is directly passed on to the async executor thread
    if (processEngineConfiguration.isAsyncExecutorActivate()) {
      jobEntity = createLockedAsyncJob(execution, exclusive, commandContext);
      
    } else {
      jobEntity = createAsyncJob(execution, exclusive, commandContext);
    }
    
    return jobEntity;
  }

  @Override
  public void scheduleAsyncJob(JobEntity jobEntity) {
    CommandContext commandContext = Context.getCommandContext();
    
    // When the async executor is activated, the job is directly passed on to the async executor thread
    if (processEngineConfiguration.isAsyncExecutorActivate()) {
      LockedJobEntity lockedJobEntity = null;
      if (jobEntity instanceof LockedJobEntity == false) {
        lockedJobEntity = copyJobToLockedJob(jobEntity, commandContext);
      } else {
        lockedJobEntity = (LockedJobEntity) jobEntity;
      }
      
      commandContext.getLockedJobEntityManager().insert(lockedJobEntity);
      hintAsyncExecutor(lockedJobEntity);
      
    } else {
      commandContext.getJobEntityManager().insert(jobEntity);
    }
  }
  
  @Override
  public TimerJobEntity createTimerJob(TimerEventDefinition timerEventDefinition, boolean interrupting, 
      ExecutionEntity execution, String timerEventType, String jobHandlerConfiguration) {
    
    TimerJobEntity timerEntity = TimerUtil.createTimerEntityForTimerEventDefinition(timerEventDefinition, interrupting, 
        execution, timerEventType, jobHandlerConfiguration);
    
    return timerEntity;
  }
  
  @Override
  public void scheduleTimerJob(TimerJobEntity timerJob) {
    if (timerJob == null) {
      throw new ActivitiException("Empty timer job can not be scheduled");
    }
    
    CommandContext commandContext = Context.getCommandContext();
    commandContext.getTimerJobEntityManager().insert(timerJob);
  }
  
  public void execute(JobEntity jobEntity) {
    CommandContext commandContext = Context.getCommandContext();
    
    LockedJobEntity lockedJobEntity = null;
    if (jobEntity instanceof LockedJobEntity) {
      lockedJobEntity = (LockedJobEntity) jobEntity;
    
    } else {
      lockedJobEntity = copyJobToLockedJob(jobEntity, commandContext);
    }
    
    commandContext.getLockedJobEntityManager().execute(lockedJobEntity);
  }
  
  protected void hintAsyncExecutor(JobEntity job) {
    TransactionListener transactionListener = new AsyncJobAddedNotification(job, getAsyncExecutor());
    getCommandContext().getTransactionContext().addTransactionListener(TransactionState.COMMITTED, transactionListener);
  }
  
  protected JobEntity createAsyncJob(ExecutionEntity execution, boolean exclusive, CommandContext commandContext) {
    JobEntity asyncJob = commandContext.getJobEntityManager().create();
    fillDefaultAsyncJobInfo(asyncJob, execution, exclusive);
    return asyncJob;
  }
  
  protected LockedJobEntity createLockedAsyncJob(ExecutionEntity execution, boolean exclusive, CommandContext commandContext) {
    LockedJobEntity asyncJob = commandContext.getLockedJobEntityManager().create();
    fillDefaultAsyncJobInfo(asyncJob, execution, exclusive);
    
    GregorianCalendar gregorianCalendar = new GregorianCalendar();
    gregorianCalendar.setTime(processEngineConfiguration.getClock().getCurrentTime());
    gregorianCalendar.add(Calendar.MILLISECOND, getAsyncExecutor().getAsyncJobLockTimeInMillis());
    asyncJob.setLockExpirationTime(gregorianCalendar.getTime());
    asyncJob.setLockOwner(getAsyncExecutor().getLockOwner());
    
    return asyncJob;
  }
  
  protected LockedJobEntity copyJobToLockedJob(JobEntity job, CommandContext commandContext) {
    LockedJobEntity lockedJob = commandContext.getLockedJobEntityManager().create();
    lockedJob.setId(job.getId());
    lockedJob.setExecutionId(job.getExecutionId());
    lockedJob.setJobHandlerConfiguration(job.getJobHandlerConfiguration());
    lockedJob.setJobHandlerType(job.getJobHandlerType());
    lockedJob.setJobType(job.getJobType());
    lockedJob.setProcessDefinitionId(job.getProcessDefinitionId());
    lockedJob.setProcessInstanceId(job.getProcessInstanceId());
    lockedJob.setTenantId(job.getTenantId());
    
    GregorianCalendar gregorianCalendar = new GregorianCalendar();
    gregorianCalendar.setTime(processEngineConfiguration.getClock().getCurrentTime());
    gregorianCalendar.add(Calendar.MILLISECOND, getAsyncExecutor().getAsyncJobLockTimeInMillis());
    lockedJob.setLockExpirationTime(gregorianCalendar.getTime());
    lockedJob.setLockOwner(getAsyncExecutor().getLockOwner());
    
    return lockedJob;
  }
  
  protected void fillDefaultAsyncJobInfo(JobEntity jobEntity, ExecutionEntity execution, boolean exclusive) {
    jobEntity.setJobType(JobEntity.JOB_TYPE_MESSAGE);
    jobEntity.setExecutionId(execution.getId());
    jobEntity.setProcessInstanceId(execution.getProcessInstanceId());
    jobEntity.setProcessDefinitionId(execution.getProcessDefinitionId());
    jobEntity.setExclusive(exclusive);
    jobEntity.setJobHandlerType(AsyncContinuationJobHandler.TYPE);
    
    // Inherit tenant id (if applicable)
    if (execution.getTenantId() != null) {
      jobEntity.setTenantId(execution.getTenantId());
    }
  }
  
  protected CommandContext getCommandContext() {
    return Context.getCommandContext();
  }
  
  protected ProcessEngineConfigurationImpl getProcessEngineConfiguration() {
    return getCommandContext().getProcessEngineConfiguration();
  }
  
  protected AsyncExecutor getAsyncExecutor() {
    return getProcessEngineConfiguration().getAsyncExecutor();
  }
  
  protected Clock getClock() {
    return getProcessEngineConfiguration().getClock();
  }

}
