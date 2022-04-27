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

import java.util.List;

import org.activiti.engine.api.internal.Internal;
import org.activiti.engine.impl.JobQueryImpl;
import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.SuspendedJobQueryImpl;
import org.activiti.engine.runtime.Job;


@Internal
public interface SuspendedJobEntityManager extends EntityManager<SuspendedJobEntity> {

  /**
   * Returns all {@link SuspendedJobEntity} instances related to on {@link ExecutionEntity}.
   */
  List<SuspendedJobEntity> findJobsByExecutionId(String id);

  /**
   * Returns all {@link SuspendedJobEntity} instances related to on {@link ExecutionEntity}.
   */
  List<SuspendedJobEntity> findJobsByProcessInstanceId(String id);

  /**
   * Executes a {@link JobQueryImpl} and returns the matching {@link SuspendedJobEntity} instances.
   */
  List<Job> findJobsByQueryCriteria(SuspendedJobQueryImpl jobQuery, Page page);

  /**
   * Same as {@link #findJobsByQueryCriteria(SuspendedJobQueryImpl, Page)}, but only returns a count
   * and not the instances itself.
   */
  long findJobCountByQueryCriteria(SuspendedJobQueryImpl jobQuery);

  /**
   * Changes the tenantId for all jobs related to a given {@link DeploymentEntity}.
   */
  void updateJobTenantIdForDeployment(String deploymentId, String newTenantId);

}
