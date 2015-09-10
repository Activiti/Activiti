/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.activiti.engine.impl.persistence.entity;

import java.util.Arrays;
import java.util.Date;
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
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.impl.ActivitiEventBuilder;
import org.activiti.engine.impl.JobQueryImpl;
import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.calendar.BusinessCalendar;
import org.activiti.engine.impl.calendar.CycleBusinessCalendar;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.cfg.TransactionListener;
import org.activiti.engine.impl.cfg.TransactionState;
import org.activiti.engine.impl.el.NoExecutionVariableScope;
import org.activiti.engine.impl.jobexecutor.AsyncJobAddedNotification;
import org.activiti.engine.impl.jobexecutor.JobAddedNotification;
import org.activiti.engine.impl.jobexecutor.JobHandler;
import org.activiti.engine.impl.jobexecutor.TimerEventHandler;
import org.activiti.engine.impl.jobexecutor.TimerStartEventJobHandler;
import org.activiti.engine.impl.persistence.entity.data.DataManager;
import org.activiti.engine.impl.persistence.entity.data.JobDataManager;
import org.activiti.engine.impl.util.ProcessDefinitionUtil;
import org.activiti.engine.runtime.Job;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Tom Baeyens
 * @author Daniel Meyer
 * @author Joram Barrez
 */
public class JobEntityManagerImpl extends AbstractEntityManager<JobEntity> implements JobEntityManager {
  
  private static final Logger logger = LoggerFactory.getLogger(JobEntityManagerImpl.class);
  
  protected JobDataManager jobDataManager;
  
  public JobEntityManagerImpl() {
    
  }
  
  public JobEntityManagerImpl(JobDataManager jobDataManager) {
    this.jobDataManager = jobDataManager;
  }
  
  @Override
  protected DataManager<JobEntity> getDataManager() {
    return jobDataManager;
  }
  
  @Override
  public MessageEntity createMessage() {
    return jobDataManager.createMessage();
  }
  
  @Override
  public TimerEntity createTimer() {
    return jobDataManager.createTimer();
  }
  
  @Override
  public TimerEntity createTimer(TimerEntity te) {
    TimerEntity newTimerEntity = createTimer();
    newTimerEntity.setJobHandlerConfiguration(te.getJobHandlerConfiguration());
    newTimerEntity.setJobHandlerType(te.getJobHandlerType());
    newTimerEntity.setExclusive(te.isExclusive());
    newTimerEntity.setRepeat(te.getRepeat());
    newTimerEntity.setRetries(te.getRetries());
    newTimerEntity.setEndDate(te.getEndDate());
    newTimerEntity.setExecutionId(te.getExecutionId());
    newTimerEntity.setProcessInstanceId(te.getProcessInstanceId());
    newTimerEntity.setProcessDefinitionId(te.getProcessDefinitionId());

    // Inherit tenant
    newTimerEntity.setTenantId(te.getTenantId());
    newTimerEntity.setJobType("timer");
    return newTimerEntity;
  }
  
  @Override
  public void insert(JobEntity jobEntity, boolean fireCreateEvent) {

    // add link to execution
    if (jobEntity.getExecutionId() != null) {
      ExecutionEntity execution = getExecutionEntityManager().findById(jobEntity.getExecutionId());
      execution.getJobs().add(jobEntity);

      // Inherit tenant if (if applicable)
      if (execution.getTenantId() != null) {
        jobEntity.setTenantId(execution.getTenantId());
      }
    }

    super.insert(jobEntity, fireCreateEvent);
  }

