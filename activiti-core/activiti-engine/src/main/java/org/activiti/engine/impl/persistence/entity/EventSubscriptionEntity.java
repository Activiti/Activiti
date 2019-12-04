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

import java.util.Date;

import org.activiti.engine.api.internal.Internal;
import org.activiti.engine.impl.db.HasRevision;

/**


 */
@Internal
public interface EventSubscriptionEntity extends Entity, HasRevision {
  
  String getEventType();

  void setEventType(String eventType);

  String getEventName();

  void setEventName(String eventName);

  String getExecutionId();

  void setExecutionId(String executionId);
  
  ExecutionEntity getExecution();

  void setExecution(ExecutionEntity execution);

  String getProcessInstanceId();

  void setProcessInstanceId(String processInstanceId);

  String getConfiguration();

  void setConfiguration(String configuration);

  String getActivityId();

  void setActivityId(String activityId);

  Date getCreated();

  void setCreated(Date created);

  String getProcessDefinitionId();

  void setProcessDefinitionId(String processDefinitionId);

  String getTenantId();

  void setTenantId(String tenantId);

}
