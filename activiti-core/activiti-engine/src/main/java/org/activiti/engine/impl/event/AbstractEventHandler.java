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

package org.activiti.engine.impl.event;

import org.activiti.bpmn.model.*;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.delegate.event.impl.ActivitiEventBuilder;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.EventSubscriptionEntity;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;

import java.util.List;
import java.util.Map;

/**

 */
public abstract class AbstractEventHandler implements EventHandler {

  public void handleEvent(EventSubscriptionEntity eventSubscription, Object payload, CommandContext commandContext) {
    ExecutionEntity execution = eventSubscription.getExecution();
    FlowNode currentFlowElement = (FlowNode) execution.getCurrentFlowElement();

    if (currentFlowElement == null) {
      throw new ActivitiException("Error while sending signal for event subscription '" + eventSubscription.getId() + "': " + "no activity associated with event subscription");
    }

    if (payload instanceof Map) {
      @SuppressWarnings("unchecked")
      Map<String, Object> processVariables = (Map<String, Object>) payload;
      execution.setVariables(processVariables);
    }

    if (currentFlowElement instanceof BoundaryEvent || currentFlowElement instanceof EventSubProcess) {
      try {
        dispatchActivitiesCanceledIfNeeded(eventSubscription, execution, currentFlowElement, commandContext);

      } catch (RuntimeException e) {
        throw e;
      } catch (Exception e) {
        throw new ActivitiException("exception while sending signal for event subscription '" + eventSubscription + "':" + e.getMessage(), e);
      }
    }

    Context.getAgenda().planTriggerExecutionOperation(execution);
  }

  protected void dispatchActivitiesCanceledIfNeeded(EventSubscriptionEntity eventSubscription, ExecutionEntity execution, FlowElement currentFlowElement, CommandContext commandContext) {
    if (currentFlowElement instanceof BoundaryEvent) {
      BoundaryEvent boundaryEvent = (BoundaryEvent) currentFlowElement;
      if (boundaryEvent.isCancelActivity()) {
        dispatchExecutionCancelled(eventSubscription, execution, commandContext);
      }
    }
  }

  protected void dispatchExecutionCancelled(EventSubscriptionEntity eventSubscription, ExecutionEntity execution, CommandContext commandContext) {
    // subprocesses
    for (ExecutionEntity subExecution : execution.getExecutions()) {
      dispatchExecutionCancelled(eventSubscription, subExecution, commandContext);
    }

    // call activities
    ExecutionEntity subProcessInstance = commandContext.getExecutionEntityManager().findSubProcessInstanceBySuperExecutionId(execution.getId());
    if (subProcessInstance != null) {
      dispatchExecutionCancelled(eventSubscription, subProcessInstance, commandContext);
    }

    // activity with message/signal boundary events
    FlowElement flowElement = execution.getCurrentFlowElement();
    if (flowElement instanceof BoundaryEvent) {
      BoundaryEvent boundaryEvent = (BoundaryEvent) flowElement;
      if (boundaryEvent.getAttachedToRef() != null) {
        dispatchActivityCancelled(eventSubscription, execution, boundaryEvent.getAttachedToRef(), commandContext);
      }
    }
  }

  protected void dispatchActivityCancelled(EventSubscriptionEntity eventSubscription, ExecutionEntity boundaryEventExecution, FlowNode flowNode, CommandContext commandContext) {

    // Scope
    commandContext.getEventDispatcher().dispatchEvent(
        ActivitiEventBuilder.createActivityCancelledEvent(flowNode.getId(), flowNode.getName(), boundaryEventExecution.getId(),
            boundaryEventExecution.getProcessInstanceId(), boundaryEventExecution.getProcessDefinitionId(),
            parseActivityType(flowNode), eventSubscription));

    if (flowNode instanceof SubProcess) {
      // The parent of the boundary event execution will be the one on which the boundary event is set
      ExecutionEntity parentExecutionEntity = commandContext.getExecutionEntityManager().findById(boundaryEventExecution.getParentId());
      if (parentExecutionEntity != null) {
        dispatchActivityCancelledForChildExecution(eventSubscription, parentExecutionEntity, boundaryEventExecution, commandContext);
      }
    }
  }

  protected void dispatchActivityCancelledForChildExecution(EventSubscriptionEntity eventSubscription,
      ExecutionEntity parentExecutionEntity, ExecutionEntity boundaryEventExecution, CommandContext commandContext) {

    List<ExecutionEntity> executionEntities = commandContext.getExecutionEntityManager().findChildExecutionsByParentExecutionId(parentExecutionEntity.getId());
    for (ExecutionEntity childExecution : executionEntities) {

      if (!boundaryEventExecution.getId().equals(childExecution.getId())
          && childExecution.getCurrentFlowElement() != null
          && childExecution.getCurrentFlowElement() instanceof FlowNode) {

        FlowNode flowNode = (FlowNode) childExecution.getCurrentFlowElement();
        commandContext.getEventDispatcher().dispatchEvent(
            ActivitiEventBuilder.createActivityCancelledEvent(flowNode.getId(), flowNode.getName(), childExecution.getId(),
                childExecution.getProcessInstanceId(), childExecution.getProcessDefinitionId(),
                parseActivityType(flowNode), eventSubscription));

        if (childExecution.isScope()) {
          dispatchActivityCancelledForChildExecution(eventSubscription, childExecution, boundaryEventExecution, commandContext);
        }

      }

    }

  }

  protected String parseActivityType(FlowNode flowNode) {
    String elementType = flowNode.getClass().getSimpleName();
    elementType = elementType.substring(0, 1).toLowerCase() + elementType.substring(1);
    return elementType;
  }

}
