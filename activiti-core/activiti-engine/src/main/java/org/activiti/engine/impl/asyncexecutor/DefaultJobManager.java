package org.activiti.engine.impl.asyncexecutor;

import static java.util.Arrays.asList;

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
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.delegate.VariableScope;
import org.activiti.engine.delegate.event.ActivitiEventDispatcher;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.impl.ActivitiEventBuilder;
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
import org.activiti.engine.impl.jobexecutor.TriggerTimerEventJobHandler;
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

  public DefaultJobManager() {
  }

  public DefaultJobManager(ProcessEngineConfigurationImpl processEngineConfiguration) {
    this.processEngineConfiguration = processEngineConfiguration;
  }

  @Override
  public JobEntity createAsyncJob(ExecutionEntity execution, boolean exclusive) {
    JobEntity jobEntity = null;
    // When the async executor is activated, the job is directly passed on to the async executor thread
    if (isAsyncExecutorActive()) {
      jobEntity = internalCreateLockedAsyncJob(execution, exclusive);

    } else {
      jobEntity = internalCreateAsyncJob(execution, exclusive);
    }

    return jobEntity;
  }

  @Override
  public void scheduleAsyncJob(JobEntity jobEntity) {
    processEngineConfiguration.getJobEntityManager().insert(jobEntity);
    triggerExecutorIfNeeded(jobEntity);
  }

  protected void triggerExecutorIfNeeded(JobEntity jobEntity) {
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

    processEngineConfiguration.getTimerJobEntityManager().insert(timerJob);

    CommandContext commandContext = Context.getCommandContext();
    ActivitiEventDispatcher eventDispatcher = commandContext.getEventDispatcher();
    if (eventDispatcher.isEnabled()) {
      eventDispatcher.dispatchEvent(ActivitiEventBuilder.createEntityEvent(ActivitiEventType.TIMER_SCHEDULED, timerJob));
    }
  }

  @Override
  public JobEntity moveTimerJobToExecutableJob(TimerJobEntity timerJob) {
    if (timerJob == null) {
      throw new ActivitiException("Empty timer job can not be scheduled");
    }

    JobEntity executableJob = createExecutableJobFromOtherJob(timerJob);
    boolean insertSuccesful = processEngineConfiguration.getJobEntityManager().insertJobEntity(executableJob);
    if (insertSuccesful) {
      processEngineConfiguration.getTimerJobEntityManager().delete(timerJob);
      triggerExecutorIfNeeded(executableJob);
      return executableJob;
    }
    return null;
  }

  @Override
  public TimerJobEntity moveJobToTimerJob(AbstractJobEntity job) {
    TimerJobEntity timerJob = createTimerJobFromOtherJob(job);
    boolean insertSuccesful = processEngineConfiguration.getTimerJobEntityManager().insertTimerJobEntity(timerJob);
    if (insertSuccesful) {
      if (job instanceof JobEntity) {
        processEngineConfiguration.getJobEntityManager().delete((JobEntity) job);
      } else if (job instanceof SuspendedJobEntity) {
        processEngineConfiguration.getSuspendedJobEntityManager().delete((SuspendedJobEntity) job);
      }

      return timerJob;
    }
    return null;
  }

  @Override
  public SuspendedJobEntity moveJobToSuspendedJob(AbstractJobEntity job) {
    SuspendedJobEntity suspendedJob = createSuspendedJobFromOtherJob(job);
    processEngineConfiguration.getSuspendedJobEntityManager().insert(suspendedJob);
    if (job instanceof TimerJobEntity) {
      processEngineConfiguration.getTimerJobEntityManager().delete((TimerJobEntity) job);

    } else if (job instanceof JobEntity) {
      processEngineConfiguration.getJobEntityManager().delete((JobEntity) job);
    }

    return suspendedJob;
  }

  @Override
  public AbstractJobEntity activateSuspendedJob(SuspendedJobEntity job) {
    AbstractJobEntity activatedJob = null;
    if (Job.JOB_TYPE_TIMER.equals(job.getJobType())) {
      activatedJob = createTimerJobFromOtherJob(job);
      processEngineConfiguration.getTimerJobEntityManager().insert((TimerJobEntity) activatedJob);

    } else {
      activatedJob = createExecutableJobFromOtherJob(job);
      JobEntity jobEntity = (JobEntity) activatedJob;
      processEngineConfiguration.getJobEntityManager().insert(jobEntity);
      triggerExecutorIfNeeded(jobEntity);
    }

    processEngineConfiguration.getSuspendedJobEntityManager().delete(job);
    return activatedJob;
  }

  @Override
  public DeadLetterJobEntity moveJobToDeadLetterJob(AbstractJobEntity job) {
    DeadLetterJobEntity deadLetterJob = createDeadLetterJobFromOtherJob(job);
    processEngineConfiguration.getDeadLetterJobEntityManager().insert(deadLetterJob);
    if (job instanceof TimerJobEntity) {
      processEngineConfiguration.getTimerJobEntityManager().delete((TimerJobEntity) job);

    } else if (job instanceof JobEntity) {
      processEngineConfiguration.getJobEntityManager().delete((JobEntity) job);
    }

    return deadLetterJob;
  }

  @Override
  public JobEntity moveDeadLetterJobToExecutableJob(DeadLetterJobEntity deadLetterJobEntity, int retries) {
    if (deadLetterJobEntity == null) {
      throw new ActivitiIllegalArgumentException("Null job provided");
    }

    JobEntity executableJob = createExecutableJobFromOtherJob(deadLetterJobEntity);
    executableJob.setRetries(retries);
    boolean insertSuccesful = processEngineConfiguration.getJobEntityManager().insertJobEntity(executableJob);
    if (insertSuccesful) {
      processEngineConfiguration.getDeadLetterJobEntityManager().delete(deadLetterJobEntity);
      triggerExecutorIfNeeded(executableJob);
      return executableJob;
    }
    return null;
  }

  @Override
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

  @Override
  public void unacquire(Job job) {

    // Deleting the old job and inserting it again with another id,
    // will avoid that the job is immediately is picked up again (for example
    // when doing lots of exclusive jobs for the same process instance)
    if (job instanceof JobEntity) {
      JobEntity jobEntity = (JobEntity) job;
      processEngineConfiguration.getJobEntityManager().delete(jobEntity.getId());

      JobEntity newJobEntity = processEngineConfiguration.getJobEntityManager().create();
      copyJobInfo(newJobEntity, jobEntity);
      newJobEntity.setId(null); // We want a new id to be assigned to this job
      newJobEntity.setLockExpirationTime(null);
      newJobEntity.setLockOwner(null);
      processEngineConfiguration.getJobEntityManager().insert(newJobEntity);

      // We're not calling triggerExecutorIfNeeded here after the inser. The unacquire happened
      // for a reason (eg queue full or exclusive lock failure). No need to try it immediately again,
      // as the chance of failure will be high.

    } else {
      // It could be a v5 job, so simply unlock it.
      processEngineConfiguration.getJobEntityManager().resetExpiredJob(job.getId());
    }

  }

  protected void executeMessageJob(JobEntity jobEntity) {
    executeJobHandler(jobEntity);
    if (jobEntity.getId() != null) {
      Context.getCommandContext().getJobEntityManager().delete(jobEntity);
    }
  }

  protected void executeTimerJob(JobEntity timerEntity) {
    TimerJobEntityManager timerJobEntityManager = processEngineConfiguration.getTimerJobEntityManager();

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
      processEngineConfiguration.getJobEntityManager().delete(timerEntity);
      return;
    }

    executeJobHandler(timerEntity);
    processEngineConfiguration.getJobEntityManager().delete(timerEntity);

    if (logger.isDebugEnabled()) {
      logger.debug("Timer {} fired. Deleting timer.", timerEntity.getId());
    }

    if (timerEntity.getRepeat() != null) {
      TimerJobEntity newTimerJobEntity = timerJobEntityManager.createAndCalculateNextTimer(timerEntity, variableScope);
      if (newTimerJobEntity != null) {
        scheduleTimerJob(newTimerJobEntity);
      }
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

    if (timerEntity.getJobHandlerType().equalsIgnoreCase(TimerStartEventJobHandler.TYPE) ||
        timerEntity.getJobHandlerType().equalsIgnoreCase(TriggerTimerEventJobHandler.TYPE)) {

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
    List<String> expression = asList(originalExpression.split("/"));
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

  protected JobEntity internalCreateAsyncJob(ExecutionEntity execution, boolean exclusive) {
    JobEntity asyncJob = processEngineConfiguration.getJobEntityManager().create();
    fillDefaultAsyncJobInfo(asyncJob, execution, exclusive);
    return asyncJob;
  }

  protected JobEntity internalCreateLockedAsyncJob(ExecutionEntity execution, boolean exclusive) {
    JobEntity asyncJob = processEngineConfiguration.getJobEntityManager().create();
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

  protected JobEntity createExecutableJobFromOtherJob(AbstractJobEntity job) {
    JobEntity executableJob = processEngineConfiguration.getJobEntityManager().create();
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

  protected TimerJobEntity createTimerJobFromOtherJob(AbstractJobEntity otherJob) {
    TimerJobEntity timerJob = processEngineConfiguration.getTimerJobEntityManager().create();
    copyJobInfo(timerJob, otherJob);
    return timerJob;
  }

  protected SuspendedJobEntity createSuspendedJobFromOtherJob(AbstractJobEntity otherJob) {
    SuspendedJobEntity suspendedJob = processEngineConfiguration.getSuspendedJobEntityManager().create();
    copyJobInfo(suspendedJob, otherJob);
    return suspendedJob;
  }

  protected DeadLetterJobEntity createDeadLetterJobFromOtherJob(AbstractJobEntity otherJob) {
    DeadLetterJobEntity deadLetterJob = processEngineConfiguration.getDeadLetterJobEntityManager().create();
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

  public ProcessEngineConfigurationImpl getProcessEngineConfiguration() {
    return processEngineConfiguration;
  }

  public void setProcessEngineConfiguration(ProcessEngineConfigurationImpl processEngineConfiguration) {
    this.processEngineConfiguration = processEngineConfiguration;
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