  @Override
  public void send(MessageEntity message) {

    ProcessEngineConfigurationImpl processEngineConfiguration = getProcessEngineConfiguration();

    if (processEngineConfiguration.isAsyncExecutorEnabled()) {

      // If the async executor is enabled, we need to set the duedate of
      // the job to the current date + the default lock time.
      // This is cope with the case where the async job executor or the
      // process engine goes down
      // before executing the job. This way, other async job executors can
      // pick the job up after the max lock time.
      Date dueDate = new Date(getClock().getCurrentTime().getTime() + processEngineConfiguration.getAsyncExecutor().getAsyncJobLockTimeInMillis());
      message.setDuedate(dueDate);
      message.setLockExpirationTime(null); // was set before, but to be quickly picked up needs to be set to null

    } else if (!processEngineConfiguration.isJobExecutorActivate()) {

      // If the async executor is disabled AND there is no old school job
      // executor, The job needs to be picked up as soon as possible. So the due date is now set to the current time
      message.setDuedate(processEngineConfiguration.getClock().getCurrentTime());
      message.setLockExpirationTime(null); // was set before, but to be quickly picked up needs to be set to null
    }

    insert(message);
    if (processEngineConfiguration.isAsyncExecutorEnabled()) {
      hintAsyncExecutor(message);
    } else {
      hintJobExecutor(message);
    }
  }

  @Override
  public void schedule(TimerEntity timer) {
    Date duedate = timer.getDuedate();
    if (duedate == null) {
      throw new ActivitiIllegalArgumentException("duedate is null");
    }

    insert(timer);

    if (getProcessEngineConfiguration().isAsyncExecutorEnabled() == false && timer.getDuedate().getTime() <= (getClock().getCurrentTime().getTime())) {
      hintJobExecutor(timer);
    }
  }

  @Override
  public void retryAsyncJob(JobEntity job) {
    try {
    	
    	// If a job has to be retried, we wait for a certain amount of time,
    	// otherwise the job will be continuously be retried without delay (and thus seriously stressing the database).
	    Thread.sleep(getAsyncExecutor().getRetryWaitTimeInMillis());
	    
    } catch (InterruptedException e) {
    }
    getAsyncExecutor().executeAsyncJob(job);
  }

  protected void hintAsyncExecutor(JobEntity job) {

    // notify job executor:
    TransactionListener transactionListener = new AsyncJobAddedNotification(job, getAsyncExecutor());
    getCommandContext().getTransactionContext().addTransactionListener(TransactionState.COMMITTED, transactionListener);
  }

  protected void hintJobExecutor(JobEntity job) {

    // notify job executor:
    TransactionListener transactionListener = new JobAddedNotification(getJobExecutor());
    getCommandContext().getTransactionContext().addTransactionListener(TransactionState.COMMITTED, transactionListener);
  }

  @Override
  public List<JobEntity> findNextJobsToExecute(Page page) {
    return jobDataManager.findNextJobsToExecute(page); 
  }

  @Override
  public List<JobEntity> findNextTimerJobsToExecute(Page page) {
    return jobDataManager.findNextTimerJobsToExecute(page);
  }

  @Override
  public List<JobEntity> findAsyncJobsDueToExecute(Page page) {
    return jobDataManager.findAsyncJobsDueToExecute(page);
  }

  @Override
  public List<JobEntity> findJobsByLockOwner(String lockOwner, int start, int maxNrOfJobs) {
    return jobDataManager.findJobsByLockOwner(lockOwner, start, maxNrOfJobs);
  }

  @Override
  public List<JobEntity> findJobsByExecutionId(String executionId) {
    return jobDataManager.findJobsByExecutionId(executionId);
  }

  @Override
  public List<JobEntity> findExclusiveJobsToExecute(String processInstanceId) {
    return jobDataManager.findExclusiveJobsToExecute(processInstanceId);
  }

  @Override
  public List<TimerEntity> findUnlockedTimersByDuedate(Date duedate, Page page) {
    return jobDataManager.findUnlockedTimersByDuedate(duedate, page);
  }

  @Override
  public List<TimerEntity> findTimersByExecutionId(String executionId) {
    return jobDataManager.findTimersByExecutionId(executionId);
  }

  @Override
  public List<Job> findJobsByQueryCriteria(JobQueryImpl jobQuery, Page page) {
    return jobDataManager.findJobsByQueryCriteria(jobQuery, page);
  }
  
