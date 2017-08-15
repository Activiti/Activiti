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

import java.util.Date;
import java.util.Optional;

import org.activiti.engine.ActivitiException;
import org.activiti.services.api.events.ProcessEngineEvent;
import org.activiti.services.query.model.Task;
import org.activiti.services.query.app.repository.TaskRepository;
import org.activiti.services.query.events.TaskCompletedEvent;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.activiti.services.query.events.handlers.TaskBuilder.aTask;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class TaskCompletedEventHandlerTest {

    @InjectMocks
    private TaskCompletedEventHandler handler;

    @Mock
    private TaskRepository taskRepository;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        initMocks(this);
    }

    @Test
    public void handleShouldUpdateTaskStatusToCompleted() throws Exception {
        //given
        String taskId = "30";
        Task eventTask = aTask().withId(taskId).build();

        given(taskRepository.findById(taskId)).willReturn(Optional.of(eventTask));

        //when
        handler.handle(new TaskCompletedEvent(System.currentTimeMillis(),
                                              "taskCompleted",
                                              "10",
                                              "100",
                                              "200",
                                              eventTask));

        //then
        verify(taskRepository).save(eventTask);
        verify(eventTask).setStatus("COMPLETED");
        verify(eventTask).setLastModified(any(Date.class));
    }

    @Test
    public void handleShouldThrowAnExceptionWhenNoTaskIsFoundForTheGivenId() throws Exception {
        //given
        String taskId = "30";
        Task eventTask = aTask().withId(taskId).build();

        given(taskRepository.findById(taskId)).willReturn(Optional.empty());

        //then
        expectedException.expect(ActivitiException.class);
        expectedException.expectMessage("Unable to find task with id: " + taskId);

        //when
        handler.handle(new TaskCompletedEvent(System.currentTimeMillis(),
                                              "taskCompleted",
                                              "10",
                                              "100",
                                              "200",
                                              eventTask));

    }

    @Test
    public void getHandledEventClass() throws Exception {
        //when
        Class<? extends ProcessEngineEvent> handledEventClass = handler.getHandledEventClass();

        //then
        assertThat(handledEventClass).isEqualTo(TaskCompletedEvent.class);
    }
}