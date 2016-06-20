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
package org.activiti5.engine.impl.cmd;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.activiti.engine.delegate.event.ActivitiEventDispatcher;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.impl.calendar.DurationHelper;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.Job;
import org.activiti5.engine.ActivitiException;
import org.activiti5.engine.ProcessEngineConfiguration;
import org.activiti5.engine.delegate.event.impl.ActivitiEventBuilder;
import org.activiti5.engine.impl.interceptor.Command;
import org.activiti5.engine.impl.interceptor.CommandContext;
import org.activiti5.engine.impl.jobexecutor.AsyncContinuationJobHandler;
import org.activiti5.engine.impl.jobexecutor.TimerCatchIntermediateEventJobHandler;
import org.activiti5.engine.impl.jobexecutor.TimerEventHandler;
import org.activiti5.engine.impl.jobexecutor.TimerExecuteNestedActivityJobHandler;
import org.activiti5.engine.impl.jobexecutor.TimerStartEventJobHandler;
import org.activiti5.engine.impl.persistence.deploy.DeploymentManager;
import org.activiti5.engine.impl.persistence.entity.AbstractJobEntity;
import org.activiti5.engine.impl.persistence.entity.DeadLetterJobEntity;
import org.activiti5.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti5.engine.impl.persistence.entity.JobEntity;
import org.activiti5.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti5.engine.impl.persistence.entity.TimerJobEntity;
import org.activiti5.engine.impl.pvm.process.ActivityImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Saeid Mirzaei
 */

public class JobRetryCmd implements Command<Object> {

  private static final Logger log = LoggerFactory.getLogger(JobRetryCmd.class.getName());

  protected String jobId;
  protected Throwable exception;

  public JobRetryCmd(String jobId, Throwable exception) {
    this.jobId = jobId;
    this.exception = exception;
  }

  public Object execute(CommandContext commandContext)  {
    JobEntity job = commandContext.getJobEntityManager().findJobById(jobId);
    if (job == null) {
      return null;
    }
     
    ActivityImpl activity = getCurrentActivity(commandContext, job);
    ProcessEngineConfiguration processEngineConfig = commandContext.getProcessEngineConfiguration();
   
    AbstractJobEntity newJobEntity = null;
    if (activity == null || activity.getFailedJobRetryTimeCycleValue() == null) {
      log.debug("activitiy or FailedJobRetryTimerCycleValue is null in job " + jobId + "'. only decrementing retries.");
      
      if (job.getRetries() <= 1) {
        DeadLetterJobEntity deadLetterJob = new DeadLetterJobEntity(job);
        deadLetterJob.insert();
        newJobEntity = deadLetterJob;
      } else {
        TimerJobEntity timerJob = new TimerJobEntity(job);
        timerJob.insert();
        newJobEntity = timerJob;
      }
      
      newJobEntity.setRetries(job.getRetries() - 1);
      if (job.getDuedate() == null) {
        // add wait time for failed async job
        newJobEntity.setDuedate(calculateDueDate(commandContext, processEngineConfig.getAsyncFailedJobWaitTime(), null));
      } else {
        // add default wait time for failed job
        newJobEntity.setDuedate(calculateDueDate(commandContext, processEngineConfig.getDefaultFailedJobWaitTime(), job.getDuedate()));
      }
      
    } else {      
      String failedJobRetryTimeCycle = activity.getFailedJobRetryTimeCycleValue();
      try {
        DurationHelper durationHelper = new DurationHelper(failedJobRetryTimeCycle, processEngineConfig.getClock());
        int jobRetries = job.getRetries();
        if (job.getExceptionMessage() == null) {
          // change default retries to the ones configured
          jobRetries = durationHelper.getTimes();
        }
        
        if (job.getRetries() <= 1) {
          DeadLetterJobEntity deadLetterJob = new DeadLetterJobEntity(job);
          deadLetterJob.insert();
          newJobEntity = deadLetterJob;
        } else {
          TimerJobEntity timerJob = new TimerJobEntity(job);
          timerJob.insert();
          newJobEntity = timerJob;
        }
        
        newJobEntity.setDuedate(durationHelper.getDateAfter());
         
        if (job.getExceptionMessage() == null) {  // is it the first exception 
          log.debug("Applying JobRetryStrategy '" + failedJobRetryTimeCycle+ "' the first time for job " + job.getId() + " with "+ durationHelper.getTimes()+" retries");
          
        } else {
          log.debug("Decrementing retries of JobRetryStrategy '" + failedJobRetryTimeCycle+ "' for job " + job.getId());
        }
        newJobEntity.setRetries(jobRetries - 1);
         
      } catch (Exception e) {
        throw new ActivitiException("failedJobRetryTimeCylcle has wrong format:" + failedJobRetryTimeCycle, exception);
      }  
    }
    
    if (exception != null) {
      newJobEntity.setExceptionMessage(exception.getMessage());
      newJobEntity.setExceptionStacktrace(getExceptionStacktrace());
    }
    
    job.delete();
    
    // Dispatch both an update and a retry-decrement event
    ActivitiEventDispatcher eventDispatcher = commandContext.getEventDispatcher();
    if (eventDispatcher.isEnabled()) {
      eventDispatcher.dispatchEvent(ActivitiEventBuilder.createEntityEvent(
          ActivitiEventType.ENTITY_UPDATED, newJobEntity));
      eventDispatcher.dispatchEvent(ActivitiEventBuilder.createEntityEvent(
          ActivitiEventType.JOB_RETRIES_DECREMENTED, newJobEntity));
    }
    
    return null;
  }
  
