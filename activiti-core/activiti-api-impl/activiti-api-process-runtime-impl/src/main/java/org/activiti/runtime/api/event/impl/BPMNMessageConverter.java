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

import java.util.Map;
import org.activiti.api.process.model.payloads.MessageEventPayload;
import org.activiti.api.runtime.model.impl.BPMNMessageImpl;
import org.activiti.engine.delegate.event.ActivitiMessageEvent;

public class BPMNMessageConverter {

    @SuppressWarnings("unchecked")
    public BPMNMessageImpl convertToBPMNMessage(
        ActivitiMessageEvent internalEvent
    ) {
        BPMNMessageImpl bpmnMessage = new BPMNMessageImpl(
            internalEvent.getActivityId()
        );
        bpmnMessage.setProcessDefinitionId(
            internalEvent.getProcessDefinitionId()
        );
        bpmnMessage.setProcessInstanceId(internalEvent.getProcessInstanceId());

        bpmnMessage.setMessagePayload(
            new MessageEventPayload(
                internalEvent.getMessageName(),
                internalEvent.getMessageCorrelationKey(),
                internalEvent.getMessageBusinessKey(),
                (Map<String, Object>) internalEvent.getMessageData()
            )
        );

        return bpmnMessage;
    }
}
