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

package org.activiti.runtime.api.event.internal;

import org.activiti.api.model.shared.event.VariableCreatedEvent;
import org.activiti.api.runtime.shared.events.VariableEventListener;
import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.ActivitiEventListener;
import org.activiti.engine.delegate.event.ActivitiVariableEvent;
import org.activiti.runtime.api.event.impl.ToVariableCreatedConverter;

import java.util.List;

public class VariableCreatedListenerDelegate implements ActivitiEventListener {

    private final List<VariableEventListener<VariableCreatedEvent>> listeners;

    private final ToVariableCreatedConverter converter;

    private final VariableEventFilter variableEventFilter;

    public VariableCreatedListenerDelegate(
        List<VariableEventListener<VariableCreatedEvent>> listeners,
        ToVariableCreatedConverter converter,
        VariableEventFilter variableEventFilter) {
        this.listeners = listeners;
        this.converter = converter;
        this.variableEventFilter = variableEventFilter;
    }

    @Override
    public void onEvent(ActivitiEvent event) {
        if (event instanceof ActivitiVariableEvent) {
            ActivitiVariableEvent internalEvent = (ActivitiVariableEvent) event;
            if (variableEventFilter.shouldEmmitEvent(internalEvent)) {
                converter.from(internalEvent)
                    .ifPresent(convertedEvent -> {
                        if (listeners != null) {
                            for (VariableEventListener<VariableCreatedEvent> listener : listeners) {
                                listener.onEvent(convertedEvent);
                            }
                        }
                    });
            }
        }
    }

    @Override
    public boolean isFailOnException() {
        return false;
    }
}
