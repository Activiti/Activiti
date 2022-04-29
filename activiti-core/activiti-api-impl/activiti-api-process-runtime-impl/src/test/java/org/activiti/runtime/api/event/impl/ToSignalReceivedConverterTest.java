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

import org.activiti.api.process.model.events.BPMNSignalReceivedEvent;
import org.activiti.api.runtime.model.impl.BPMNSignalImpl;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.impl.ActivitiSignalEventImpl;
import org.activiti.runtime.api.model.impl.ToSignalConverter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class ToSignalReceivedConverterTest {

    @InjectMocks
    private ToSignalReceivedConverter toSignalReceivedConverter;

    @Mock
    private ToSignalConverter toSignalConverter;

    @Test
    public void fromShouldReturnConvertedEventAndSetProcessInstanceIdAndProcessDefinitionId() {
        //given
        ActivitiSignalEventImpl internalEvent = new ActivitiSignalEventImpl(ActivitiEventType.ACTIVITY_SIGNALED);
        internalEvent.setProcessDefinitionId("procDefId");
        internalEvent.setProcessInstanceId("procInstId");

        BPMNSignalImpl bpmnSignal = new BPMNSignalImpl();
        given(toSignalConverter.from(internalEvent)).willReturn(bpmnSignal);


        //when
        BPMNSignalReceivedEvent bpmnSignalReceivedEvent = toSignalReceivedConverter.from(internalEvent).orElse(null);

        //then
        assertThat(bpmnSignalReceivedEvent).isNotNull();
        assertThat(bpmnSignalReceivedEvent.getEntity()).isEqualTo(bpmnSignal);
        assertThat(bpmnSignalReceivedEvent.getProcessDefinitionId()).isEqualTo("procDefId");
        assertThat(bpmnSignalReceivedEvent.getProcessInstanceId()).isEqualTo("procInstId");

    }
}
