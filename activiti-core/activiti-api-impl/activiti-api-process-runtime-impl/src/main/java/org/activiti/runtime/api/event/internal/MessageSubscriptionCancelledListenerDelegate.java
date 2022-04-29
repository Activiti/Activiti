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

import java.util.List;
import java.util.Optional;

import org.activiti.api.process.model.events.MessageSubscriptionCancelledEvent;
import org.activiti.api.process.runtime.events.listener.ProcessRuntimeEventListener;
import org.activiti.engine.delegate.event.ActivitiEntityEvent;
import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.ActivitiEventListener;
import org.activiti.engine.impl.persistence.entity.MessageEventSubscriptionEntity;
import org.activiti.runtime.api.event.impl.ToMessageSubscriptionCancelledConverter;

public class MessageSubscriptionCancelledListenerDelegate implements ActivitiEventListener {

    private List<ProcessRuntimeEventListener<MessageSubscriptionCancelledEvent>> processRuntimeEventListeners;

    private ToMessageSubscriptionCancelledConverter converter;

    public MessageSubscriptionCancelledListenerDelegate(List<ProcessRuntimeEventListener<MessageSubscriptionCancelledEvent>> processRuntimeEventListeners,
                                                        ToMessageSubscriptionCancelledConverter converter) {
        this.processRuntimeEventListeners = processRuntimeEventListeners;
        this.converter = converter;
    }

    @Override
    public void onEvent(ActivitiEvent event) {
        if (isValidEvent(event)) {
            converter.from((ActivitiEntityEvent) event)
                    .ifPresent(convertedEvent -> {
                        processRuntimeEventListeners.forEach(listener -> listener.onEvent(convertedEvent));
                    });
        }
    }

    @Override
    public boolean isFailOnException() {
        return false;
    }

    protected boolean isValidEvent(ActivitiEvent event) {
        return Optional.ofNullable(event)
                       .filter(ActivitiEntityEvent.class::isInstance)
                       .map(e -> ((ActivitiEntityEvent) event).getEntity() instanceof MessageEventSubscriptionEntity)
                       .orElse(false);
    }
}
