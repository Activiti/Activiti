/*
 * Copyright 2019 Alfresco, Inc. and/or its affiliates.
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

import org.activiti.api.process.model.payloads.MessageEventPayload;
import org.activiti.api.runtime.model.impl.BPMNMessageImpl;
import org.activiti.engine.delegate.event.ActivitiMessageEvent;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

public class BPMNMessageConverterTest {

    private BPMNMessageConverter bpmnMessageConverter = new BPMNMessageConverter();

    @Test
    public void convertShouldReturnBPMNMessage() {

        ActivitiMessageEvent internalEvent = mock(ActivitiMessageEvent.class);
        given(internalEvent.getMessageBusinessKey()).willReturn("businessKey");
        given(internalEvent.getMessageCorrelationKey()).willReturn("correlationKey");
        given(internalEvent.getMessageName()).willReturn("messageName");
        given(internalEvent.getProcessDefinitionId()).willReturn("procDefId");
        given(internalEvent.getProcessInstanceId()).willReturn("procInstId");

        BPMNMessageImpl bpmnMessage = bpmnMessageConverter.convertToBPMNMessage(internalEvent);

        //then
        assertThat(bpmnMessage).isNotNull();
        assertThat(bpmnMessage.getProcessInstanceId()).isEqualTo("procInstId");
        assertThat(bpmnMessage.getProcessDefinitionId()).isEqualTo("procDefId");
        assertThat(bpmnMessage.getMessagePayload())
                .isNotNull()
                .extracting(MessageEventPayload::getName,
                            MessageEventPayload::getBusinessKey,
                            MessageEventPayload::getCorrelationKey)
                .contains("messageName",
                          "businessKey",
                          "correlationKey");
    }

}