  @Override
  public List<Job> findJobsByTypeAndProcessDefinitionIds(String jobHandlerType, List<String> processDefinitionIds) {
   return jobDataManager.findJobsByTypeAndProcessDefinitionIds(jobHandlerType, processDefinitionIds);
  }
  
  @Override
  public List<Job> findJobsByTypeAndProcessDefinitionKeyNoTenantId(String jobHandlerType, String processDefinitionKey) {
    return jobDataManager.findJobsByTypeAndProcessDefinitionKeyNoTenantId(jobHandlerType, processDefinitionKey);
  }
  
  @Override
  public List<Job> findJobsByTypeAndProcessDefinitionKeyAndTenantId(String jobHandlerType, String processDefinitionKey, String tenantId) {
    return jobDataManager.findJobsByTypeAndProcessDefinitionKeyAndTenantId(jobHandlerType, processDefinitionKey, tenantId);
  }
  
  @Override
  public List<Job> findJobsByTypeAndProcessDefinitionId(String jobHandlerType, String processDefinitionId) {
     return jobDataManager.findJobsByTypeAndProcessDefinitionId(jobHandlerType, processDefinitionId);
  }
  
  @Override
  public long findJobCountByQueryCriteria(JobQueryImpl jobQuery) {
    return jobDataManager.findJobCountByQueryCriteria(jobQuery);
  }

  @Override
  public void updateJobTenantIdForDeployment(String deploymentId, String newTenantId) {
    jobDataManager.updateJobTenantIdForDeployment(deploymentId, newTenantId);
  }

  @Override
  public void delete(JobEntity jobEntity) {
    super.delete(jobEntity);

    ByteArrayRef exceptionByteArrayRef = jobEntity.getExceptionByteArrayRef();

    // Also delete the job's exception byte array
    exceptionByteArrayRef.delete();

    // remove link to execution
    if (jobEntity.getExecutionId() != null) {
      ExecutionEntity execution = getExecutionEntityManager().findById(jobEntity.getExecutionId());
      execution.getJobs().remove(this);
    }
    
    // Send event
    if (getEventDispatcher().isEnabled()) {
      getEventDispatcher().dispatchEvent(ActivitiEventBuilder.createEntityEvent(ActivitiEventType.ENTITY_DELETED, this));
    }
  }
  
  // Job Execution logic ////////////////////////////////////////////////////////////////////
  
  @Override
  public void execute(JobEntity jobEntity) {
    if (jobEntity instanceof MessageEntity) {
      executeMessageJob(jobEntity);
    } else if (jobEntity instanceof TimerEntity) {
      executeTimerJob((TimerEntity) jobEntity);
    } 
  }

  protected void executeJobHandler(JobEntity jobEntity) {
    
    ExecutionEntity execution = null;
    if (jobEntity.getExecutionId() != null) {
      execution = getExecutionEntityManager().findById(jobEntity.getExecutionId());
    }

    Map<String, JobHandler> jobHandlers = getProcessEngineConfiguration().getJobHandlers();
    JobHandler jobHandler = jobHandlers.get(jobEntity.getJobHandlerType());
    jobHandler.execute(jobEntity, jobEntity.getJobHandlerConfiguration(), execution, getCommandContext());
  }
  
  protected void executeMessageJob(JobEntity jobEntity) {
    executeJobHandler(jobEntity);
    delete(jobEntity);
  }
  
  protected void executeTimerJob(TimerEntity timerEntity) {

    // set endDate if it was set to the definition
    restoreExtraData(timerEntity);

    if (timerEntity.getDuedate() != null && !isValidTime(timerEntity, timerEntity.getDuedate())) {
      if (logger.isDebugEnabled()) {
        logger.debug("Timer {} fired. but the dueDate is after the endDate.  Deleting timer.", timerEntity.getId());
      }
      delete(timerEntity);
      return;
    }

    executeJobHandler(timerEntity);

    if (logger.isDebugEnabled()) {
      logger.debug("Timer {} fired. Deleting timer.", timerEntity.getId());
    }
    delete(timerEntity);

    if (timerEntity.getRepeat() != null) {
      int repeatValue = calculateRepeatValue(timerEntity);
      if (repeatValue != 0) {
        if (repeatValue > 0) {
          setNewRepeat(timerEntity, repeatValue);
        }
        Date newTimer = calculateNextTimer(timerEntity);
        if (newTimer != null && isValidTime(timerEntity, newTimer)) {
          TimerEntity te = createTimer(timerEntity);
          te.setDuedate(newTimer);
          getJobEntityManager().schedule(te);
        }
      }
    }
  }
  
