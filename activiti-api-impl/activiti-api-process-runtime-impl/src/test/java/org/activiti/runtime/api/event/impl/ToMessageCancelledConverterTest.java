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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.MockitoAnnotations.initMocks;

import org.activiti.api.process.model.events.BPMNMessageCancelledEvent;
import org.activiti.api.runtime.model.impl.BPMNMessageImpl;
import org.activiti.engine.delegate.event.ActivitiEntityEvent;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

public class ToMessageCancelledConverterTest {

    @InjectMocks
    private ToMessageCancelledConverter toMessageConverter;

    @Mock
    private BPMNMessageConverter bpmnMessageConverter;

    @Before
    public void setUp() {
        initMocks(this);
    }

    @Test
    public void fromShouldReturnConvertedEventAndSetProcessInstanceIdAndProcessDefinitionId() {
        //given
        ActivitiEntityEvent internalEvent = mock(ActivitiEntityEvent.class);
  
        BPMNMessageImpl bpmnMessage = new BPMNMessageImpl("myMessage");
        bpmnMessage.setProcessDefinitionId("procDefId");
        bpmnMessage.setProcessInstanceId("procInstId");
        
        given(bpmnMessageConverter.convertToBPMNMessage(internalEvent)).willReturn(bpmnMessage);

        //when
        BPMNMessageCancelledEvent messageEvent = toMessageConverter.from(internalEvent).orElse(null);

        //then
        assertThat(messageEvent).isNotNull();
        assertThat(messageEvent.getProcessInstanceId()).isEqualTo("procInstId");
        assertThat(messageEvent.getProcessDefinitionId()).isEqualTo("procDefId");
        assertThat(messageEvent.getEntity()).isEqualTo(bpmnMessage);
    }

}
