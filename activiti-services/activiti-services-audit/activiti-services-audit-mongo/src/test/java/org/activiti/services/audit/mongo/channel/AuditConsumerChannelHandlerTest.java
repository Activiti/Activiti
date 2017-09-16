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

package org.activiti.services.audit.mongo.channel;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.Arrays;

import org.activiti.services.audit.mongo.events.ProcessEngineEventDocument;
import org.activiti.services.audit.mongo.repository.EventsRepository;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

public class AuditConsumerChannelHandlerTest {

    @InjectMocks
    private AuditConsumerChannelHandler handler;

    @Mock
    private EventsRepository eventsRepository;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
    }

    @Test
    public void receiveShouldStoreAllReceivedEvents() throws Exception {
        //given
        ProcessEngineEventDocument firstEvent = mock(ProcessEngineEventDocument.class);
        ProcessEngineEventDocument secondEvent = mock(ProcessEngineEventDocument.class);
        ProcessEngineEventDocument[] events = {firstEvent, secondEvent};

        //when
        handler.receive(events);

        //then
        verify(eventsRepository).saveAll(Arrays.asList(events));
    }
}