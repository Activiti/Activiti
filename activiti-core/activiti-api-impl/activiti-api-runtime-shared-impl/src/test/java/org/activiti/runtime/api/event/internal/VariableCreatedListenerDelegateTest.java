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

package org.activiti.runtime.api.event.internal;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.Arrays;
import java.util.Optional;
import org.activiti.api.model.shared.event.VariableCreatedEvent;
import org.activiti.api.runtime.shared.events.VariableEventListener;
import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.impl.ActivitiVariableEventImpl;
import org.activiti.runtime.api.event.impl.ToVariableCreatedConverter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

public class VariableCreatedListenerDelegateTest {

    private VariableCreatedListenerDelegate variableCreatedListenerDelegate;

    @Mock
    private VariableEventListener<VariableCreatedEvent> firstListener;

    @Mock
    private VariableEventListener<VariableCreatedEvent> secondListener;

    @Mock
    private ToVariableCreatedConverter converter;

    @Mock
    private VariableEventFilter variableEventFilter;

    @BeforeEach
    public void setUp() {
        initMocks(this);

        variableCreatedListenerDelegate =
            new VariableCreatedListenerDelegate(
                Arrays.asList(firstListener, secondListener),
                converter,
                variableEventFilter
            );
    }

    @Test
    public void onEvent_should_callListenersWhenItsVariableEventAndItsNotFiltered() {
        //given
        ActivitiVariableEventImpl internalEvent = new ActivitiVariableEventImpl(
            ActivitiEventType.VARIABLE_CREATED
        );
        given(variableEventFilter.shouldEmmitEvent(internalEvent))
            .willReturn(true);
        VariableCreatedEvent apiEvent = mock(VariableCreatedEvent.class);
        given(converter.from(internalEvent)).willReturn(Optional.of(apiEvent));

        //when
        variableCreatedListenerDelegate.onEvent(internalEvent);

        //then
        verify(firstListener).onEvent(apiEvent);
        verify(secondListener).onEvent(apiEvent);
    }

    @Test
    public void onEvent_shouldNot_callListenersWhenItsNotAVariableEvent() {
        //given
        ActivitiEvent internalEvent = mock(ActivitiEvent.class);

        //when
        variableCreatedListenerDelegate.onEvent(internalEvent);

        //then
        verifyNoInteractions(firstListener);
        verifyNoInteractions(secondListener);
    }

    @Test
    public void onEvent_shouldNot_callListenersWhenItsFiltered() {
        //given
        ActivitiVariableEventImpl internalEvent = new ActivitiVariableEventImpl(
            ActivitiEventType.VARIABLE_CREATED
        );
        given(variableEventFilter.shouldEmmitEvent(internalEvent))
            .willReturn(false);
        VariableCreatedEvent apiEvent = mock(VariableCreatedEvent.class);
        given(converter.from(internalEvent)).willReturn(Optional.of(apiEvent));

        //when
        variableCreatedListenerDelegate.onEvent(internalEvent);

        //then
        verifyNoInteractions(firstListener);
        verifyNoInteractions(secondListener);
    }
}
