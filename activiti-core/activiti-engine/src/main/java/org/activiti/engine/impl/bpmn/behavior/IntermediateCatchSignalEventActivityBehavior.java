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

package org.activiti.engine.impl.bpmn.behavior;

import java.util.List;

import org.activiti.bpmn.model.Signal;
import org.activiti.bpmn.model.SignalEventDefinition;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.history.DeleteReason;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.EventSubscriptionEntity;
import org.activiti.engine.impl.persistence.entity.EventSubscriptionEntityManager;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.SignalEventSubscriptionEntity;
import org.apache.commons.lang3.StringUtils;

public class IntermediateCatchSignalEventActivityBehavior extends IntermediateCatchEventActivityBehavior {

  private static final long serialVersionUID = 1L;

  protected SignalEventDefinition signalEventDefinition;
  protected Signal signal;

  public IntermediateCatchSignalEventActivityBehavior(SignalEventDefinition signalEventDefinition, Signal signal) {
    this.signalEventDefinition = signalEventDefinition;
    this.signal = signal;
  }

  public void execute(DelegateExecution execution) {
    CommandContext commandContext = Context.getCommandContext();
    ExecutionEntity executionEntity = (ExecutionEntity) execution;

    String signalName = null;
    if (StringUtils.isNotEmpty(signalEventDefinition.getSignalRef())) {
      signalName = signalEventDefinition.getSignalRef();
    } else {
      Expression signalExpression = commandContext.getProcessEngineConfiguration().getExpressionManager()
          .createExpression(signalEventDefinition.getSignalExpression());
      signalName = signalExpression.getValue(execution).toString();
    }

    commandContext.getEventSubscriptionEntityManager().insertSignalEvent(signalName, signal, executionEntity);
  }

  @Override
  public void trigger(DelegateExecution execution, String triggerName, Object triggerData) {
    ExecutionEntity executionEntity = deleteSignalEventSubscription(execution);
    leaveIntermediateCatchEvent(executionEntity);
  }

  @Override
  public void eventCancelledByEventGateway(DelegateExecution execution) {
    deleteSignalEventSubscription(execution);
    Context.getCommandContext().getExecutionEntityManager().deleteExecutionAndRelatedData((ExecutionEntity) execution,
        DeleteReason.EVENT_BASED_GATEWAY_CANCEL);
  }

  protected ExecutionEntity deleteSignalEventSubscription(DelegateExecution execution) {
    ExecutionEntity executionEntity = (ExecutionEntity) execution;

    String eventName = null;
    if (signal != null) {
      eventName = signal.getName();
    } else {
      eventName = signalEventDefinition.getSignalRef();
    }

    EventSubscriptionEntityManager eventSubscriptionEntityManager = Context.getCommandContext().getEventSubscriptionEntityManager();
    List<EventSubscriptionEntity> eventSubscriptions = executionEntity.getEventSubscriptions();
    for (EventSubscriptionEntity eventSubscription : eventSubscriptions) {
      if (eventSubscription instanceof SignalEventSubscriptionEntity && eventSubscription.getEventName().equals(eventName)) {

        eventSubscriptionEntityManager.delete(eventSubscription);
      }
    }
    return executionEntity;
  }
}
