/*
 * Copyright 2018 Alfresco, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.activiti.runtime.api.event.impl;

import java.util.Optional;

import org.activiti.api.process.model.events.BPMNMessageReceivedEvent;
import org.activiti.api.runtime.event.impl.BPMNMessageReceivedEventImpl;
import org.activiti.engine.delegate.event.ActivitiMessageEvent;

public class ToMessageReceivedConverter implements EventConverter<BPMNMessageReceivedEvent, ActivitiMessageEvent> {

    private BPMNMessageConverter bpmnMessageConverter;

    public ToMessageReceivedConverter(BPMNMessageConverter bpmnMessageConverter) {
        this.bpmnMessageConverter = bpmnMessageConverter;
    }

    @Override
    public Optional<BPMNMessageReceivedEvent> from(ActivitiMessageEvent internalEvent) {
        BPMNMessageReceivedEventImpl event = new BPMNMessageReceivedEventImpl(bpmnMessageConverter.convertToBPMNMessage(internalEvent));
        event.setProcessInstanceId(internalEvent.getProcessInstanceId());
        event.setProcessDefinitionId(internalEvent.getProcessDefinitionId());
        return Optional.of(event);
    }
}
