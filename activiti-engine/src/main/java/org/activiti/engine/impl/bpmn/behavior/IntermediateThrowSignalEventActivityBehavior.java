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

package org.activiti.engine.impl.bpmn.behavior;

import org.activiti.bpmn.model.Signal;
import org.activiti.bpmn.model.SignalEventDefinition;
import org.activiti.bpmn.model.ThrowEvent;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.impl.ActivitiEventBuilder;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.EventSubscriptionEntityManager;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.SignalEventSubscriptionEntity;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * @author Tijs Rademakers
 */
public class IntermediateThrowSignalEventActivityBehavior extends AbstractBpmnActivityBehavior {

  private static final long serialVersionUID = 1L;

  protected final SignalEventDefinition signalEventDefinition;
  protected String signalEventName;
  protected String signalExpression;
  protected boolean processInstanceScope;

  public IntermediateThrowSignalEventActivityBehavior(ThrowEvent throwEvent, SignalEventDefinition signalEventDefinition, Signal signal) {
    if (signal != null) {
      signalEventName = signal.getName();
      if (Signal.SCOPE_PROCESS_INSTANCE.equals(signal.getScope())) {
        this.processInstanceScope = true;
      }
    } else if (StringUtils.isNotEmpty(signalEventDefinition.getSignalRef())) {
      signalEventName = signalEventDefinition.getSignalRef();
    } else {
      signalExpression = signalEventDefinition.getSignalExpression();
    }

    this.signalEventDefinition = signalEventDefinition;
  }

  public void execute(DelegateExecution execution) {

    CommandContext commandContext = Context.getCommandContext();

    String eventSubscriptionName = null;
    if (signalEventName != null) {
      eventSubscriptionName = signalEventName;
    } else {
      Expression expressionObject = commandContext.getProcessEngineConfiguration().getExpressionManager().createExpression(signalExpression);
      eventSubscriptionName = expressionObject.getValue(execution).toString();
    }

    EventSubscriptionEntityManager eventSubscriptionEntityManager = commandContext.getEventSubscriptionEntityManager();
    List<SignalEventSubscriptionEntity> subscriptionEntities = null;
    if (processInstanceScope) {
      subscriptionEntities = eventSubscriptionEntityManager
          .findSignalEventSubscriptionsByProcessInstanceAndEventName(execution.getProcessInstanceId(), eventSubscriptionName);
    } else {
      subscriptionEntities = eventSubscriptionEntityManager
          .findSignalEventSubscriptionsByEventName(eventSubscriptionName, execution.getTenantId());
    }

    for (SignalEventSubscriptionEntity signalEventSubscriptionEntity : subscriptionEntities) {
      Context.getProcessEngineConfiguration().getEventDispatcher().dispatchEvent(
          ActivitiEventBuilder.createSignalEvent(ActivitiEventType.ACTIVITY_SIGNALED, signalEventSubscriptionEntity.getActivityId(), eventSubscriptionName,
              null, signalEventSubscriptionEntity.getExecutionId(), signalEventSubscriptionEntity.getProcessInstanceId(),
              signalEventSubscriptionEntity.getProcessDefinitionId()));

      eventSubscriptionEntityManager.eventReceived(signalEventSubscriptionEntity, null, signalEventDefinition.isAsync());
    }

    Context.getAgenda().planTakeOutgoingSequenceFlowsOperation((ExecutionEntity) execution, true);
  }

}
