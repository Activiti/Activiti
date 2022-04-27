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
import org.activiti.engine.impl.DeadLetterJobQueryImpl;
import org.activiti.engine.impl.JobQueryImpl;
import org.activiti.engine.impl.Page;
import org.activiti.engine.runtime.Job;


@Internal
public interface DeadLetterJobEntityManager extends EntityManager<DeadLetterJobEntity> {

  /**
   * Returns all {@link DeadLetterJobEntity} instances related to on {@link ExecutionEntity}.
   */
  List<DeadLetterJobEntity> findJobsByExecutionId(String id);

  /**
   * Executes a {@link JobQueryImpl} and returns the matching {@link DeadLetterJobEntity} instances.
   */
  List<Job> findJobsByQueryCriteria(DeadLetterJobQueryImpl jobQuery, Page page);

  /**
   * Same as {@link #findJobsByQueryCriteria(DeadLetterJobQueryImpl, Page)}, but only returns a count
   * and not the instances itself.
   */
  long findJobCountByQueryCriteria(DeadLetterJobQueryImpl jobQuery);

  /**
   * Changes the tenantId for all jobs related to a given {@link DeploymentEntity}.
   */
  void updateJobTenantIdForDeployment(String deploymentId, String newTenantId);
}
