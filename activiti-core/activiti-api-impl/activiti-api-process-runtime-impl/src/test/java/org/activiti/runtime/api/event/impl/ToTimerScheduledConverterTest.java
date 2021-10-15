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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.Optional;
import org.activiti.api.process.model.events.BPMNTimerScheduledEvent;
import org.activiti.api.runtime.model.impl.BPMNTimerImpl;
import org.activiti.engine.delegate.event.ActivitiEntityEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

public class ToTimerScheduledConverterTest {

    @InjectMocks
    private ToTimerScheduledConverter toTimerConverter;

    @Mock
    private BPMNTimerConverter bpmnTimerConverter;

    @BeforeEach
    public void setUp() {
        initMocks(this);
    }

    @Test
    public void shouldReturnConvertedEventsWhenInternalEvenIsRelatedToTimers() {
        //given
        ActivitiEntityEvent internalEvent = mock(ActivitiEntityEvent.class);
        given(internalEvent.getProcessDefinitionId()).willReturn("procDefId");
        given(internalEvent.getProcessInstanceId()).willReturn("procInstId");

        BPMNTimerImpl bpmnTimer = new BPMNTimerImpl("myTimer");
        given(bpmnTimerConverter.convertToBPMNTimer(internalEvent))
            .willReturn(bpmnTimer);
        given(bpmnTimerConverter.isTimerRelatedEvent(internalEvent))
            .willReturn(true);

        //when
        BPMNTimerScheduledEvent timerEvent = toTimerConverter
            .from(internalEvent)
            .orElse(null);

        //then
        assertThat(timerEvent).isNotNull();
        assertThat(timerEvent.getProcessInstanceId()).isEqualTo("procInstId");
        assertThat(timerEvent.getProcessDefinitionId()).isEqualTo("procDefId");
        assertThat(timerEvent.getEntity()).isEqualTo(bpmnTimer);
    }

    @Test
    public void shouldReturnEmptyOptionalWhenInternalEventIsNotRelatedToTimers() {
        //given
        given(
            bpmnTimerConverter.isTimerRelatedEvent(
                mock(ActivitiEntityEvent.class)
            )
        )
            .willReturn(false);

        //when
        Optional<BPMNTimerScheduledEvent> optional = toTimerConverter.from(
            mock(ActivitiEntityEvent.class)
        );

        //then
        assertThat(optional).isEmpty();
    }
}