  protected void restoreExtraData(TimerEntity timerEntity) {
    String activityId = timerEntity.getJobHandlerConfiguration();

    if (timerEntity.getJobHandlerType().equalsIgnoreCase(TimerStartEventJobHandler.TYPE)) {

      activityId = TimerEventHandler.getActivityIdFromConfiguration(timerEntity.getJobHandlerConfiguration());
      String endDateExpressionString = TimerEventHandler.getEndDateFromConfiguration(timerEntity.getJobHandlerConfiguration());

      if (endDateExpressionString != null) {
        Expression endDateExpression = getProcessEngineConfiguration().getExpressionManager().createExpression(endDateExpressionString);

        String endDateString = null;

        BusinessCalendar businessCalendar = getProcessEngineConfiguration().getBusinessCalendarManager().getBusinessCalendar(CycleBusinessCalendar.NAME);

        VariableScope executionEntity = null;
        if (timerEntity.getExecutionId() != null) {
          executionEntity = getExecutionEntityManager().findById(timerEntity.getExecutionId());
        }
        
        if (executionEntity == null) {
          executionEntity = NoExecutionVariableScope.getSharedInstance();
        }

        if (endDateExpression != null) {
          Object endDateValue = endDateExpression.getValue(executionEntity);
          if (endDateValue instanceof String) {
            endDateString = (String) endDateValue;
          } else if (endDateValue instanceof Date) {
            timerEntity.setEndDate((Date) endDateValue);
          } else {
            throw new ActivitiException("Timer '" + ((ExecutionEntity) executionEntity).getActivityId()
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
  
  protected int calculateRepeatValue(TimerEntity timerEntity) {
    int times = -1;
    List<String> expression = Arrays.asList(timerEntity.getRepeat().split("/"));
    if (expression.size() > 1 && expression.get(0).startsWith("R") && expression.get(0).length() > 1) {
      times = Integer.parseInt(expression.get(0).substring(1));
      if (times > 0) {
        times--;
      }
    }
    return times;
  }

  protected void setNewRepeat(TimerEntity timerEntity, int newRepeatValue) {
    List<String> expression = Arrays.asList(timerEntity.getRepeat().split("/"));
    expression = expression.subList(1, expression.size());
    StringBuilder repeatBuilder = new StringBuilder("R");
    repeatBuilder.append(newRepeatValue);
    for (String value : expression) {
      repeatBuilder.append("/");
      repeatBuilder.append(value);
    }
    timerEntity.setRepeat(repeatBuilder.toString());
  }
  
  protected boolean isValidTime(TimerEntity timerEntity, Date newTimerDate) {
    BusinessCalendar businessCalendar = getProcessEngineConfiguration().getBusinessCalendarManager().getBusinessCalendar(CycleBusinessCalendar.NAME);
    return businessCalendar.validateDuedate(timerEntity.getRepeat(), timerEntity.getMaxIterations(), timerEntity.getEndDate(), newTimerDate);
  }
  
  protected Date calculateNextTimer(TimerEntity timerEntity) {
    BusinessCalendar businessCalendar = getProcessEngineConfiguration().getBusinessCalendarManager().getBusinessCalendar(CycleBusinessCalendar.NAME);
    return businessCalendar.resolveDuedate(timerEntity.getRepeat(), timerEntity.getMaxIterations());
  }

  public JobDataManager getJobDataManager() {
    return jobDataManager;
  }

  public void setJobDataManager(JobDataManager jobDataManager) {
    this.jobDataManager = jobDataManager;
  }
  
}