  protected Date calculateDueDate(CommandContext commandContext, int waitTimeInSeconds, Date oldDate) {
    Calendar newDateCal = new GregorianCalendar();
    if (oldDate != null) {
      newDateCal.setTime(oldDate);
      
    } else {
      newDateCal.setTime(commandContext.getProcessEngineConfiguration().getClock().getCurrentTime());
    }
    
    newDateCal.add(Calendar.SECOND, waitTimeInSeconds);
    return newDateCal.getTime();
  }

  private ActivityImpl getCurrentActivity(CommandContext commandContext, Job job) {
    String type = job.getJobHandlerType();
    ActivityImpl activity = null;

    if (TimerExecuteNestedActivityJobHandler.TYPE.equals(type) ||
        TimerCatchIntermediateEventJobHandler.TYPE.equals(type)) {
      ExecutionEntity execution = fetchExecutionEntity(commandContext, job.getExecutionId());
      if (execution != null) {
        activity = execution.getProcessDefinition().findActivity(job.getJobHandlerConfiguration());
      }
    } else if (TimerStartEventJobHandler.TYPE.equals(type)) {
      
      DeploymentManager deploymentManager = commandContext.getProcessEngineConfiguration().getDeploymentManager();
      if (TimerEventHandler.hasRealActivityId(job.getJobHandlerConfiguration())) {
        
        ProcessDefinition processDefinition = deploymentManager.findDeployedProcessDefinitionById(job.getProcessDefinitionId());
        String activityId = TimerEventHandler.getActivityIdFromConfiguration(job.getJobHandlerConfiguration());
        activity = ((ProcessDefinitionEntity) processDefinition).findActivity(activityId);
        
      } else {
        String processId = job.getJobHandlerConfiguration();
        if (job instanceof TimerJobEntity) {
           processId = TimerEventHandler.getActivityIdFromConfiguration(job.getJobHandlerConfiguration());
        }
        
        ProcessDefinition processDefinition = null;
        if (job.getTenantId() != null && job.getTenantId().length() > 0) {
          processDefinition = deploymentManager.findDeployedLatestProcessDefinitionByKeyAndTenantId(processId, job.getTenantId());
        } else {
          processDefinition = deploymentManager.findDeployedLatestProcessDefinitionByKey(processId);
        }
        
        if (processDefinition != null) {
          activity = ((ProcessDefinitionEntity) processDefinition).getInitial();
        }
      }
      
    } else if (AsyncContinuationJobHandler.TYPE.equals(type)) {
      ExecutionEntity execution = fetchExecutionEntity(commandContext, job.getExecutionId());
      if (execution != null) {
        activity = execution.getActivity();
      }
    } else {
      // nop, because activity type is not supported
    }

    return activity;
  }

  private String getExceptionStacktrace() {
    StringWriter stringWriter = new StringWriter();
    exception.printStackTrace(new PrintWriter(stringWriter));
    return stringWriter.toString();
  }

  private ExecutionEntity fetchExecutionEntity(CommandContext commandContext, String executionId) {
    return commandContext.getExecutionEntityManager().findExecutionById(executionId);
  }
  
}
