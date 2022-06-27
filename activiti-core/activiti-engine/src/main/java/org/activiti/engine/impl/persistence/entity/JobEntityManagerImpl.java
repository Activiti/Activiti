/*
 * Copyright 2010-2022 Alfresco Software, Ltd.
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

import java.util.List;

import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.impl.ActivitiEventBuilder;
import org.activiti.engine.impl.JobQueryImpl;
import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.persistence.CountingExecutionEntity;
import org.activiti.engine.impl.persistence.entity.data.DataManager;
import org.activiti.engine.impl.persistence.entity.data.JobDataManager;
import org.activiti.engine.runtime.Job;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JobEntityManagerImpl extends AbstractEntityManager<JobEntity> implements JobEntityManager {

    protected JobDataManager jobDataManager;

    public JobEntityManagerImpl(ProcessEngineConfigurationImpl processEngineConfiguration,
                                JobDataManager jobDataManager) {
        super(processEngineConfiguration);
        this.jobDataManager = jobDataManager;
    }

    @Override
    protected DataManager<JobEntity> getDataManager() {
        return jobDataManager;
    }

    @Override
    public boolean insertJobEntity(JobEntity timerJobEntity) {
        return doInsert(timerJobEntity,
                        true);
    }

    @Override
    public void insert(JobEntity jobEntity,
                       boolean fireCreateEvent) {
        doInsert(jobEntity,
                 fireCreateEvent);
    }

    protected boolean doInsert(JobEntity jobEntity,
                               boolean fireCreateEvent) {
        // add link to execution
        if (jobEntity.getExecutionId() != null) {
            ExecutionEntity execution = getExecutionEntityManager().findById(jobEntity.getExecutionId());
            if (execution != null) {
                execution.getJobs().add(jobEntity);

                // Inherit tenant if (if applicable)
                if (execution.getTenantId() != null) {
                    jobEntity.setTenantId(execution.getTenantId());
                }

                if (isExecutionRelatedEntityCountEnabled(execution)) {
                    CountingExecutionEntity countingExecutionEntity = (CountingExecutionEntity) execution;
                    countingExecutionEntity.setJobCount(countingExecutionEntity.getJobCount() + 1);
                }
            } else {
                return false;
            }
        }

        super.insert(jobEntity,
                     fireCreateEvent);
        return true;
    }

    public List<JobEntity> findJobsToExecute(Page page) {
        return jobDataManager.findJobsToExecute(page);
    }

    @Override
    public List<JobEntity> findJobsByExecutionId(String executionId) {
        return jobDataManager.findJobsByExecutionId(executionId);
    }

    @Override
    public List<JobEntity> findJobsByProcessDefinitionId(String processDefinitionId) {
        return jobDataManager.findJobsByProcessDefinitionId(processDefinitionId);
    }

    @Override
    public List<JobEntity> findJobsByTypeAndProcessDefinitionId(String jobTypeTimer,
                                                                String id) {
        return jobDataManager.findJobsByTypeAndProcessDefinitionId(jobTypeTimer,
                                                                   id);
    }

    @Override
    public List<JobEntity> findJobsByProcessInstanceId(String processInstanceId) {
        return jobDataManager.findJobsByProcessInstanceId(processInstanceId);
    }

    @Override
    public List<JobEntity> findExpiredJobs(Page page) {
        return jobDataManager.findExpiredJobs(page);
    }

    @Override
    public void resetExpiredJob(String jobId) {
        jobDataManager.resetExpiredJob(jobId);
    }

    @Override
    public List<Job> findJobsByQueryCriteria(JobQueryImpl jobQuery,
                                             Page page) {
        return jobDataManager.findJobsByQueryCriteria(jobQuery,
                                                      page);
    }

    @Override
    public long findJobCountByQueryCriteria(JobQueryImpl jobQuery) {
        return jobDataManager.findJobCountByQueryCriteria(jobQuery);
    }

    @Override
    public void updateJobTenantIdForDeployment(String deploymentId,
                                               String newTenantId) {
        jobDataManager.updateJobTenantIdForDeployment(deploymentId,
                                                      newTenantId);
    }

    @Override
    public void delete(JobEntity jobEntity) {
        super.delete(jobEntity);

        deleteExceptionByteArrayRef(jobEntity);

        removeExecutionLink(jobEntity);

        // Send event
        if (getEventDispatcher().isEnabled()) {
            getEventDispatcher().dispatchEvent(ActivitiEventBuilder.createEntityEvent(ActivitiEventType.ENTITY_DELETED,
                                                                                      this));
        }
    }

    @Override
    public void delete(JobEntity entity,
                       boolean fireDeleteEvent) {
        if (entity.getExecutionId() != null && isExecutionRelatedEntityCountEnabledGlobally()) {
            CountingExecutionEntity executionEntity = (CountingExecutionEntity) getExecutionEntityManager().findById(entity.getExecutionId());
            if (isExecutionRelatedEntityCountEnabled(executionEntity)) {
                executionEntity.setJobCount(executionEntity.getJobCount() - 1);
            }
        }
        super.delete(entity,
                     fireDeleteEvent);
    }

    /**
     * Removes the job's execution's reference to this job, if the job has an associated execution.
     * Subclasses may override to provide custom implementations.
     */
    protected void removeExecutionLink(JobEntity jobEntity) {
        if (jobEntity.getExecutionId() != null) {
            ExecutionEntity execution = getExecutionEntityManager().findById(jobEntity.getExecutionId());
            if (execution != null) {
                execution.getJobs().remove(jobEntity);
            }
        }
    }

    /**
     * Deletes a the byte array used to store the exception information.  Subclasses may override
     * to provide custom implementations.
     */
    protected void deleteExceptionByteArrayRef(JobEntity jobEntity) {
        ByteArrayRef exceptionByteArrayRef = jobEntity.getExceptionByteArrayRef();
        if (exceptionByteArrayRef != null) {
            exceptionByteArrayRef.delete();
        }
    }

    public JobDataManager getJobDataManager() {
        return jobDataManager;
    }

    public void setJobDataManager(JobDataManager jobDataManager) {
        this.jobDataManager = jobDataManager;
    }
}
