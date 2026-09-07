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

import org.activiti.bpmn.model.Signal;
import org.activiti.bpmn.model.SignalEventDefinition;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.impl.event.EventDefinitionExpressionUtil;
import org.activiti.engine.impl.persistence.entity.EventSubscriptionEntity;
import org.activiti.engine.impl.persistence.entity.SignalEventSubscriptionEntity;
import org.activiti.engine.impl.util.ProcessDefinitionUtil;

/**
 * Implementation of the BPMN 2.0 event sub-process signal start event.
 * Mirrors {@link EventSubProcessMessageStartEventActivityBehavior} but for {@link SignalEventDefinition}.
 */
public class EventSubProcessSignalStartEventActivityBehavior extends AbstractEventSubProcessStartEventActivityBehavior {

    private static final long serialVersionUID = 1L;

    protected final transient SignalEventDefinition signalEventDefinition;
    protected final transient Signal signal;

    public EventSubProcessSignalStartEventActivityBehavior(SignalEventDefinition signalEventDefinition, Signal signal) {
        this.signalEventDefinition = signalEventDefinition;
        this.signal = signal;
    }

    @Override
    protected String resolveEventName(DelegateExecution execution) {
        return EventDefinitionExpressionUtil.determineSignalName(
            signalEventDefinition,
            ProcessDefinitionUtil.getBpmnModel(execution.getProcessDefinitionId()),
            execution
        );
    }

    @Override
    protected boolean matchesSubscription(EventSubscriptionEntity eventSubscription, String eventName) {
        return (
            eventSubscription instanceof SignalEventSubscriptionEntity &&
            eventSubscription.getEventName().equals(eventName)
        );
    }
}
