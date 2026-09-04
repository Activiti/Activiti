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

import org.activiti.bpmn.model.MessageEventDefinition;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.impl.bpmn.parser.factory.MessageExecutionContext;
import org.activiti.engine.impl.persistence.entity.EventSubscriptionEntity;
import org.activiti.engine.impl.persistence.entity.MessageEventSubscriptionEntity;

/**
 * Implementation of the BPMN 2.0 event sub-process message start event.
 */
public class EventSubProcessMessageStartEventActivityBehavior
    extends AbstractEventSubProcessStartEventActivityBehavior {

    private static final long serialVersionUID = 1L;

    protected final MessageEventDefinition messageEventDefinition;
    protected final MessageExecutionContext messageExecutionContext;

    public EventSubProcessMessageStartEventActivityBehavior(
        MessageEventDefinition messageEventDefinition,
        MessageExecutionContext messageExecutionContext
    ) {
        this.messageEventDefinition = messageEventDefinition;
        this.messageExecutionContext = messageExecutionContext;
    }

    @Override
    protected String resolveEventName(DelegateExecution execution) {
        // Should we use triggerName and triggerData, because message name expression can change?
        return messageExecutionContext.getMessageName(execution);
    }

    @Override
    protected boolean matchesSubscription(EventSubscriptionEntity eventSubscription, String eventName) {
        return (
            eventSubscription instanceof MessageEventSubscriptionEntity &&
            eventSubscription.getEventName().equals(eventName)
        );
    }
}
