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

import org.activiti.api.process.model.events.BPMNTimerScheduledEvent;
import org.activiti.api.process.runtime.events.listener.BPMNElementEventListener;
import org.activiti.api.runtime.event.impl.BPMNTimerScheduledEventImpl;
import org.activiti.engine.delegate.event.ActivitiEntityEvent;
import org.activiti.runtime.api.event.impl.ToTimerScheduledConverter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class TimerScheduledListenerDelegateTest {

    private TimerScheduledListenerDelegate listenerDelegate;

    @Mock
    private BPMNElementEventListener<BPMNTimerScheduledEvent> listener;

    @Mock
    private ToTimerScheduledConverter converter;

    @BeforeEach
    public void setUp() {
        listenerDelegate = new TimerScheduledListenerDelegate(singletonList(listener), converter);
    }

    @Test
    public void shouldCallRegisteredListenersWhenConvertedEventIsNotEmpty() {
        //given
        ActivitiEntityEvent internalEvent = mock(ActivitiEntityEvent.class);
        BPMNTimerScheduledEventImpl convertedEvent = new BPMNTimerScheduledEventImpl();
        given(converter.from(internalEvent)).willReturn(Optional.of(convertedEvent));

        //when
        listenerDelegate.onEvent(internalEvent);

        //then
        verify(listener).onEvent(convertedEvent);
    }

    @Test
    public void shouldDoNothingWhenConvertedEventIsEmpty() {
        //given
        ActivitiEntityEvent internalEvent = mock(ActivitiEntityEvent.class);
        given(converter.from(internalEvent)).willReturn(Optional.empty());

        //when
        listenerDelegate.onEvent(internalEvent);

        //then
        verify(listener, never()).onEvent(any());
    }
}
