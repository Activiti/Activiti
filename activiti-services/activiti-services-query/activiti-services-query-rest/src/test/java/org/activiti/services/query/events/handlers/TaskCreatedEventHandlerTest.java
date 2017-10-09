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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.Date;

import javax.persistence.EntityManager;

import org.activiti.services.api.events.ProcessEngineEvent;
import org.activiti.services.query.app.repository.TaskRepository;
import org.activiti.services.query.events.TaskCreatedEvent;
import org.activiti.services.query.model.ProcessInstance;
import org.activiti.services.query.model.Task;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;

public class TaskCreatedEventHandlerTest {

    @InjectMocks
    private TaskCreatedEventHandler handler;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private EntityManager entityManager;
    
    @Before
    public void setUp() throws Exception {
        initMocks(this);
    }

    @Test
    public void handleShouldStoreNewTaskInstance() throws Exception {
        //given
        Task eventTask = mock(Task.class);
        TaskCreatedEvent taskCreated = new TaskCreatedEvent(System.currentTimeMillis(),
                                                            "taskCreated",
                                                            "10",
                                                            "100",
                                                            "200",
                                                            eventTask);

        when(entityManager.getReference(ArgumentMatchers.eq(ProcessInstance.class), any()))
        	.thenReturn(mock(ProcessInstance.class));
        
        //when
        handler.handle(taskCreated);

        //then
        verify(taskRepository).save(eventTask);
        verify(eventTask).setStatus("CREATED");
        verify(eventTask).setLastModified(any(Date.class));
        verify(eventTask).setProcessInstance(any(ProcessInstance.class));
    }

    @Test
    public void getHandledEventClassShouldReturnTaskCreatedEventClass() throws Exception {
        //when
        Class<? extends ProcessEngineEvent> handledEventClass = handler.getHandledEventClass();

        //then
        assertThat(handledEventClass).isEqualTo(TaskCreatedEvent.class);
    }
}