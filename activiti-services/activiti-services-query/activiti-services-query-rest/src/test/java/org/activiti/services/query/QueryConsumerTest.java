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

package org.activiti.services.query;

import org.activiti.services.query.app.QueryConsumer;
import org.activiti.services.query.events.AbstractProcessEngineEvent;
import org.activiti.services.query.events.handlers.QueryEventHandlerContext;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

public class QueryConsumerTest {

    @InjectMocks
    private QueryConsumer consumer;

    @Mock
    private QueryEventHandlerContext eventHandlerContext;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
    }

    @Test
    public void receiveShouldHandleReceivedEvent() throws Exception {
        //given
        AbstractProcessEngineEvent event = mock(AbstractProcessEngineEvent.class);

        //when
        consumer.receive(event);

        //then
        verify(eventHandlerContext).handle(event);
    }
}