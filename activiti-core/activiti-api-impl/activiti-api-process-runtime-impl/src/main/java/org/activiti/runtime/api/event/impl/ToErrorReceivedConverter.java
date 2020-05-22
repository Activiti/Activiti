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

import java.util.Optional;

import org.activiti.api.process.model.events.BPMNErrorReceivedEvent;
import org.activiti.api.runtime.event.impl.BPMNErrorReceivedEventImpl;
import org.activiti.engine.delegate.event.ActivitiErrorEvent;

public class ToErrorReceivedConverter implements EventConverter<BPMNErrorReceivedEvent, ActivitiErrorEvent> {

    private BPMNErrorConverter bpmnErrorConverter;

    public ToErrorReceivedConverter(BPMNErrorConverter bpmnErrorConverter) {
        this.bpmnErrorConverter = bpmnErrorConverter;
    }

    @Override
    public Optional<BPMNErrorReceivedEvent> from(ActivitiErrorEvent internalEvent) {
        BPMNErrorReceivedEventImpl event = new BPMNErrorReceivedEventImpl(bpmnErrorConverter.convertToBPMNError(internalEvent));
        event.setProcessInstanceId(internalEvent.getProcessInstanceId());
        event.setProcessDefinitionId(internalEvent.getProcessDefinitionId());
        return Optional.of(event);
    }
}
