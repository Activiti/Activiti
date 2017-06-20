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
package org.activiti.engine.impl.persistence.entity.data.impl.cachematcher;

import java.util.Collection;
import java.util.Map;

import org.activiti.engine.impl.persistence.CachedEntityMatcherAdapter;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;

/**

 */
public class ExecutionsByParentExecutionIdAndActivityIdEntityMatcher extends CachedEntityMatcherAdapter<ExecutionEntity> {
  
  @Override
  public boolean isRetained(ExecutionEntity executionEntity, Object parameter) {
    Map<String, Object> paramMap = (Map<String, Object>) parameter;
    String parentExecutionId = (String) paramMap.get("parentExecutionId");
    Collection<String> activityIds = (Collection<String>) paramMap.get("activityIds");
    
    return executionEntity.getParentId() != null && executionEntity.getParentId().equals(parentExecutionId)
        && executionEntity.getActivityId() != null && activityIds.contains(executionEntity.getActivityId());
  }
  
}