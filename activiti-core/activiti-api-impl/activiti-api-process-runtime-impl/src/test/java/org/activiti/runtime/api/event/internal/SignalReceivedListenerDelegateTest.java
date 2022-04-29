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
package org.activiti.runtime.api.event.internal;

import org.activiti.api.process.model.events.BPMNSignalReceivedEvent;
import org.activiti.api.process.runtime.events.listener.BPMNElementEventListener;
import org.activiti.api.runtime.event.impl.BPMNSignalReceivedEventImpl;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.impl.ActivitiSignalEventImpl;
import org.activiti.runtime.api.event.impl.ToSignalReceivedConverter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class SignalReceivedListenerDelegateTest {

    private SignalReceivedListenerDelegate listenerDelegate;

    @Mock
    private BPMNElementEventListener<BPMNSignalReceivedEvent> firstListener;

    @Mock
    private BPMNElementEventListener<BPMNSignalReceivedEvent> secondListener;

    @Mock
    private ToSignalReceivedConverter converter;

    @BeforeEach
    public void setUp() {
        listenerDelegate = new SignalReceivedListenerDelegate(asList(firstListener, secondListener), converter);
    }

    @Test
    public void onEventShouldCallOnAvailableListenersWhenIsASignalEvent() {
        //given
        ActivitiSignalEventImpl internalEvent = new ActivitiSignalEventImpl(ActivitiEventType.ACTIVITY_SIGNALED);
        BPMNSignalReceivedEventImpl signalReceivedEvent = new BPMNSignalReceivedEventImpl();
        given(converter.from(internalEvent)).willReturn(Optional.of(signalReceivedEvent));

        //when
        listenerDelegate.onEvent(internalEvent);

        //then
        verify(firstListener).onEvent(signalReceivedEvent);
        verify(secondListener).onEvent(signalReceivedEvent);
    }

    @Test
    public void failOnExceptionShouldReturnFalse() {
        assertThat(listenerDelegate.isFailOnException()).isFalse();
    }
}
