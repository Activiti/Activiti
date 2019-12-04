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

import java.util.List;

import org.activiti.engine.api.internal.Internal;
import org.activiti.engine.impl.JobQueryImpl;
import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.asyncexecutor.AcquireTimerJobsRunnable;
import org.activiti.engine.impl.cmd.AcquireJobsCmd;
import org.activiti.engine.runtime.Job;

/**
 * {@link EntityManager} responsible for the {@link JobEntity} class.
 * 

 */
public interface JobEntityManager extends EntityManager<JobEntity> {
  
  /**
   * Insert the {@link JobEntity}, similar to {@link #insert(JobEntity)},
   * but returns a boolean in case the insert did not go through.
   * This could happen if the execution related to the {@link JobEntity} has been removed. 
   */
  boolean insertJobEntity(JobEntity timerJobEntity);
  
  /**
   * Returns {@link JobEntity} that are eligble to be executed.
   * 
   * For example used by the default {@link AcquireJobsCmd} command used by 
   * the default {@link AcquireTimerJobsRunnable} implementation to get async jobs 
   * that can be executed.
   */
  List<JobEntity> findJobsToExecute(Page page);

  /**
   * Returns all {@link JobEntity} instances related to on {@link ExecutionEntity}. 
   */
  List<JobEntity> findJobsByExecutionId(String executionId);

  /**
   * Returns all {@link JobEntity} instances related to on {@link ProcessDefinitionEntity}.
   */
  List<JobEntity> findJobsByProcessDefinitionId(String processDefinitionId);

  /**
   * Returns all {@link JobEntity} instances related to on {@link ProcessDefinitionEntity}.
   */
  List<JobEntity> findJobsByTypeAndProcessDefinitionId(String jobTypeTimer, String id);

  /**
   * Returns all {@link JobEntity} instances related to one process instance {@link ExecutionEntity}. 
   */
  List<JobEntity> findJobsByProcessInstanceId(String processInstanceId);

  /**
   * Returns all {@link JobEntity} instance which are expired, which means 
   * that the lock time of the {@link JobEntity} is past a certain configurable
   * date and is deemed to be in error. 
   */
  List<JobEntity> findExpiredJobs(Page page);
  
  /**
   * Executes a {@link JobQueryImpl} and returns the matching {@link JobEntity} instances.
   */
  List<Job> findJobsByQueryCriteria(JobQueryImpl jobQuery, Page page);

  /**
   * Same as {@link #findJobsByQueryCriteria(JobQueryImpl, Page)}, but only returns a count 
   * and not the instances itself.
   */
  long findJobCountByQueryCriteria(JobQueryImpl jobQuery);
  
  /**
   * Resets an expired job. These are jobs that were locked, but not completed.
   * Resetting these will make them available for being picked up by other executors.
   */
  void resetExpiredJob(String jobId);
  
  /**
   * Changes the tenantId for all jobs related to a given {@link DeploymentEntity}. 
   */
  void updateJobTenantIdForDeployment(String deploymentId, String newTenantId);
  
}