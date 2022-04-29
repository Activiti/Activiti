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

import org.activiti.bpmn.model.Signal;
import org.activiti.engine.api.internal.Internal;
import org.activiti.engine.impl.EventSubscriptionQueryImpl;
import org.activiti.engine.impl.Page;


@Internal
public interface EventSubscriptionEntityManager extends EntityManager<EventSubscriptionEntity> {

  /* Create entity */

  MessageEventSubscriptionEntity createMessageEventSubscription();

  SignalEventSubscriptionEntity createSignalEventSubscription();

  CompensateEventSubscriptionEntity createCompensateEventSubscription();


  /* Create and insert */

  SignalEventSubscriptionEntity insertSignalEvent(String signalName, Signal signal, ExecutionEntity execution);

  MessageEventSubscriptionEntity insertMessageEvent(String messageName, ExecutionEntity execution);

  CompensateEventSubscriptionEntity insertCompensationEvent(ExecutionEntity execution, String activityId);


  /* Update */

  void updateEventSubscriptionTenantId(String oldTenantId, String newTenantId);


  /* Delete */

  void deleteEventSubscriptionsForProcessDefinition(String processDefinitionId);


  /* Event receival */

  void eventReceived(EventSubscriptionEntity eventSubscriptionEntity, Object payload, boolean processASync);


  /* Find (generic) */

  List<EventSubscriptionEntity> findEventSubscriptionsByName(String type, String eventName, String tenantId);

  List<EventSubscriptionEntity> findEventSubscriptionsByNameAndExecution(String type, String eventName, String executionId);

  List<EventSubscriptionEntity> findEventSubscriptionsByExecution(String executionId);

  List<EventSubscriptionEntity> findEventSubscriptionsByExecutionAndType(String executionId, String type);

  List<EventSubscriptionEntity> findEventSubscriptionsByProcessInstanceAndActivityId(String processInstanceId, String activityId, String type);

  List<EventSubscriptionEntity> findEventSubscriptionsByTypeAndProcessDefinitionId(String type, String processDefinitionId, String tenantId);

  List<EventSubscriptionEntity> findEventSubscriptionsByQueryCriteria(EventSubscriptionQueryImpl eventSubscriptionQueryImpl, Page page);

  long findEventSubscriptionCountByQueryCriteria(EventSubscriptionQueryImpl eventSubscriptionQueryImpl);


  /* Find (signal) */

  List<SignalEventSubscriptionEntity> findSignalEventSubscriptionsByEventName(String eventName, String tenantId);

  List<SignalEventSubscriptionEntity> findSignalEventSubscriptionsByProcessInstanceAndEventName(String processInstanceId, String eventName);

  List<SignalEventSubscriptionEntity> findSignalEventSubscriptionsByNameAndExecution(String name, String executionId);


  /* Find (message) */

  MessageEventSubscriptionEntity findMessageStartEventSubscriptionByName(String messageName, String tenantId);

  List<MessageEventSubscriptionEntity> findMessageEventSubscriptionsByProcessInstanceAndEventName(String processInstanceId, String eventName);


  /* Find (compensation) */

  List<CompensateEventSubscriptionEntity> findCompensateEventSubscriptionsByExecutionId(String executionId);

  List<CompensateEventSubscriptionEntity> findCompensateEventSubscriptionsByExecutionIdAndActivityId(String executionId, String activityId);

  List<CompensateEventSubscriptionEntity> findCompensateEventSubscriptionsByProcessInstanceIdAndActivityId(String processInstanceId, String activityId);


}
