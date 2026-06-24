/*
 * Copyright 2010-2026 Hyland Software, Inc. and its affiliates.
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

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.activiti.bpmn.model.EventSubProcess;
import org.activiti.bpmn.model.StartEvent;
import org.activiti.bpmn.model.SubProcess;
import org.activiti.bpmn.model.ValuedDataObject;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.history.DeleteReason;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.EventSubscriptionEntity;
import org.activiti.engine.impl.persistence.entity.EventSubscriptionEntityManager;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.ExecutionEntityManager;

/**
 * Common base for BPMN 2.0 event sub-process start event behaviors (message, signal, ...).
 * <p>
 * Encapsulates the shared lifecycle:
 * <ul>
 *   <li>{@link #execute(DelegateExecution)} marks the execution as a scope and initializes
 *       data objects declared on the event sub-process.</li>
 *   <li>{@link #trigger(DelegateExecution, String, Object)} handles interruption of sibling
 *       executions (for interrupting start events), removes the matching event subscription,
 *       lets subclasses perform any post-consumption bookkeeping, and finally leaves the
 *       outgoing flow from the start event.</li>
 * </ul>
 * Subclasses provide the event-type specific bits: how to resolve the event name and how
 * to identify the matching subscription. They may additionally override
 * {@link #onSubscriptionConsumed} to e.g. re-create a subscription for non-interrupting events.
 */
public abstract class AbstractEventSubProcessStartEventActivityBehavior extends AbstractBpmnActivityBehavior {

    private static final long serialVersionUID = 1L;

    @Override
    public void execute(DelegateExecution execution) {
        StartEvent startEvent = (StartEvent) execution.getCurrentFlowElement();
        EventSubProcess eventSubProcess = (EventSubProcess) startEvent.getSubProcess();

        execution.setScope(true);

        // initialize the template-defined data objects as variables
        Map<String, Object> dataObjectVars = processDataObjects(eventSubProcess.getDataObjects());
        if (dataObjectVars != null) {
            execution.setVariablesLocal(dataObjectVars);
        }
    }

    @Override
    public void trigger(DelegateExecution execution, String triggerName, Object triggerData) {
        CommandContext commandContext = Context.getCommandContext();
        ExecutionEntityManager executionEntityManager = commandContext.getExecutionEntityManager();
        ExecutionEntity executionEntity = (ExecutionEntity) execution;

        StartEvent startEvent = (StartEvent) execution.getCurrentFlowElement();
        if (startEvent.isInterrupting()) {
            interruptSiblingExecutions(executionEntityManager, executionEntity, startEvent);
        }

        String eventName = resolveEventName(execution);

        EventSubscriptionEntityManager eventSubscriptionEntityManager =
            commandContext.getEventSubscriptionEntityManager();
        List<EventSubscriptionEntity> eventSubscriptions = executionEntity.getEventSubscriptions();
        for (EventSubscriptionEntity eventSubscription : eventSubscriptions) {
            if (matchesSubscription(eventSubscription, eventName)) {
                eventSubscriptionEntityManager.delete(eventSubscription);
                onSubscriptionConsumed(
                    executionEntity,
                    startEvent,
                    eventName,
                    executionEntityManager,
                    eventSubscriptionEntityManager
                );
            }
        }

        executionEntity.setCurrentFlowElement(
            (SubProcess) executionEntity.getCurrentFlowElement().getParentContainer()
        );
        executionEntity.setScope(true);

        ExecutionEntity outgoingFlowExecution = executionEntityManager.createChildExecution(executionEntity);
        outgoingFlowExecution.setCurrentFlowElement(startEvent);

        leave(outgoingFlowExecution);
    }

    /**
     * Resolve the event name to match against pending subscriptions on this execution.
     */
    protected abstract String resolveEventName(DelegateExecution execution);

    /**
     * Whether the given subscription corresponds to this start event's event type and name.
     */
    protected abstract boolean matchesSubscription(EventSubscriptionEntity eventSubscription, String eventName);

    /**
     * Hook invoked after a matching subscription has been removed. Default implementation
     * is a no-op; subclasses may override e.g. to re-arm a subscription on non-interrupting
     * start events.
     */
    protected void onSubscriptionConsumed(
        ExecutionEntity executionEntity,
        StartEvent startEvent,
        String eventName,
        ExecutionEntityManager executionEntityManager,
        EventSubscriptionEntityManager eventSubscriptionEntityManager
    ) {
        // no-op by default
    }

    private void interruptSiblingExecutions(
        ExecutionEntityManager executionEntityManager,
        ExecutionEntity executionEntity,
        StartEvent startEvent
    ) {
        List<ExecutionEntity> childExecutions = executionEntityManager.findChildExecutionsByParentExecutionId(
            executionEntity.getParentId()
        );
        for (ExecutionEntity childExecution : childExecutions) {
            if (!childExecution.getId().equals(executionEntity.getId())) {
                executionEntityManager.cancelExecutionAndRelatedData(
                    childExecution,
                    DeleteReason.EVENT_SUBPROCESS_INTERRUPTING + "(" + startEvent.getId() + ")"
                );
            }
        }
    }

    protected Map<String, Object> processDataObjects(Collection<ValuedDataObject> dataObjects) {
        Map<String, Object> variablesMap = new HashMap<>();
        if (dataObjects != null) {
            for (ValuedDataObject dataObject : dataObjects) {
                variablesMap.put(dataObject.getName(), dataObject.getValue());
            }
        }
        return variablesMap;
    }
}
