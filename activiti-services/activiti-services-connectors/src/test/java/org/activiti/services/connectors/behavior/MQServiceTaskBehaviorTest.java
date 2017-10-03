/*
 * Copyright 2017 Alfresco, Inc. and/or its affiliates.
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

package org.activiti.services.connectors.behavior;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.services.connectors.channel.ServiceTaskChannels;
import org.activiti.services.connectors.model.ServiceTaskEvent;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class MQServiceTaskBehaviorTest {

    @Spy
    @InjectMocks
    private MQServiceTaskBehavior behavior;

    @Mock
    private ServiceTaskChannels channels;

    @Mock
    private MessageChannel messageChannel;

    @Captor
    private ArgumentCaptor<Message<ServiceTaskEvent>> captor;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        when(channels.serviceTasksProducer()).thenReturn(messageChannel);
    }

    @Test
    public void executeShouldSendAMessage() throws Exception {
        //given
        DelegateExecution execution = mock(DelegateExecution.class);
        given(execution.getId()).willReturn("execId");
        given(execution.getProcessInstanceId()).willReturn("procInstId");
        given(execution.getCurrentActivityId()).willReturn("actId");

        //when
        behavior.execute(execution);

        //then
        verify(messageChannel).send(captor.capture());
        Message<ServiceTaskEvent> message = captor.getValue();
        assertThat(message.getPayload().getContext().getExecutionId()).isEqualTo("execId");
        assertThat(message.getPayload().getContext().getProcessInstanceId()).isEqualTo("procInstId");
        assertThat(message.getPayload().getContext().getTaskId()).isEqualTo("actId");
    }

    @Test
    public void triggerShouldCallLeave() throws Exception {
        //given
        DelegateExecution execution = mock(DelegateExecution.class);
        doNothing().when(behavior).leave(execution);

        //when
        behavior.trigger(execution, null, null);

        //then
        verify(behavior).leave(execution);
    }
}