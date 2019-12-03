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

package org.activiti.runtime.api.model.impl;

import org.activiti.api.process.model.BPMNSignal;
import org.activiti.api.process.model.builders.ProcessPayloadBuilder;
import org.activiti.api.process.model.payloads.SignalPayload;
import org.activiti.api.runtime.model.impl.BPMNSignalImpl;
import org.activiti.engine.delegate.event.ActivitiSignalEvent;

import java.util.HashMap;
import java.util.Map;

public class ToSignalConverter {

    @SuppressWarnings("unchecked")
    public BPMNSignal from(ActivitiSignalEvent internalEvent) {
        BPMNSignalImpl signal = new BPMNSignalImpl(internalEvent.getActivityId());

        signal.setProcessDefinitionId(internalEvent.getProcessDefinitionId());
        signal.setProcessInstanceId(internalEvent.getProcessInstanceId());

        SignalPayload signalPayload = ProcessPayloadBuilder.signal()
                .withName(internalEvent.getSignalName())
                .build();

        if (internalEvent.getSignalData() != null) {
            Map<String, Object> sourceVariables = (Map<String, Object>) internalEvent.getSignalData();
            signalPayload.setVariables(new HashMap<>(sourceVariables));
        }
        signal.setSignalPayload(signalPayload);

        return signal;
    }
}
