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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import org.activiti.api.process.model.events.BPMNErrorReceivedEvent;
import org.activiti.api.runtime.model.impl.BPMNErrorImpl;
import org.activiti.engine.delegate.event.ActivitiErrorEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ToErrorReceivedConverterTest {

    @InjectMocks
    private ToErrorReceivedConverter toErrorReceivedConverter;

    @Mock
    private BPMNErrorConverter bpmnErrorConverter;

    @Test
    public void fromShouldReturnConvertedEventAndSetProcessInstanceIdAndProcessDefinitionId() {
        //given
        ActivitiErrorEvent internalEvent = mock(ActivitiErrorEvent.class);
        given(internalEvent.getProcessDefinitionId()).willReturn("procDefId");
        given(internalEvent.getProcessInstanceId()).willReturn("procInstId");

        BPMNErrorImpl bpmnError = new BPMNErrorImpl("myError");

        given(bpmnErrorConverter.convertToBPMNError(internalEvent)).willReturn(bpmnError);

        //when
        BPMNErrorReceivedEvent errorEvent = toErrorReceivedConverter.from(internalEvent).orElse(null);

        //then
        assertThat(errorEvent).isNotNull();
        assertThat(errorEvent.getProcessInstanceId()).isEqualTo("procInstId");
        assertThat(errorEvent.getProcessDefinitionId()).isEqualTo("procDefId");
        assertThat(errorEvent.getEntity()).isEqualTo(bpmnError);
    }

}
