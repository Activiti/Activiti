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

package org.activiti.engine.impl.persistence.entity.data.impl.cachematcher;

import java.util.Collection;

import org.activiti.engine.impl.persistence.CachedEntityMatcher;
import org.activiti.engine.impl.persistence.cache.CachedEntity;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;


public class ExecutionsWithSameRootProcessInstanceIdMatcher implements CachedEntityMatcher<ExecutionEntity> {

  @Override
  public boolean isRetained(Collection<ExecutionEntity> databaseEntities, Collection<CachedEntity> cachedEntities, ExecutionEntity entity, Object param) {
    ExecutionEntity executionEntity = getMatchingExecution(databaseEntities, cachedEntities, (String) param);
    return(executionEntity.getRootProcessInstanceId() != null
        && executionEntity.getRootProcessInstanceId().equals(entity.getRootProcessInstanceId()));
  }

  public ExecutionEntity getMatchingExecution(Collection<ExecutionEntity> databaseEntities, Collection<CachedEntity> cachedEntities, String executionId) {

    // Doing some preprocessing here: we need to find the execution that matches the provided execution id,
    // as we need to match the root process instance id later on.

    if (cachedEntities != null) {
      for (CachedEntity cachedEntity : cachedEntities) {
        ExecutionEntity executionEntity = (ExecutionEntity) cachedEntity.getEntity();
        if (executionId.equals(executionEntity.getId())) {
          return executionEntity;
        }
      }
    }

    if (databaseEntities != null) {
      for (ExecutionEntity databaseExecutionEntity : databaseEntities) {
        if (executionId.equals(databaseExecutionEntity.getId())) {
          return databaseExecutionEntity;
        }
      }
    }

    return null;
  }

}
