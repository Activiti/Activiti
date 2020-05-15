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
package org.activiti.runtime.api.event.impl;

import org.activiti.api.process.model.events.BPMNTimerFailedEvent;
import org.activiti.api.runtime.event.impl.BPMNTimerFailedEventImpl;
import org.activiti.engine.delegate.event.ActivitiEntityEvent;
import org.activiti.engine.delegate.event.ActivitiEvent;

import java.util.Optional;

public class ToTimerFailedConverter implements EventConverter<BPMNTimerFailedEvent, ActivitiEvent> {

    private BPMNTimerConverter bpmnTimerConverter;

    public ToTimerFailedConverter(BPMNTimerConverter bpmnTimerConverter) {
        this.bpmnTimerConverter = bpmnTimerConverter;
    }

    @Override
    public Optional<BPMNTimerFailedEvent> from(ActivitiEvent internalEvent) {
        BPMNTimerFailedEventImpl event = null;
        if (bpmnTimerConverter.isTimerRelatedEvent(internalEvent)) {
            event = new BPMNTimerFailedEventImpl(bpmnTimerConverter.convertToBPMNTimer((ActivitiEntityEvent) internalEvent));
            event.setProcessInstanceId(internalEvent.getProcessInstanceId());
            event.setProcessDefinitionId(internalEvent.getProcessDefinitionId());
        }
        return Optional.ofNullable(event);
    }
}
