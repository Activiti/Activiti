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
import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.SuspendedJobQueryImpl;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.persistence.CountingExecutionEntity;
import org.activiti.engine.impl.persistence.entity.data.SuspendedJobDataManager;
import org.activiti.engine.runtime.Job;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SuspendedJobEntityManagerImpl extends AbstractEntityManager<SuspendedJobEntity> implements SuspendedJobEntityManager {


    protected SuspendedJobDataManager jobDataManager;

    public SuspendedJobEntityManagerImpl(ProcessEngineConfigurationImpl processEngineConfiguration,
                                         SuspendedJobDataManager jobDataManager) {
        super(processEngineConfiguration);
        this.jobDataManager = jobDataManager;
    }

    @Override
    public List<SuspendedJobEntity> findJobsByExecutionId(String id) {
        return jobDataManager.findJobsByExecutionId(id);
    }

    @Override
    public List<SuspendedJobEntity> findJobsByProcessInstanceId(String id) {
        return jobDataManager.findJobsByProcessInstanceId(id);
    }

    @Override
    public List<Job> findJobsByQueryCriteria(SuspendedJobQueryImpl jobQuery,
                                             Page page) {
        return jobDataManager.findJobsByQueryCriteria(jobQuery,
                                                      page);
    }

    @Override
    public long findJobCountByQueryCriteria(SuspendedJobQueryImpl jobQuery) {
        return jobDataManager.findJobCountByQueryCriteria(jobQuery);
    }

    @Override
    public void updateJobTenantIdForDeployment(String deploymentId,
                                               String newTenantId) {
        jobDataManager.updateJobTenantIdForDeployment(deploymentId,
                                                      newTenantId);
    }

    @Override
    public void insert(SuspendedJobEntity jobEntity,
                       boolean fireCreateEvent) {

        // add link to execution
        if (jobEntity.getExecutionId() != null) {
            ExecutionEntity execution = getExecutionEntityManager().findById(jobEntity.getExecutionId());

            // Inherit tenant if (if applicable)
            if (execution.getTenantId() != null) {
                jobEntity.setTenantId(execution.getTenantId());
            }

            if (isExecutionRelatedEntityCountEnabled(execution)) {
                CountingExecutionEntity countingExecutionEntity = (CountingExecutionEntity) execution;
                countingExecutionEntity.setSuspendedJobCount(countingExecutionEntity.getSuspendedJobCount() + 1);
            }
        }

        super.insert(jobEntity,
                     fireCreateEvent);
    }

    @Override
    public void insert(SuspendedJobEntity jobEntity) {
        insert(jobEntity,
               true);
    }

    @Override
    public void delete(SuspendedJobEntity jobEntity) {
        super.delete(jobEntity);

        deleteExceptionByteArrayRef(jobEntity);

        if (jobEntity.getExecutionId() != null && isExecutionRelatedEntityCountEnabledGlobally()) {
            CountingExecutionEntity executionEntity = (CountingExecutionEntity) getExecutionEntityManager().findById(jobEntity.getExecutionId());
            if (isExecutionRelatedEntityCountEnabled(executionEntity)) {
                executionEntity.setSuspendedJobCount(executionEntity.getSuspendedJobCount() - 1);
            }
        }

        // Send event
        if (getEventDispatcher().isEnabled()) {
            getEventDispatcher().dispatchEvent(ActivitiEventBuilder.createEntityEvent(ActivitiEventType.ENTITY_DELETED,
                                                                                      this));
        }
    }

    /**
     * Deletes a the byte array used to store the exception information.  Subclasses may override
     * to provide custom implementations.
     */
    protected void deleteExceptionByteArrayRef(SuspendedJobEntity jobEntity) {
        ByteArrayRef exceptionByteArrayRef = jobEntity.getExceptionByteArrayRef();
        if (exceptionByteArrayRef != null) {
            exceptionByteArrayRef.delete();
        }
    }

    protected SuspendedJobEntity createSuspendedJob(AbstractJobEntity job) {
        SuspendedJobEntity newSuspendedJobEntity = create();
        newSuspendedJobEntity.setJobHandlerConfiguration(job.getJobHandlerConfiguration());
        newSuspendedJobEntity.setJobHandlerType(job.getJobHandlerType());
        newSuspendedJobEntity.setExclusive(job.isExclusive());
        newSuspendedJobEntity.setRepeat(job.getRepeat());
        newSuspendedJobEntity.setRetries(job.getRetries());
        newSuspendedJobEntity.setEndDate(job.getEndDate());
        newSuspendedJobEntity.setExecutionId(job.getExecutionId());
        newSuspendedJobEntity.setProcessInstanceId(job.getProcessInstanceId());
        newSuspendedJobEntity.setProcessDefinitionId(job.getProcessDefinitionId());

        // Inherit tenant
        newSuspendedJobEntity.setTenantId(job.getTenantId());
        newSuspendedJobEntity.setJobType(job.getJobType());
        return newSuspendedJobEntity;
    }

    protected SuspendedJobDataManager getDataManager() {
        return jobDataManager;
    }

    public void setJobDataManager(SuspendedJobDataManager jobDataManager) {
        this.jobDataManager = jobDataManager;
    }
}
