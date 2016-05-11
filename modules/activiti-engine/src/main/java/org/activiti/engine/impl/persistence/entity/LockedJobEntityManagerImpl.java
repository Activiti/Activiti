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
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.delegate.VariableScope;
import org.activiti.engine.impl.JobQueryImpl;
import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.calendar.BusinessCalendar;
import org.activiti.engine.impl.calendar.CycleBusinessCalendar;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.el.NoExecutionVariableScope;
import org.activiti.engine.impl.jobexecutor.JobHandler;
import org.activiti.engine.impl.jobexecutor.TimerEventHandler;
import org.activiti.engine.impl.jobexecutor.TimerStartEventJobHandler;
import org.activiti.engine.impl.persistence.entity.data.LockedJobDataManager;
import org.activiti.engine.impl.util.ProcessDefinitionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Tijs Rademakers
 */
public class LockedJobEntityManagerImpl extends AbstractEntityManager<LockedJobEntity> implements LockedJobEntityManager {

  private static final Logger logger = LoggerFactory.getLogger(LockedJobEntityManagerImpl.class);

  protected LockedJobDataManager jobDataManager;

  public LockedJobEntityManagerImpl(ProcessEngineConfigurationImpl processEngineConfiguration, LockedJobDataManager jobDataManager) {
    super(processEngineConfiguration);
    this.jobDataManager = jobDataManager;
  }

  @Override
  protected LockedJobDataManager getDataManager() {
    return jobDataManager;
  }

  @Override
  public LockedJobEntity createLockedJob(JobEntity te) {
    LockedJobEntity newLockedJobEntity = create();
    newLockedJobEntity.setJobHandlerConfiguration(te.getJobHandlerConfiguration());
    newLockedJobEntity.setJobHandlerType(te.getJobHandlerType());
    newLockedJobEntity.setExclusive(te.isExclusive());
    newLockedJobEntity.setRepeat(te.getRepeat());
    newLockedJobEntity.setRetries(te.getRetries());
    newLockedJobEntity.setEndDate(te.getEndDate());
    newLockedJobEntity.setExecutionId(te.getExecutionId());
    newLockedJobEntity.setProcessInstanceId(te.getProcessInstanceId());
    newLockedJobEntity.setProcessDefinitionId(te.getProcessDefinitionId());

    // Inherit tenant
    newLockedJobEntity.setTenantId(te.getTenantId());
    newLockedJobEntity.setJobType(te.getJobType());
    return newLockedJobEntity;
  }

  @Override
  public void insert(LockedJobEntity jobEntity, boolean fireCreateEvent) {

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
  public List<LockedJobEntity> findJobsByLockOwner(String lockOwner, int start, int maxNrOfJobs) {
    return jobDataManager.findJobsByLockOwner(lockOwner, start, maxNrOfJobs);
  }

  @Override
  public List<LockedJobEntity> findJobsByExecutionId(String executionId) {
    return jobDataManager.findJobsByExecutionId(executionId);
  }

  @Override
  public List<LockedJobEntity> findExclusiveJobsToExecute(String processInstanceId) {
    return jobDataManager.findExclusiveJobsToExecute(processInstanceId);
  }

  @Override
  public List<LockedJobEntity> findJobsByQueryCriteria(JobQueryImpl jobQuery, Page page) {
    return jobDataManager.findJobsByQueryCriteria(jobQuery, page);
  }

  @Override
  public List<LockedJobEntity> findJobsByTypeAndProcessDefinitionIds(String jobHandlerType, List<String> processDefinitionIds) {
    return jobDataManager.findJobsByTypeAndProcessDefinitionIds(jobHandlerType, processDefinitionIds);
  }

  @Override
  public List<LockedJobEntity> findJobsByTypeAndProcessDefinitionId(String jobHandlerType, String processDefinitionId) {
    return jobDataManager.findJobsByTypeAndProcessDefinitionId(jobHandlerType, processDefinitionId);
  }

  @Override
  public long findJobCountByQueryCriteria(JobQueryImpl jobQuery) {
    return jobDataManager.findJobCountByQueryCriteria(jobQuery);
  }

  @Override
  public List<LockedJobEntity> selectExpiredJobs(long maxLockDuration, Page page) {
    return jobDataManager.selectExpiredJobs(maxLockDuration, page);
  }
  
  // Job Execution logic ////////////////////////////////////////////////////////////////////
  
  @Override
  public void execute(LockedJobEntity jobEntity) {
    if (JobEntity.JOB_TYPE_MESSAGE.equals(jobEntity.getJobType())) {
      executeMessageJob(jobEntity);
    } else if (JobEntity.JOB_TYPE_TIMER.equals(jobEntity.getJobType())) {
      executeTimerJob(jobEntity);
    } 
  }
  
  protected void executeJobHandler(LockedJobEntity jobEntity) {
    ExecutionEntity execution = null;
    if (jobEntity.getExecutionId() != null) {
      execution = getExecutionEntityManager().findById(jobEntity.getExecutionId());
    }

    Map<String, JobHandler> jobHandlers = getProcessEngineConfiguration().getJobHandlers();
    JobHandler jobHandler = jobHandlers.get(jobEntity.getJobHandlerType());
    jobHandler.execute(jobEntity, jobEntity.getJobHandlerConfiguration(), execution, getCommandContext());
  }
  
  protected void executeMessageJob(LockedJobEntity jobEntity) {
    executeJobHandler(jobEntity);
    delete(jobEntity);
  }
  
  protected void executeTimerJob(LockedJobEntity timerEntity) {

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
      getTimerJobEntityManager().createAndCalculateNextTimer(timerEntity);
    }
  }
  
  protected void restoreExtraData(LockedJobEntity timerEntity) {
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
  
  protected boolean isValidTime(JobEntity timerEntity, Date newTimerDate) {
    BusinessCalendar businessCalendar = getProcessEngineConfiguration().getBusinessCalendarManager().getBusinessCalendar(CycleBusinessCalendar.NAME);
    return businessCalendar.validateDuedate(timerEntity.getRepeat(), timerEntity.getMaxIterations(), timerEntity.getEndDate(), newTimerDate);
  }

  public LockedJobDataManager getJobDataManager() {
    return jobDataManager;
  }

  public void setJobDataManager(LockedJobDataManager jobDataManager) {
    this.jobDataManager = jobDataManager;
  }

}
