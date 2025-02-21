/*
 * Copyright 2010-2020 Alfresco Software, Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.activiti.engine.impl.persistence.entity;

import static java.util.Arrays.asList;

import java.util.Date;
import java.util.List;

import org.activiti.engine.delegate.VariableScope;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.impl.ActivitiEventBuilder;
import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.TimerJobQueryImpl;
import org.activiti.engine.impl.calendar.BusinessCalendar;
import org.activiti.engine.impl.calendar.CycleBusinessCalendar;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.jobexecutor.TimerEventHandler;
import org.activiti.engine.impl.persistence.CountingExecutionEntity;
import org.activiti.engine.impl.persistence.entity.data.TimerJobDataManager;
import org.activiti.engine.runtime.Job;
import org.apache.commons.lang3.StringUtils;

public class TimerJobEntityManagerImpl extends AbstractEntityManager<TimerJobEntity> implements TimerJobEntityManager {

    protected TimerJobDataManager jobDataManager;

    public TimerJobEntityManagerImpl(ProcessEngineConfigurationImpl processEngineConfiguration,
                                     TimerJobDataManager jobDataManager) {
        super(processEngineConfiguration);
        this.jobDataManager = jobDataManager;
    }

    @Override
    public TimerJobEntity createAndCalculateNextTimer(JobEntity timerEntity,
                                                      VariableScope variableScope) {
        int repeatValue = calculateRepeatValue(timerEntity);
        if (repeatValue != 0) {
            if (repeatValue > 0) {
                setNewRepeat(timerEntity,
                             repeatValue);
            }
            Date newTimer = calculateNextTimer(timerEntity,
                                               variableScope);
            if (newTimer != null && isValidTime(timerEntity,
                                                newTimer,
                                                variableScope)) {
                TimerJobEntity te = createTimer(timerEntity);
                te.setDuedate(newTimer);
                return te;
            }
        }
        return null;
    }

    @Override
    public List<TimerJobEntity> findTimerJobsToExecute(Page page) {
        return jobDataManager.findTimerJobsToExecute(page);
    }

    @Override
    public List<TimerJobEntity> findTimerStartEvents() {
        return jobDataManager.findTimerStartEvents();
    }

    @Override
    public List<TimerJobEntity> findJobsByTypeAndProcessDefinitionId(String jobHandlerType,
                                                                     String processDefinitionId) {
        return jobDataManager.findJobsByTypeAndProcessDefinitionId(jobHandlerType,
                                                                   processDefinitionId);
    }

    @Override
    public List<TimerJobEntity> findJobsByTypeAndProcessDefinitionKeyNoTenantId(String jobHandlerType,
                                                                                String processDefinitionKey) {
        return jobDataManager.findJobsByTypeAndProcessDefinitionKeyNoTenantId(jobHandlerType,
                                                                              processDefinitionKey);
    }

    @Override
    public List<TimerJobEntity> findJobsByTypeAndProcessDefinitionKeyAndTenantId(String jobHandlerType,
                                                                                 String processDefinitionKey,
                                                                                 String tenantId) {
        return jobDataManager.findJobsByTypeAndProcessDefinitionKeyAndTenantId(jobHandlerType,
                                                                               processDefinitionKey,
                                                                               tenantId);
    }

    @Override
    public List<TimerJobEntity> findJobsByExecutionId(String id) {
        return jobDataManager.findJobsByExecutionId(id);
    }

    @Override
    public List<TimerJobEntity> findJobsByProcessInstanceId(String id) {
        return jobDataManager.findJobsByProcessInstanceId(id);
    }

    @Override
    public List<Job> findJobsByQueryCriteria(TimerJobQueryImpl jobQuery,
                                             Page page) {
        return jobDataManager.findJobsByQueryCriteria(jobQuery,
                                                      page);
    }

    @Override
    public long findJobCountByQueryCriteria(TimerJobQueryImpl jobQuery) {
        return jobDataManager.findJobCountByQueryCriteria(jobQuery);
    }

    @Override
    public void updateJobTenantIdForDeployment(String deploymentId,
                                               String newTenantId) {
        jobDataManager.updateJobTenantIdForDeployment(deploymentId,
                                                      newTenantId);
    }

    @Override
    public boolean insertTimerJobEntity(TimerJobEntity timerJobEntity) {
        return doInsert(timerJobEntity,
                        true);
    }

    @Override
    public void insert(TimerJobEntity jobEntity) {
        insert(jobEntity,
               true);
    }

    @Override
    public void insert(TimerJobEntity jobEntity,
                       boolean fireCreateEvent) {
        doInsert(jobEntity,
                 fireCreateEvent);
    }

    protected boolean doInsert(TimerJobEntity jobEntity,
                               boolean fireCreateEvent) {
        // add link to execution
        if (jobEntity.getExecutionId() != null) {
            ExecutionEntity execution = getExecutionEntityManager().findById(jobEntity.getExecutionId());
            if (execution != null) {
                execution.getTimerJobs().add(jobEntity);

                // Inherit tenant if (if applicable)
                if (execution.getTenantId() != null) {
                    jobEntity.setTenantId(execution.getTenantId());
                }

                if (isExecutionRelatedEntityCountEnabled(execution)) {
                    CountingExecutionEntity countingExecutionEntity = (CountingExecutionEntity) execution;
                    countingExecutionEntity.setTimerJobCount(countingExecutionEntity.getTimerJobCount() + 1);
                }
            } else {
                // In case the job has an executionId, but the Execution is not found,
                // it means that for example for a boundary timer event on a user task,
                // the task has been completed and the Execution and job have been removed.
                return false;
            }
        }

        super.insert(jobEntity,
                     fireCreateEvent);
        return true;
    }

    @Override
    public void delete(TimerJobEntity jobEntity) {
        super.delete(jobEntity);

        deleteExceptionByteArrayRef(jobEntity);
        removeExecutionLink(jobEntity);

        if (jobEntity.getExecutionId() != null && isExecutionRelatedEntityCountEnabledGlobally()) {
            CountingExecutionEntity executionEntity = (CountingExecutionEntity) getExecutionEntityManager().findById(jobEntity.getExecutionId());
            if (isExecutionRelatedEntityCountEnabled(executionEntity)) {
                executionEntity.setTimerJobCount(executionEntity.getTimerJobCount() - 1);
            }
        }

        // Send event
        if (getEventDispatcher().isEnabled()) {
            getEventDispatcher().dispatchEvent(ActivitiEventBuilder.createEntityEvent(ActivitiEventType.ENTITY_DELETED,
                                                                                      this));
        }
    }

    /**
     * Removes the job's execution's reference to this job, if the job has an associated execution.
     * Subclasses may override to provide custom implementations.
     */
    protected void removeExecutionLink(TimerJobEntity jobEntity) {
        if (jobEntity.getExecutionId() != null) {
            ExecutionEntity execution = getExecutionEntityManager().findById(jobEntity.getExecutionId());
            if (execution != null) {
                execution.getTimerJobs().remove(jobEntity);
            }
        }
    }

    /**
     * Deletes a the byte array used to store the exception information.  Subclasses may override
     * to provide custom implementations.
     */
    protected void deleteExceptionByteArrayRef(TimerJobEntity jobEntity) {
        ByteArrayRef exceptionByteArrayRef = jobEntity.getExceptionByteArrayRef();
        if (exceptionByteArrayRef != null) {
            exceptionByteArrayRef.delete();
        }
    }

    protected TimerJobEntity createTimer(JobEntity te) {
        TimerJobEntity newTimerEntity = create();
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
        newTimerEntity.setJobType(JobEntity.JOB_TYPE_TIMER);
        return newTimerEntity;
    }

    protected void setNewRepeat(JobEntity timerEntity,
                                int newRepeatValue) {
        List<String> expression = asList(timerEntity.getRepeat().split("/"));
        expression = expression.subList(1,
                                        expression.size());
        StringBuilder repeatBuilder = new StringBuilder("R");
        repeatBuilder.append(newRepeatValue);
        for (String value : expression) {
            repeatBuilder.append("/");
            repeatBuilder.append(value);
        }
        timerEntity.setRepeat(repeatBuilder.toString());
    }

    protected boolean isValidTime(JobEntity timerEntity,
                                  Date newTimerDate,
                                  VariableScope variableScope) {
        BusinessCalendar businessCalendar = getProcessEngineConfiguration().getBusinessCalendarManager().getBusinessCalendar(
                getBusinessCalendarName(TimerEventHandler.geCalendarNameFromConfiguration(timerEntity.getJobHandlerConfiguration()),
                                        variableScope));
        return businessCalendar.validateDuedate(timerEntity.getRepeat(),
                                                timerEntity.getMaxIterations(),
                                                timerEntity.getEndDate(),
                                                newTimerDate);
    }

    protected Date calculateNextTimer(JobEntity timerEntity,
                                      VariableScope variableScope) {
        BusinessCalendar businessCalendar = getProcessEngineConfiguration().getBusinessCalendarManager().getBusinessCalendar(
                getBusinessCalendarName(TimerEventHandler.geCalendarNameFromConfiguration(timerEntity.getJobHandlerConfiguration()),
                                        variableScope));
        return businessCalendar.resolveDuedate(timerEntity.getRepeat(),
                                               timerEntity.getMaxIterations());
    }

    protected int calculateRepeatValue(JobEntity timerEntity) {
        int times = -1;
        List<String> expression = asList(timerEntity.getRepeat().split("/"));
        if (expression.size() > 1 && expression.get(0).startsWith("R") && expression.get(0).length() > 1) {
            times = Integer.parseInt(expression.get(0).substring(1));
            if (times > 0) {
                times--;
            }
        }
        return times;
    }

    protected String getBusinessCalendarName(String calendarName,
                                             VariableScope variableScope) {
        String businessCalendarName = CycleBusinessCalendar.NAME;
        if (StringUtils.isNotEmpty(calendarName)) {
            businessCalendarName = (String) Context.getProcessEngineConfiguration().getExpressionManager()
                    .createExpression(calendarName).getValue(variableScope);
        }
        return businessCalendarName;
    }

    protected TimerJobDataManager getDataManager() {
        return jobDataManager;
    }

    public void setJobDataManager(TimerJobDataManager jobDataManager) {
        this.jobDataManager = jobDataManager;
    }
}
