package org.activiti.engine.impl.asyncexecutor;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;

import org.activiti.bpmn.model.Event;
import org.activiti.bpmn.model.EventDefinition;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.TimerEventDefinition;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.delegate.VariableScope;
import org.activiti.engine.impl.calendar.BusinessCalendar;
import org.activiti.engine.impl.calendar.CycleBusinessCalendar;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.el.NoExecutionVariableScope;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.jobexecutor.AsyncContinuationJobHandler;
import org.activiti.engine.impl.jobexecutor.AsyncJobAddedNotification;
import org.activiti.engine.impl.jobexecutor.JobHandler;
import org.activiti.engine.impl.jobexecutor.TimerEventHandler;
import org.activiti.engine.impl.jobexecutor.TimerStartEventJobHandler;
import org.activiti.engine.impl.persistence.entity.AbstractJobEntity;
import org.activiti.engine.impl.persistence.entity.DeadLetterJobEntity;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.ExecutionEntityManager;
import org.activiti.engine.impl.persistence.entity.JobEntity;
import org.activiti.engine.impl.persistence.entity.SuspendedJobEntity;
import org.activiti.engine.impl.persistence.entity.TimerJobEntity;
import org.activiti.engine.impl.persistence.entity.TimerJobEntityManager;
import org.activiti.engine.impl.util.ProcessDefinitionUtil;
import org.activiti.engine.impl.util.TimerUtil;
import org.activiti.engine.runtime.Job;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultJobManager implements JobManager {
  
  private static Logger logger = LoggerFactory.getLogger(DefaultJobManager.class);
  
  protected ProcessEngineConfigurationImpl processEngineConfiguration;
  
  public DefaultJobManager(ProcessEngineConfigurationImpl processEngineConfiguration) {
    this.processEngineConfiguration = processEngineConfiguration;
  }
  
  @Override
  public JobEntity createAsyncJob(ExecutionEntity execution, boolean exclusive) {
    CommandContext commandContext = Context.getCommandContext();
    
    JobEntity jobEntity = null;
    // When the async executor is activated, the job is directly passed on to the async executor thread
    if (isAsyncExecutorActive()) {
      jobEntity = createLockedAsyncJob(execution, exclusive, commandContext);
      
    } else {
      jobEntity = createAsyncJob(execution, exclusive, commandContext);
    }
    
    return jobEntity;
  }

  @Override
  public void scheduleAsyncJob(JobEntity jobEntity) {
    CommandContext commandContext = Context.getCommandContext();
    commandContext.getJobEntityManager().insert(jobEntity);
    
    // When the async executor is activated, the job is directly passed on to the async executor thread
    if (isAsyncExecutorActive()) {
      hintAsyncExecutor(jobEntity); 
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
  
  @Override
  public JobEntity moveTimerJobToExecutableJob(TimerJobEntity timerJob) {
    if (timerJob == null) {
      throw new ActivitiException("Empty timer job can not be scheduled");
    }
    
    CommandContext commandContext = Context.getCommandContext();
    JobEntity executableJob = createExecutableJobFromOtherJob(timerJob, commandContext);
    commandContext.getJobEntityManager().insert(executableJob);
    commandContext.getTimerJobEntityManager().delete(timerJob);
    
    // When the async executor is activated, the job is directly passed on to the async executor thread
    if (isAsyncExecutorActive()) {
      hintAsyncExecutor(executableJob); 
    }
    
    return executableJob;
  }
  
  @Override
  public TimerJobEntity moveJobToTimerJob(AbstractJobEntity job) {
    CommandContext commandContext = Context.getCommandContext();
    TimerJobEntity timerJob = createTimerJobFromOtherJob(job, commandContext);
    commandContext.getTimerJobEntityManager().insert(timerJob);
    
    if (job instanceof JobEntity) {
      commandContext.getJobEntityManager().delete((JobEntity) job);
    } else if (job instanceof SuspendedJobEntity) {
      commandContext.getSuspendedJobEntityManager().delete((SuspendedJobEntity) job);
    }
    
    return timerJob;
  }
  
  @Override
  public SuspendedJobEntity moveJobToSuspendedJob(AbstractJobEntity job) {
    CommandContext commandContext = Context.getCommandContext();
    SuspendedJobEntity suspendedJob = createSuspendedJobFromOtherJob(job, commandContext);
    commandContext.getSuspendedJobEntityManager().insert(suspendedJob);
    if (job instanceof TimerJobEntity) {
      commandContext.getTimerJobEntityManager().delete((TimerJobEntity) job);
      
    } else if (job instanceof JobEntity) {
      commandContext.getJobEntityManager().delete((JobEntity) job);
    }
    
    return suspendedJob;
  }
  
  @Override
  public AbstractJobEntity activateSuspendedJob(SuspendedJobEntity job) {
    CommandContext commandContext = Context.getCommandContext();
    AbstractJobEntity activatedJob = null;
    if (Job.JOB_TYPE_TIMER.equals(job.getJobType())) {
      activatedJob = createTimerJobFromOtherJob(job, commandContext);
      commandContext.getTimerJobEntityManager().insert((TimerJobEntity) activatedJob);
      
    } else {
      activatedJob = createExecutableJobFromOtherJob(job, commandContext);
      commandContext.getJobEntityManager().insert((JobEntity) activatedJob);
    }
    
    commandContext.getSuspendedJobEntityManager().delete(job);
    return activatedJob;
  }
  
  @Override
  public DeadLetterJobEntity moveJobToDeadLetterJob(AbstractJobEntity job) {
    CommandContext commandContext = Context.getCommandContext();
    DeadLetterJobEntity deadLetterJob = createDeadLetterJobFromOtherJob(job, commandContext);
    commandContext.getDeadLetterJobEntityManager().insert(deadLetterJob);
    if (job instanceof TimerJobEntity) {
      commandContext.getTimerJobEntityManager().delete((TimerJobEntity) job);
      
    } else if (job instanceof JobEntity) {
      commandContext.getJobEntityManager().delete((JobEntity) job);
    }
    
    return deadLetterJob;
  }
  
  public void execute(Job job) {
    if (job instanceof JobEntity) {
      if (Job.JOB_TYPE_MESSAGE.equals(job.getJobType())) {
        executeMessageJob((JobEntity) job);
      } else if (Job.JOB_TYPE_TIMER.equals(job.getJobType())) {
        executeTimerJob((JobEntity) job);
      }
      
    } else {
      throw new ActivitiException("Only jobs with type JobEntity are supported to be executed");
    }
  }
   
  protected void executeMessageJob(JobEntity jobEntity) {
    executeJobHandler(jobEntity);
    if (jobEntity.getId() != null) {
      Context.getCommandContext().getJobEntityManager().delete(jobEntity);
    }
  }
   
  protected void executeTimerJob(JobEntity timerEntity) {
    CommandContext commandContext = Context.getCommandContext();
    TimerJobEntityManager timerJobEntityManager = commandContext.getTimerJobEntityManager();
    
    VariableScope variableScope = null;
    if (timerEntity.getExecutionId() != null) {
      variableScope = getExecutionEntityManager().findById(timerEntity.getExecutionId());
    }
     
    if (variableScope == null) {
      variableScope = NoExecutionVariableScope.getSharedInstance();
    }

    // set endDate if it was set to the definition
    restoreExtraData(timerEntity, variableScope);

    if (timerEntity.getDuedate() != null && !isValidTime(timerEntity, timerEntity.getDuedate(), variableScope)) {
      if (logger.isDebugEnabled()) {
        logger.debug("Timer {} fired. but the dueDate is after the endDate.  Deleting timer.", timerEntity.getId());
      }
      commandContext.getJobEntityManager().delete(timerEntity);
      return;
    }

    executeJobHandler(timerEntity);
    commandContext.getJobEntityManager().delete(timerEntity);

    if (logger.isDebugEnabled()) {
      logger.debug("Timer {} fired. Deleting timer.", timerEntity.getId());
    }
    
    if (timerEntity.getRepeat() != null) {
      timerJobEntityManager.createAndCalculateNextTimer(timerEntity, variableScope);
    }
  }
  
  protected void executeJobHandler(JobEntity jobEntity) {
    ExecutionEntity execution = null;
    if (jobEntity.getExecutionId() != null) {
      execution = getExecutionEntityManager().findById(jobEntity.getExecutionId());
    }

    Map<String, JobHandler> jobHandlers = processEngineConfiguration.getJobHandlers();
    JobHandler jobHandler = jobHandlers.get(jobEntity.getJobHandlerType());
    jobHandler.execute(jobEntity, jobEntity.getJobHandlerConfiguration(), execution, getCommandContext());
  }
   
  protected void restoreExtraData(JobEntity timerEntity, VariableScope variableScope) {
    String activityId = timerEntity.getJobHandlerConfiguration();

    if (timerEntity.getJobHandlerType().equalsIgnoreCase(TimerStartEventJobHandler.TYPE)) {

      activityId = TimerEventHandler.getActivityIdFromConfiguration(timerEntity.getJobHandlerConfiguration());
      String endDateExpressionString = TimerEventHandler.getEndDateFromConfiguration(timerEntity.getJobHandlerConfiguration());

      if (endDateExpressionString != null) {
        Expression endDateExpression = processEngineConfiguration.getExpressionManager().createExpression(endDateExpressionString);

        String endDateString = null;

        BusinessCalendar businessCalendar = processEngineConfiguration.getBusinessCalendarManager().getBusinessCalendar(
            getBusinessCalendarName(TimerEventHandler.geCalendarNameFromConfiguration(timerEntity.getJobHandlerConfiguration()), variableScope));

        if (endDateExpression != null) {
          Object endDateValue = endDateExpression.getValue(variableScope);
          if (endDateValue instanceof String) {
            endDateString = (String) endDateValue;
          } else if (endDateValue instanceof Date) {
            timerEntity.setEndDate((Date) endDateValue);
          } else {
            throw new ActivitiException("Timer '" + ((ExecutionEntity) variableScope).getActivityId()
                + "' was not configured with a valid duration/time, either hand in a java.util.Date or a String in format 'yyyy-MM-dd'T'hh:mm:ss'");
          }

          if (timerEntity.getEndDate() == null) {
            timerEntity.setEndDate(businessCalendar.resolveEndDate(endDateString));
          }
        }
      }
    }

    int maxIterations = 1;
    if (timerEntity.getProcessDefinitionId() != null) {
      org.activiti.bpmn.model.Process process = ProcessDefinitionUtil.getProcess(timerEntity.getProcessDefinitionId());
      maxIterations = getMaxIterations(process, activityId);
      if (maxIterations <= 1) {
        maxIterations = getMaxIterations(process, activityId);
      }
    }
    timerEntity.setMaxIterations(maxIterations);
  }
   
  protected int getMaxIterations(org.activiti.bpmn.model.Process process, String activityId) {
    FlowElement flowElement = process.getFlowElement(activityId, true);
    if (flowElement != null) {
      if (flowElement instanceof Event) {
         
        Event event = (Event) flowElement;
        List<EventDefinition> eventDefinitions = event.getEventDefinitions();
         
        if (eventDefinitions != null) {
           
          for (EventDefinition eventDefinition : eventDefinitions) {
            if (eventDefinition instanceof TimerEventDefinition) {
              TimerEventDefinition timerEventDefinition = (TimerEventDefinition) eventDefinition;
              if (timerEventDefinition.getTimeCycle() != null) {
                return calculateMaxIterationsValue(timerEventDefinition.getTimeCycle());
              }
            }
          }
           
        }
         
      }
    }
    return -1;
  }
   
  protected int calculateMaxIterationsValue(String originalExpression) {
    int times = Integer.MAX_VALUE;
    List<String> expression = Arrays.asList(originalExpression.split("/"));
    if (expression.size() > 1 && expression.get(0).startsWith("R")) {
      times = Integer.MAX_VALUE;
      if (expression.get(0).length() > 1) {
        times = Integer.parseInt(expression.get(0).substring(1));
      }
    }
    return times;
  }
   
  protected boolean isValidTime(JobEntity timerEntity, Date newTimerDate, VariableScope variableScope) {
    BusinessCalendar businessCalendar = processEngineConfiguration.getBusinessCalendarManager().getBusinessCalendar(
        getBusinessCalendarName(TimerEventHandler.geCalendarNameFromConfiguration(timerEntity.getJobHandlerConfiguration()), variableScope));
    return businessCalendar.validateDuedate(timerEntity.getRepeat(), timerEntity.getMaxIterations(), timerEntity.getEndDate(), newTimerDate);
  }
  
  protected String getBusinessCalendarName(String calendarName, VariableScope variableScope) {
    String businessCalendarName = CycleBusinessCalendar.NAME;
    if (StringUtils.isNotEmpty(calendarName)) {
      businessCalendarName = (String) Context.getProcessEngineConfiguration().getExpressionManager()
          .createExpression(calendarName).getValue(variableScope);
    }
    return businessCalendarName;
  }
  
  protected void hintAsyncExecutor(JobEntity job) {
    AsyncJobAddedNotification jobAddedNotification = new AsyncJobAddedNotification(job, getAsyncExecutor());
    getCommandContext().addCloseListener(jobAddedNotification);
  }
  
  protected JobEntity createAsyncJob(ExecutionEntity execution, boolean exclusive, CommandContext commandContext) {
    JobEntity asyncJob = commandContext.getJobEntityManager().create();
    fillDefaultAsyncJobInfo(asyncJob, execution, exclusive);
    return asyncJob;
  }
  
  protected JobEntity createLockedAsyncJob(ExecutionEntity execution, boolean exclusive, CommandContext commandContext) {
    JobEntity asyncJob = commandContext.getJobEntityManager().create();
    fillDefaultAsyncJobInfo(asyncJob, execution, exclusive);
    
    GregorianCalendar gregorianCalendar = new GregorianCalendar();
    gregorianCalendar.setTime(processEngineConfiguration.getClock().getCurrentTime());
    gregorianCalendar.add(Calendar.MILLISECOND, getAsyncExecutor().getAsyncJobLockTimeInMillis());
    asyncJob.setLockExpirationTime(gregorianCalendar.getTime());
    asyncJob.setLockOwner(getAsyncExecutor().getLockOwner());
    
    return asyncJob;
  }
  
  protected void fillDefaultAsyncJobInfo(JobEntity jobEntity, ExecutionEntity execution, boolean exclusive) {
    jobEntity.setJobType(JobEntity.JOB_TYPE_MESSAGE);
    jobEntity.setRevision(1);
    jobEntity.setRetries(processEngineConfiguration.getAsyncExecutorNumberOfRetries());
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
  
  protected JobEntity createExecutableJobFromOtherJob(AbstractJobEntity job, CommandContext commandContext) {
    JobEntity executableJob = commandContext.getJobEntityManager().create();
    copyJobInfo(executableJob, job);
    
    if (isAsyncExecutorActive()) {
      GregorianCalendar gregorianCalendar = new GregorianCalendar();
      gregorianCalendar.setTime(processEngineConfiguration.getClock().getCurrentTime());
      gregorianCalendar.add(Calendar.MILLISECOND, getAsyncExecutor().getTimerLockTimeInMillis());
      executableJob.setLockExpirationTime(gregorianCalendar.getTime());
      executableJob.setLockOwner(getAsyncExecutor().getLockOwner());
    }
    
    return executableJob;
  }
  
  protected TimerJobEntity createTimerJobFromOtherJob(AbstractJobEntity otherJob, CommandContext commandContext) {
    TimerJobEntity timerJob = commandContext.getTimerJobEntityManager().create();
    copyJobInfo(timerJob, otherJob);
    return timerJob;
  }
  
  protected SuspendedJobEntity createSuspendedJobFromOtherJob(AbstractJobEntity otherJob, CommandContext commandContext) {
    SuspendedJobEntity suspendedJob = commandContext.getSuspendedJobEntityManager().create();
    copyJobInfo(suspendedJob, otherJob);
    return suspendedJob;
  }
  
  protected DeadLetterJobEntity createDeadLetterJobFromOtherJob(AbstractJobEntity otherJob, CommandContext commandContext) {
    DeadLetterJobEntity deadLetterJob = commandContext.getDeadLetterJobEntityManager().create();
    copyJobInfo(deadLetterJob, otherJob);
    return deadLetterJob;
  }
  
  protected AbstractJobEntity copyJobInfo(AbstractJobEntity copyToJob, AbstractJobEntity copyFromJob) {
    copyToJob.setDuedate(copyFromJob.getDuedate());
    copyToJob.setEndDate(copyFromJob.getEndDate());
    copyToJob.setExclusive(copyFromJob.isExclusive());
    copyToJob.setExecutionId(copyFromJob.getExecutionId());
    copyToJob.setId(copyFromJob.getId());
    copyToJob.setJobHandlerConfiguration(copyFromJob.getJobHandlerConfiguration());
    copyToJob.setJobHandlerType(copyFromJob.getJobHandlerType());
    copyToJob.setJobType(copyFromJob.getJobType());
    copyToJob.setExceptionMessage(copyFromJob.getExceptionMessage());
    copyToJob.setExceptionStacktrace(copyFromJob.getExceptionStacktrace());
    copyToJob.setMaxIterations(copyFromJob.getMaxIterations());
    copyToJob.setProcessDefinitionId(copyFromJob.getProcessDefinitionId());
    copyToJob.setProcessInstanceId(copyFromJob.getProcessInstanceId());
    copyToJob.setRepeat(copyFromJob.getRepeat());
    copyToJob.setRetries(copyFromJob.getRetries());
    copyToJob.setRevision(copyFromJob.getRevision());
    copyToJob.setTenantId(copyFromJob.getTenantId());
    
    return copyToJob;
  }
  
  protected boolean isAsyncExecutorActive() {
    return processEngineConfiguration.getAsyncExecutor().isActive();
  }
  
  protected CommandContext getCommandContext() {
    return Context.getCommandContext();
  }
  
  protected AsyncExecutor getAsyncExecutor() {
    return processEngineConfiguration.getAsyncExecutor();
  }
  
  protected ExecutionEntityManager getExecutionEntityManager() {
    return processEngineConfiguration.getExecutionEntityManager();
  }
  
}
