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

import java.util.Map;

import org.activiti.bpmn.model.BoundaryEvent;
import org.activiti.bpmn.model.EventSubProcess;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.FlowNode;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.delegate.event.impl.ActivitiEventBuilder;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.EventSubscriptionEntity;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;

/**
 * @author Tijs Rademakers
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
        FlowElement activityElement = execution.getCurrentFlowElement();
        if (activityElement != null && activityElement instanceof FlowNode) {
            dispatchActivityCancelled(eventSubscription, execution, (FlowNode) activityElement, commandContext);
        }
    }

    protected void dispatchActivityCancelled(EventSubscriptionEntity eventSubscription, ExecutionEntity execution, FlowNode flowNode, CommandContext commandContext) {
        commandContext.getEventDispatcher().dispatchEvent(
                ActivitiEventBuilder.createActivityCancelledEvent(flowNode.getId(), flowNode.getName(), execution.getId(), execution.getProcessInstanceId(),
                        execution.getProcessDefinitionId(), parseActivityType(flowNode), flowNode.getBehavior().getClass().getCanonicalName(), eventSubscription));
    }
    
    protected String parseActivityType(FlowNode flowNode) {
        String elementType = flowNode.getClass().getSimpleName();
        elementType = elementType.substring(0, 1).toLowerCase() + elementType.substring(1);
        return elementType;
    }

}
