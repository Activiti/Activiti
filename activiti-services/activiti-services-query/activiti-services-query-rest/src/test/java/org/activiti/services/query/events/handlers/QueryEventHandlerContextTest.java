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

package org.activiti.services.query.events.handlers;

import java.util.Collections;

import org.activiti.services.query.events.TaskCompletedEvent;
import org.activiti.services.query.events.TaskCreatedEvent;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class QueryEventHandlerContextTest {

    private QueryEventHandlerContext context;

    @Mock
    private QueryEventHandler handler;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        doReturn(TaskCreatedEvent.class).when(handler).getHandledEventClass();
        context = new QueryEventHandlerContext(Collections.singleton(handler));
    }

    @Test
    public void handleShouldSelectHandlerBasedOnEventType() throws Exception {
        //given
        TaskCreatedEvent event = new TaskCreatedEvent();

        //when
        context.handle(event);

        //then
        verify(handler).handle(event);
    }

    @Test
    public void handleShouldDoNothingWhenNoHandlerIsFoundForTheGivenEvent() throws Exception {
        //given
        TaskCompletedEvent event = new TaskCompletedEvent();

        //when
        context.handle(event);

        //then
        verify(handler, never()).handle(any());
    }
}