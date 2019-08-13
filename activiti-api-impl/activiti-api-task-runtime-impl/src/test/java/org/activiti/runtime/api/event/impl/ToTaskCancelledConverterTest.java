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

import org.activiti.api.task.model.events.TaskRuntimeEvent;
import org.activiti.api.task.model.impl.TaskImpl;
import org.activiti.api.task.runtime.events.TaskCancelledEvent;
import org.activiti.engine.TaskService;
import org.activiti.engine.delegate.event.impl.ActivitiActivityCancelledEventImpl;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.activiti.runtime.api.model.impl.APITaskConverter;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

public class ToTaskCancelledConverterTest {

    @InjectMocks
    private ToTaskCancelledConverter eventConverter;

    @Mock
    private APITaskConverter taskConverter;

    @Mock
    private TaskService taskService;

    @Before
    public void setUp() {
        initMocks(this);
    }

    @Test
    public void fromShouldFilterOnProcessInstanceAndTaskDefinitionKeyWhenTheyAreSet() {
        //given
        String procInstId = "procInstId";
        String activityId = "activityId";
        ActivitiActivityCancelledEventImpl internalEvent = new ActivitiActivityCancelledEventImpl();
        internalEvent.setProcessInstanceId(procInstId);
        internalEvent.setActivityId(activityId);

        TaskQuery taskQuery = mock(TaskQuery.class,
                              Answers.RETURNS_SELF);
        given(taskService.createTaskQuery()).willReturn(taskQuery);

        Task internalTask = mock(Task.class);
        given(taskQuery.list()).willReturn(Collections.singletonList(internalTask));

        TaskImpl apiTask = new TaskImpl("id",
                                  "myTask",
                                  org.activiti.api.task.model.Task.TaskStatus.CREATED);
        given(taskConverter.from(internalTask)).willReturn(apiTask);

        //when
        TaskCancelledEvent convertedTaskCancelledEvent = eventConverter.from(internalEvent).orElse(null);

        //then
        assertThat(convertedTaskCancelledEvent).isNotNull();
        assertThat(convertedTaskCancelledEvent.getEntity()).isEqualTo(apiTask);
        assertThat(convertedTaskCancelledEvent.getEventType()).isEqualTo(TaskRuntimeEvent.TaskEvents.TASK_CANCELLED);

        verify(taskQuery).processInstanceId(procInstId);
        verify(taskQuery).taskDefinitionKey(activityId);
    }

    @Test
    public void fromShouldFilterOnTaskIdWhenProcessInstanceIsNotSet() {
        //given
        String taskId = "taskId";
        ActivitiActivityCancelledEventImpl internalEvent = new ActivitiActivityCancelledEventImpl();
        internalEvent.setExecutionId(taskId); //work around for standalone tasks, task id is set as execution id

        TaskQuery taskQuery = mock(TaskQuery.class,
                              Answers.RETURNS_SELF);
        given(taskService.createTaskQuery()).willReturn(taskQuery);

        Task internalTask = mock(Task.class);
        given(taskQuery.list()).willReturn(Collections.singletonList(internalTask));

        TaskImpl apiTask = new TaskImpl("id",
                                  "myTask",
                                  org.activiti.api.task.model.Task.TaskStatus.CREATED);
        given(taskConverter.from(internalTask)).willReturn(apiTask);

        //when
        TaskCancelledEvent convertedTaskCancelledEvent = eventConverter.from(internalEvent).orElse(null);

        //then
        assertThat(convertedTaskCancelledEvent).isNotNull();
        assertThat(convertedTaskCancelledEvent.getEntity()).isEqualTo(apiTask);
        assertThat(convertedTaskCancelledEvent.getEventType()).isEqualTo(TaskRuntimeEvent.TaskEvents.TASK_CANCELLED);

        verify(taskQuery).taskId(taskId);
    }
}
