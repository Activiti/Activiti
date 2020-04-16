/*
 * Copyright 2019 Alfresco, Inc. and/or its affiliates.
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

package org.activiti.runtime.api.event.impl;

import static org.activiti.runtime.api.model.impl.MockTaskBuilder.taskEntityBuilder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.Optional;

import org.activiti.api.task.model.events.TaskRuntimeEvent;
import org.activiti.api.task.model.impl.TaskImpl;
import org.activiti.api.task.runtime.events.TaskCancelledEvent;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.impl.ActivitiEntityEventImpl;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.runtime.api.model.impl.APITaskConverter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

public class ToTaskCancelledConverterTest {

    @InjectMocks
    private ToTaskCancelledConverter eventConverter;

    @Mock
    private APITaskConverter taskConverter;

    @BeforeEach
    public void setUp() {
        initMocks(this);
    }

    @Test
    public void should_returnConvertedTask_when_entityIsACancelledTask() {
        //given
        Task internalTask = taskEntityBuilder()
                .withCancelled(true)
                .build();
        ActivitiEntityEventImpl internalEvent = new ActivitiEntityEventImpl(internalTask,
                                                                            ActivitiEventType.ENTITY_DELETED);

        TaskImpl apiTask = new TaskImpl("id",
                                        "myTask",
                                        org.activiti.api.task.model.Task.TaskStatus.CANCELLED);
        given(taskConverter.from(internalTask, org.activiti.api.task.model.Task.TaskStatus.CANCELLED)).willReturn(apiTask);

        //when
        TaskCancelledEvent convertedTaskCancelledEvent = eventConverter.from(internalEvent).orElse(null);

        //then
        assertThat(convertedTaskCancelledEvent).isNotNull();
        assertThat(convertedTaskCancelledEvent.getEntity()).isEqualTo(apiTask);
        assertThat(convertedTaskCancelledEvent.getEventType()).isEqualTo(TaskRuntimeEvent.TaskEvents.TASK_CANCELLED);

    }

    @Test
    public void should_ReturnEmpty_when_entityIsANonCancelledTask() {
        //given
        Task internalTask = taskEntityBuilder()
                .withCancelled(false)
                .build();
        ActivitiEntityEventImpl internalEvent = new ActivitiEntityEventImpl(internalTask,
                                                                            ActivitiEventType.ENTITY_DELETED);

        //when
        Optional<TaskCancelledEvent> convertedTaskCancelledEvent = eventConverter.from(internalEvent);

        //then
        assertThat(convertedTaskCancelledEvent).isEmpty();

    }

    @Test
    public void should_returnEmpty_when_entityIsNotTask() {
        //given
        ActivitiEntityEventImpl internalEvent = new ActivitiEntityEventImpl(mock(ProcessInstance.class),
                                                                            ActivitiEventType.ENTITY_DELETED);

        //when
        Optional<TaskCancelledEvent> convertedTaskCancelledEvent = eventConverter.from(internalEvent);

        //then
        assertThat(convertedTaskCancelledEvent).isEmpty();

    }
}
