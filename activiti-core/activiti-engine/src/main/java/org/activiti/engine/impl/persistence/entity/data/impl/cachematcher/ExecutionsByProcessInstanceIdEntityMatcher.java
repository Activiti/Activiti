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

package org.activiti.engine.impl.persistence.entity.data.impl.cachematcher;

import org.activiti.engine.impl.persistence.CachedEntityMatcherAdapter;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;


public class ExecutionsByProcessInstanceIdEntityMatcher extends CachedEntityMatcherAdapter<ExecutionEntity> {

  @Override
  public boolean isRetained(ExecutionEntity entity, Object parameter) {
    // parameter = process instance execution id
      return entity.getProcessInstanceId() != null
          && entity.getProcessInstanceId().equals((String) parameter)
          && entity.getParentId() != null;
  }

}
