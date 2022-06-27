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


package org.activiti.engine.impl.bpmn.helper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.persistence.entity.CompensateEventSubscriptionEntity;
import org.activiti.engine.impl.persistence.entity.EventSubscriptionEntity;
import org.activiti.engine.impl.persistence.entity.EventSubscriptionEntityManager;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.ExecutionEntityManager;
import org.activiti.engine.impl.util.CollectionUtil;

/**
 */
public class ScopeUtil {

  /**
   * we create a separate execution for each compensation handler invocation.
   */
  public static void throwCompensationEvent(List<CompensateEventSubscriptionEntity> eventSubscriptions, DelegateExecution execution, boolean async) {

    ExecutionEntityManager executionEntityManager = Context.getCommandContext().getExecutionEntityManager();

    // first spawn the compensating executions
    for (EventSubscriptionEntity eventSubscription : eventSubscriptions) {
      ExecutionEntity compensatingExecution = null;

      // check whether compensating execution is already created (which is the case when compensating an embedded subprocess,
      // where the compensating execution is created when leaving the subprocess and holds snapshot data).
      if (eventSubscription.getConfiguration() != null) {
        compensatingExecution = executionEntityManager.findById(eventSubscription.getConfiguration());
        compensatingExecution.setParent(compensatingExecution.getProcessInstance());
        compensatingExecution.setEventScope(false);
      } else {
        compensatingExecution = executionEntityManager.createChildExecution((ExecutionEntity) execution);
        eventSubscription.setConfiguration(compensatingExecution.getId());
      }

    }

    // signal compensation events in reverse order of their 'created' timestamp
    Collections.sort(eventSubscriptions, new Comparator<EventSubscriptionEntity>() {
      public int compare(EventSubscriptionEntity o1, EventSubscriptionEntity o2) {
        return o2.getCreated().compareTo(o1.getCreated());
      }
    });

    for (CompensateEventSubscriptionEntity compensateEventSubscriptionEntity : eventSubscriptions) {
      Context.getCommandContext().getEventSubscriptionEntityManager().eventReceived(compensateEventSubscriptionEntity, null, async);
    }
  }

  /**
   * Creates a new event scope execution and moves existing event subscriptions to this new execution
   */
  public static void createCopyOfSubProcessExecutionForCompensation(ExecutionEntity subProcessExecution) {
    EventSubscriptionEntityManager eventSubscriptionEntityManager = Context.getCommandContext().getEventSubscriptionEntityManager();
    List<EventSubscriptionEntity> eventSubscriptions = eventSubscriptionEntityManager.findEventSubscriptionsByExecutionAndType(subProcessExecution.getId(), "compensate");

    List<CompensateEventSubscriptionEntity> compensateEventSubscriptions = new ArrayList<CompensateEventSubscriptionEntity>();
    for (EventSubscriptionEntity event : eventSubscriptions) {
      if (event instanceof CompensateEventSubscriptionEntity) {
        compensateEventSubscriptions.add((CompensateEventSubscriptionEntity) event);
      }
    }

    if (CollectionUtil.isNotEmpty(compensateEventSubscriptions)) {

      ExecutionEntity processInstanceExecutionEntity = subProcessExecution.getProcessInstance();

      ExecutionEntity eventScopeExecution = Context.getCommandContext().getExecutionEntityManager().createChildExecution(processInstanceExecutionEntity);
      eventScopeExecution.setActive(false);
      eventScopeExecution.setEventScope(true);
      eventScopeExecution.setCurrentFlowElement(subProcessExecution.getCurrentFlowElement());

      // copy local variables to eventScopeExecution by value. This way,
      // the eventScopeExecution references a 'snapshot' of the local variables
      new SubProcessVariableSnapshotter().setVariablesSnapshots(subProcessExecution, eventScopeExecution);

      // set event subscriptions to the event scope execution:
      for (CompensateEventSubscriptionEntity eventSubscriptionEntity : compensateEventSubscriptions) {
        eventSubscriptionEntityManager.delete(eventSubscriptionEntity);

        CompensateEventSubscriptionEntity newSubscription = eventSubscriptionEntityManager.insertCompensationEvent(
            eventScopeExecution, eventSubscriptionEntity.getActivityId());
        newSubscription.setConfiguration(eventSubscriptionEntity.getConfiguration());
        newSubscription.setCreated(eventSubscriptionEntity.getCreated());
      }

      CompensateEventSubscriptionEntity eventSubscription = eventSubscriptionEntityManager.insertCompensationEvent(
          processInstanceExecutionEntity, eventScopeExecution.getCurrentFlowElement().getId());
      eventSubscription.setConfiguration(eventScopeExecution.getId());
    }
  }
}
