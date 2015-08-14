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

import java.util.List;

import org.activiti.bpmn.model.Signal;
import org.activiti.bpmn.model.SignalEventDefinition;
import org.activiti.bpmn.model.ThrowEvent;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.impl.ActivitiEventBuilder;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.delegate.ActivityExecution;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.SignalEventSubscriptionEntity;

/**
 * @author Tijs Rademakers
 */
public class IntermediateThrowSignalEventActivityBehavior extends AbstractBpmnActivityBehavior {

  private static final long serialVersionUID = -2961893934810190972L;

  protected final SignalEventDefinition signalEventDefinition;
  protected String signalEventName;
  protected boolean processInstanceScope;

  public IntermediateThrowSignalEventActivityBehavior(ThrowEvent throwEvent, SignalEventDefinition signalEventDefinition, Signal signal) {
    if (signal != null) {
      signalEventName = signal.getName();
      if (Signal.SCOPE_PROCESS_INSTANCE.equals(signal.getScope())) {
        this.processInstanceScope = true;
      }
    } else {
      signalEventName = signalEventDefinition.getSignalRef();
    }
    this.signalEventDefinition = signalEventDefinition;
  }

  public void execute(ActivityExecution execution) {

    CommandContext commandContext = Context.getCommandContext();

    List<SignalEventSubscriptionEntity> subscriptionEntities = null;
    if (processInstanceScope) {
      subscriptionEntities = commandContext.getEventSubscriptionEntityManager().findSignalEventSubscriptionsByProcessInstanceAndEventName(execution.getProcessInstanceId(), signalEventName);
    } else {
      subscriptionEntities = commandContext.getEventSubscriptionEntityManager().findSignalEventSubscriptionsByEventName(signalEventName, execution.getTenantId());
    }

    for (SignalEventSubscriptionEntity signalEventSubscriptionEntity : subscriptionEntities) {
      Context.getProcessEngineConfiguration().getEventDispatcher().dispatchEvent(
          ActivitiEventBuilder.createSignalEvent(ActivitiEventType.ACTIVITY_SIGNALED, signalEventSubscriptionEntity.getActivityId(), signalEventName, 
              null, signalEventSubscriptionEntity.getExecutionId(), signalEventSubscriptionEntity.getProcessInstanceId(), 
              signalEventSubscriptionEntity.getProcessDefinitionId()));
      
      signalEventSubscriptionEntity.eventReceived(null, signalEventDefinition.isAsync());
    }

    commandContext.getAgenda().planTakeOutgoingSequenceFlowsOperation(execution);
//    if (execution.getCurrentActivityId() != null) { // don't continue if process has already finished
//      leave(execution);
//    }
  }

}
