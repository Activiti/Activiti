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

import org.activiti.api.process.model.events.BPMNSignalReceivedEvent;
import org.activiti.api.runtime.event.impl.BPMNSignalReceivedEventImpl;
import org.activiti.engine.delegate.event.ActivitiSignalEvent;
import org.activiti.runtime.api.model.impl.ToSignalConverter;

import java.util.Optional;

public class ToSignalReceivedConverter implements EventConverter<BPMNSignalReceivedEvent, ActivitiSignalEvent> {

    private ToSignalConverter toSignalConverter;

    public ToSignalReceivedConverter(ToSignalConverter toSignalConverter) {
        this.toSignalConverter = toSignalConverter;
    }

    @Override
    public Optional<BPMNSignalReceivedEvent> from(ActivitiSignalEvent internalEvent) {
        BPMNSignalReceivedEventImpl event = new BPMNSignalReceivedEventImpl(toSignalConverter.from(internalEvent));
        event.setProcessInstanceId(internalEvent.getProcessInstanceId());
        event.setProcessDefinitionId(internalEvent.getProcessDefinitionId());
        return Optional.of(event);
    }

}
