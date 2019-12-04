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
package org.activiti.engine.impl.persistence.entity.data;

import java.util.List;

import org.activiti.engine.impl.EventSubscriptionQueryImpl;
import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.persistence.entity.CompensateEventSubscriptionEntity;
import org.activiti.engine.impl.persistence.entity.EventSubscriptionEntity;
import org.activiti.engine.impl.persistence.entity.MessageEventSubscriptionEntity;
import org.activiti.engine.impl.persistence.entity.SignalEventSubscriptionEntity;

/**

 */
public interface EventSubscriptionDataManager extends DataManager<EventSubscriptionEntity> {
  
  MessageEventSubscriptionEntity createMessageEventSubscription();
  
  SignalEventSubscriptionEntity createSignalEventSubscription();
  
  CompensateEventSubscriptionEntity createCompensateEventSubscription();
  
  long findEventSubscriptionCountByQueryCriteria(EventSubscriptionQueryImpl eventSubscriptionQueryImpl);

  List<EventSubscriptionEntity> findEventSubscriptionsByQueryCriteria(EventSubscriptionQueryImpl eventSubscriptionQueryImpl, Page page);
  
  List<MessageEventSubscriptionEntity> findMessageEventSubscriptionsByProcessInstanceAndEventName(final String processInstanceId, final String eventName);

  List<SignalEventSubscriptionEntity> findSignalEventSubscriptionsByEventName(final String eventName, final String tenantId);

  List<SignalEventSubscriptionEntity> findSignalEventSubscriptionsByProcessInstanceAndEventName(final String processInstanceId, final String eventName);

  List<SignalEventSubscriptionEntity> findSignalEventSubscriptionsByNameAndExecution(final String name, final String executionId);

  List<EventSubscriptionEntity> findEventSubscriptionsByExecutionAndType(final String executionId, final String type);
  
  List<EventSubscriptionEntity> findEventSubscriptionsByProcessInstanceAndActivityId(final String processInstanceId, final String activityId, final String type);

  List<EventSubscriptionEntity> findEventSubscriptionsByExecution(final String executionId);

  List<EventSubscriptionEntity> findEventSubscriptionsByTypeAndProcessDefinitionId(String type, String processDefinitionId, String tenantId);

  List<EventSubscriptionEntity> findEventSubscriptionsByName(final String type, final String eventName, final String tenantId);

  List<EventSubscriptionEntity> findEventSubscriptionsByNameAndExecution(String type, String eventName, String executionId);

  MessageEventSubscriptionEntity findMessageStartEventSubscriptionByName(String messageName, String tenantId);

  void updateEventSubscriptionTenantId(String oldTenantId, String newTenantId);
  
  void deleteEventSubscriptionsForProcessDefinition(String processDefinitionId);

}
