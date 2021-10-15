/*
 * Copyright 2010-2020 Alfresco Software, Ltd.
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
import org.activiti.bpmn.model.BoundaryEvent;
import org.activiti.bpmn.model.MessageEventDefinition;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.event.impl.ActivitiEventBuilder;
import org.activiti.engine.impl.bpmn.parser.factory.MessageExecutionContext;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.EventSubscriptionEntity;
import org.activiti.engine.impl.persistence.entity.EventSubscriptionEntityManager;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.MessageEventSubscriptionEntity;

/**

 */
public class BoundaryMessageEventActivityBehavior
    extends BoundaryEventActivityBehavior {

    private static final long serialVersionUID = 1L;

    private final MessageEventDefinition messageEventDefinition;
    private final MessageExecutionContext messageExecutionContext;

    public BoundaryMessageEventActivityBehavior(
        MessageEventDefinition messageEventDefinition,
        boolean interrupting,
        MessageExecutionContext messageExecutionContext
    ) {
        super(interrupting);
        this.messageEventDefinition = messageEventDefinition;
        this.messageExecutionContext = messageExecutionContext;
    }

    @Override
    public void execute(DelegateExecution execution) {
        CommandContext commandContext = Context.getCommandContext();
        ExecutionEntity executionEntity = (ExecutionEntity) execution;

        MessageEventSubscriptionEntity messageEvent = messageExecutionContext.createMessageEventSubscription(
            commandContext,
            executionEntity
        );
        if (
            commandContext
                .getProcessEngineConfiguration()
                .getEventDispatcher()
                .isEnabled()
        ) {
            commandContext
                .getProcessEngineConfiguration()
                .getEventDispatcher()
                .dispatchEvent(
                    ActivitiEventBuilder.createMessageWaitingEvent(
                        executionEntity,
                        messageEvent.getEventName(),
                        messageEvent.getConfiguration()
                    )
                );
        }
    }

    @Override
    public void trigger(
        DelegateExecution execution,
        String triggerName,
        Object triggerData
    ) {
        ExecutionEntity executionEntity = (ExecutionEntity) execution;
        BoundaryEvent boundaryEvent = (BoundaryEvent) execution.getCurrentFlowElement();
        // Should we use triggerName and triggerData, because message name expression can change?
        String messageName = messageExecutionContext.getMessageName(execution);

        if (boundaryEvent.isCancelActivity()) {
            EventSubscriptionEntityManager eventSubscriptionEntityManager = Context
                .getCommandContext()
                .getEventSubscriptionEntityManager();
            List<EventSubscriptionEntity> eventSubscriptions = executionEntity.getEventSubscriptions();
            for (EventSubscriptionEntity eventSubscription : eventSubscriptions) {
                if (
                    eventSubscription instanceof MessageEventSubscriptionEntity &&
                    eventSubscription.getEventName().equals(messageName)
                ) {
                    eventSubscriptionEntityManager.delete(eventSubscription);
                }
            }
        }

        super.trigger(executionEntity, triggerName, triggerData);
    }

    public MessageEventDefinition getMessageEventDefinition() {
        return messageEventDefinition;
    }

    public MessageExecutionContext getMessageExecutionContext() {
        return messageExecutionContext;
    }
}
