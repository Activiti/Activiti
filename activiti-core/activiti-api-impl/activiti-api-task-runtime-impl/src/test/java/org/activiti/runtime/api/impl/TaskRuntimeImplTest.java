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
package org.activiti.runtime.api.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.activiti.api.runtime.shared.security.SecurityManager;
import org.activiti.api.task.model.Task;
import org.activiti.api.task.model.builders.TaskPayloadBuilder;
import org.activiti.api.task.model.impl.TaskImpl;
import org.activiti.api.task.model.payloads.AssignTaskPayload;
import org.activiti.api.task.model.payloads.UpdateTaskPayload;
import org.activiti.engine.TaskService;
import org.activiti.runtime.api.model.impl.APITaskConverter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class TaskRuntimeImplTest {

    private static final String AUTHENTICATED_USER = "user";

    @Spy
    @InjectMocks
    private TaskRuntimeImpl taskRuntime;

    @Mock
    private TaskRuntimeHelper taskRuntimeHelper;

    @Mock
    private APITaskConverter taskConverter;

    @Mock
    private SecurityManager securityManager;

    @Mock
    private TaskService taskService;

    @Test
    public void should_returnResultOfHelper_when_updateTask() {
        //given
        UpdateTaskPayload updateTaskPayload = TaskPayloadBuilder
                .update()
                .withTaskId("taskId")
                .withDescription("new description")
                .build();

        TaskImpl updatedTask = new TaskImpl();
        given(taskRuntimeHelper.applyUpdateTaskPayload(false,
                                                       updateTaskPayload)).willReturn(updatedTask);

        //when
        Task retrievedTask = taskRuntime.update(updateTaskPayload);

        //then
        assertThat(retrievedTask).isEqualTo(updatedTask);
    }

    @Test
    public void assign_should_returnIllegalStateException_when_assigneeIsNotACandidateUser() {
        //given
        AssignTaskPayload assignTaskPayload = TaskPayloadBuilder
                .assign()
                .withTaskId("taskId")
                .withAssignee("assignee")
                .build();
        List<String> userCandidates = Collections.emptyList();
        doReturn(userCandidates).when(taskRuntime).userCandidates( "taskId");

        //when
        Throwable thrown = catchThrowable(() -> taskRuntime.assign(assignTaskPayload));

        //then
        assertThat(thrown)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageStartingWith("You cannot assign a task to " + assignTaskPayload.getAssignee()
                        + " due it is not a candidate for it");
    }

    @Test
    public void assign_should_updateTaskAssignee_whenAssigneeIsACandidateUser() {
        //given
        when(securityManager.getAuthenticatedUserId()).thenReturn(AUTHENTICATED_USER);

        String taskId = "taskId";
        String newAssignee = "newAssignee";
        AssignTaskPayload assignTaskPayload = TaskPayloadBuilder
                .assign()
                .withTaskId(taskId)
                .withAssignee(newAssignee)
                .build();
        List<String> userCandidates = Arrays.asList(newAssignee);
        doReturn(userCandidates).when(taskRuntime).userCandidates(taskId);
        TaskImpl task =  mock(TaskImpl.class);
        given(task.getAssignee()).willReturn("user");
        doReturn(task).when(taskConverter).fromWithCandidates(any());

        taskRuntime.assign(assignTaskPayload);

        verify(taskService).unclaim(taskId);
        verify(taskService).claim(taskId, newAssignee);
    }

}
