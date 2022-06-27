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
package org.activiti.runtime.api.event.impl;

import org.activiti.api.process.model.events.BPMNTimerFiredEvent;
import org.activiti.api.runtime.event.impl.BPMNTimerFiredEventImpl;
import org.activiti.engine.delegate.event.ActivitiEntityEvent;
import org.activiti.engine.delegate.event.ActivitiEvent;

import java.util.Optional;

public class ToTimerFiredConverter implements EventConverter<BPMNTimerFiredEvent, ActivitiEvent> {

    private BPMNTimerConverter bpmnTimerConverter;

    public ToTimerFiredConverter(BPMNTimerConverter bpmnTimerConverter) {
        this.bpmnTimerConverter = bpmnTimerConverter;
    }

    @Override
    public Optional<BPMNTimerFiredEvent> from(ActivitiEvent internalEvent) {
        BPMNTimerFiredEventImpl event = null;
        if (bpmnTimerConverter.isTimerRelatedEvent(internalEvent)) {
            event = new BPMNTimerFiredEventImpl(bpmnTimerConverter.convertToBPMNTimer((ActivitiEntityEvent) internalEvent));
            event.setProcessInstanceId(internalEvent.getProcessInstanceId());
            event.setProcessDefinitionId(internalEvent.getProcessDefinitionId());
        }
        return Optional.ofNullable(event);
    }
}
