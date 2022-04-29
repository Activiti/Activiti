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

import org.activiti.engine.api.internal.Internal;
import org.activiti.engine.delegate.VariableScope;
import org.activiti.engine.impl.JobQueryImpl;
import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.TimerJobQueryImpl;
import org.activiti.engine.runtime.Job;

/**
 * {@link EntityManager} responsible for {@link TimerJobEntity} instances.
 *
 */
@Internal
public interface TimerJobEntityManager extends EntityManager<TimerJobEntity> {

  /**
   * Insert the {@link TimerJobEntity}, similar to {@link #insert(TimerJobEntity)},
   * but returns a boolean in case the insert did not go through.
   * This could happen if the execution related to the {@link TimerJobEntity}
   * has been removed (for example due to a task complete for a timer boundary on that task).
   */
  boolean insertTimerJobEntity(TimerJobEntity timerJobEntity);

  /**
   * Returns the {@link TimerJobEntity} instances that are elegible to execute,
   * meaning the due date of the timer has been passed.
   */
  List<TimerJobEntity> findTimerJobsToExecute(Page page);

  /**
   * Returns the {@link TimerJobEntity} for a given process definition.
   *
   * This is for example used when deleting a process definition: it finds
   * the {@link TimerJobEntity} representing the timer start events.
   */
  List<TimerJobEntity> findJobsByTypeAndProcessDefinitionId(String type, String processDefinitionId);

  /**
   * The same as {@link #findJobsByTypeAndProcessDefinitionId(String, String)}, but
   * by key and for a specific tenantId.
   */
  List<TimerJobEntity> findJobsByTypeAndProcessDefinitionKeyAndTenantId(String type, String processDefinitionKey, String tenantId);

  /**
   * The same as {@link #findJobsByTypeAndProcessDefinitionId(String, String)}, but
   * by key and specifically for the 'no tenant' mode.
   */
  List<TimerJobEntity> findJobsByTypeAndProcessDefinitionKeyNoTenantId(String type, String processDefinitionKey);

  /**
   * Returns all {@link TimerJobEntity} instances related to on {@link ExecutionEntity}.
   */
  List<TimerJobEntity> findJobsByExecutionId(String id);

  /**
   * Returns all {@link TimerJobEntity} instances related to on {@link ExecutionEntity}.
   */
  List<TimerJobEntity> findJobsByProcessInstanceId(String id);

  /**
   * Executes a {@link JobQueryImpl} and returns the matching {@link TimerJobEntity} instances.
   */
  List<Job> findJobsByQueryCriteria(TimerJobQueryImpl jobQuery, Page page);

  /**
   * Same as {@link #findJobsByQueryCriteria(TimerJobQueryImpl, Page)}, but only returns a count
   * and not the instances itself.
   */
  long findJobCountByQueryCriteria(TimerJobQueryImpl jobQuery);

  /**
   * Creates a new {@link TimerJobEntity}, typically when a timer is used in a
   * repeating way. The returns {@link TimerJobEntity} is not yet inserted.
   *
   * Returns null if the timer has finished its repetitions.
   */
  TimerJobEntity createAndCalculateNextTimer(JobEntity timerEntity, VariableScope variableScope);

  /**
   * Changes the tenantId for all jobs related to a given {@link DeploymentEntity}.
   */
  void updateJobTenantIdForDeployment(String deploymentId, String newTenantId);

}
