/*
 * Copyright 2010-2026 Hyland Software, Inc. and its affiliates.
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.activiti.api.runtime.shared.query.Order;
import org.activiti.api.runtime.shared.query.Pageable;
import org.activiti.api.runtime.shared.security.SecurityManager;
import org.activiti.api.task.model.Task;
import org.activiti.api.task.model.builders.TaskPayloadBuilder;
import org.activiti.api.task.model.impl.TaskImpl;
import org.activiti.api.task.model.payloads.AssignTaskPayload;
import org.activiti.api.task.model.payloads.GetTasksPayload;
import org.activiti.api.task.model.payloads.UpdateTaskPayload;
import org.activiti.engine.TaskService;
import org.activiti.engine.task.TaskQuery;
import org.activiti.runtime.api.model.impl.APITaskConverter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
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
        UpdateTaskPayload updateTaskPayload = TaskPayloadBuilder.update()
            .withTaskId("taskId")
            .withDescription("new description")
            .build();

        TaskImpl updatedTask = new TaskImpl();
        given(taskRuntimeHelper.applyUpdateTaskPayload(false, updateTaskPayload)).willReturn(updatedTask);

        //when
        Task retrievedTask = taskRuntime.update(updateTaskPayload);

        //then
        assertThat(retrievedTask).isEqualTo(updatedTask);
    }

    @Test
    public void assign_should_returnIllegalStateException_when_assigneeIsNotACandidateUser() {
        //given
        AssignTaskPayload assignTaskPayload = TaskPayloadBuilder.assign()
            .withTaskId("taskId")
            .withAssignee("assignee")
            .build();
        List<String> userCandidates = Collections.emptyList();
        doReturn(userCandidates).when(taskRuntime).userCandidates("taskId");

        //when
        Throwable thrown = catchThrowable(() -> taskRuntime.assign(assignTaskPayload));

        //then
        assertThat(thrown)
            .isInstanceOf(IllegalStateException.class)
            .hasMessageStartingWith(
                "You cannot assign a task to " + assignTaskPayload.getAssignee() + " due it is not a candidate for it"
            );
    }

    @Test
    public void assign_should_updateTaskAssignee_whenAssigneeIsACandidateUser() {
        //given
        when(securityManager.getAuthenticatedUserId()).thenReturn(AUTHENTICATED_USER);

        String taskId = "taskId";
        String newAssignee = "newAssignee";
        AssignTaskPayload assignTaskPayload = TaskPayloadBuilder.assign()
            .withTaskId(taskId)
            .withAssignee(newAssignee)
            .build();
        List<String> userCandidates = Arrays.asList(newAssignee);
        doReturn(userCandidates).when(taskRuntime).userCandidates(taskId);
        TaskImpl task = mock(TaskImpl.class);
        given(task.getAssignee()).willReturn("user");
        doReturn(task).when(taskConverter).fromWithCandidates(any());

        taskRuntime.assign(assignTaskPayload);

        verify(taskService).unclaim(taskId);
        verify(taskService).claim(taskId, newAssignee);
    }

    @Test
    public void tasks_should_invokeOrderByTaskCreateTimeAsc_when_sortingByCreatedDateAsc() {
        //given
        when(securityManager.getAuthenticatedUserId()).thenReturn(AUTHENTICATED_USER);
        when(securityManager.getAuthenticatedUserGroups()).thenReturn(Collections.emptyList());

        TaskQuery taskQuery = mock(TaskQuery.class);
        TaskQuery sortedQuery = mock(TaskQuery.class);
        TaskQuery ascQuery = mock(TaskQuery.class);

        when(taskService.createTaskQuery()).thenReturn(taskQuery);
        when(taskQuery.or()).thenReturn(taskQuery);
        when(taskQuery.taskCandidateOrAssigned(AUTHENTICATED_USER, Collections.emptyList()))
            .thenReturn(taskQuery);
        when(taskQuery.taskOwner(AUTHENTICATED_USER)).thenReturn(taskQuery);
        when(taskQuery.endOr()).thenReturn(taskQuery);
        when(taskQuery.orderByTaskCreateTime()).thenReturn(sortedQuery);
        when(sortedQuery.asc()).thenReturn(ascQuery);
        when(ascQuery.listPage(0, 50)).thenReturn(Collections.emptyList());
        when(ascQuery.count()).thenReturn(0L);
        when(taskConverter.from(Collections.emptyList())).thenReturn(Collections.emptyList());

        Order order = Order.by("createdDate", Order.Direction.ASC);
        Pageable pageable = Pageable.of(0, 50, order);
        GetTasksPayload payload = TaskPayloadBuilder.tasks().build();

        //when
        taskRuntime.tasks(pageable, payload);

        //then
        verify(taskQuery).orderByTaskCreateTime();
        verify(sortedQuery).asc();
    }

    @Test
    public void tasks_should_invokeOrderByTaskCreateTimeDesc_when_sortingByCreatedDateDesc() {
        //given
        when(securityManager.getAuthenticatedUserId()).thenReturn(AUTHENTICATED_USER);
        when(securityManager.getAuthenticatedUserGroups()).thenReturn(Collections.emptyList());

        TaskQuery taskQuery = mock(TaskQuery.class);
        TaskQuery sortedQuery = mock(TaskQuery.class);
        TaskQuery descQuery = mock(TaskQuery.class);

        when(taskService.createTaskQuery()).thenReturn(taskQuery);
        when(taskQuery.or()).thenReturn(taskQuery);
        when(taskQuery.taskCandidateOrAssigned(AUTHENTICATED_USER, Collections.emptyList()))
            .thenReturn(taskQuery);
        when(taskQuery.taskOwner(AUTHENTICATED_USER)).thenReturn(taskQuery);
        when(taskQuery.endOr()).thenReturn(taskQuery);
        when(taskQuery.orderByTaskCreateTime()).thenReturn(sortedQuery);
        when(sortedQuery.desc()).thenReturn(descQuery);
        when(descQuery.listPage(0, 50)).thenReturn(Collections.emptyList());
        when(descQuery.count()).thenReturn(0L);
        when(taskConverter.from(Collections.emptyList())).thenReturn(Collections.emptyList());

        Order order = Order.by("createdDate", Order.Direction.DESC);
        Pageable pageable = Pageable.of(0, 50, order);
        GetTasksPayload payload = TaskPayloadBuilder.tasks().build();

        //when
        taskRuntime.tasks(pageable, payload);

        //then
        verify(taskQuery).orderByTaskCreateTime();
        verify(sortedQuery).desc();
    }

    @Test
    public void tasks_should_throwException_when_sortingByUnsupportedField() {
        //given
        when(securityManager.getAuthenticatedUserId()).thenReturn(AUTHENTICATED_USER);
        when(securityManager.getAuthenticatedUserGroups()).thenReturn(Collections.emptyList());

        TaskQuery taskQuery = mock(TaskQuery.class);

        when(taskService.createTaskQuery()).thenReturn(taskQuery);
        when(taskQuery.or()).thenReturn(taskQuery);
        when(taskQuery.taskCandidateOrAssigned(AUTHENTICATED_USER, Collections.emptyList()))
            .thenReturn(taskQuery);
        when(taskQuery.taskOwner(AUTHENTICATED_USER)).thenReturn(taskQuery);
        when(taskQuery.endOr()).thenReturn(taskQuery);

        Order order = Order.by("unsupportedField", Order.Direction.ASC);
        Pageable pageable = Pageable.of(0, 50, order);
        GetTasksPayload payload = TaskPayloadBuilder.tasks().build();

        //when
        Throwable thrown = catchThrowable(() -> taskRuntime.tasks(pageable, payload));

        //then
        assertThat(thrown)
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Sorting by unsupportedField is not supported");
    }

    @Test
    public void tasks_should_handleNullOrder_gracefully() {
        //given
        when(securityManager.getAuthenticatedUserId()).thenReturn(AUTHENTICATED_USER);
        when(securityManager.getAuthenticatedUserGroups()).thenReturn(Collections.emptyList());

        TaskQuery taskQuery = mock(TaskQuery.class);

        when(taskService.createTaskQuery()).thenReturn(taskQuery);
        when(taskQuery.or()).thenReturn(taskQuery);
        when(taskQuery.taskCandidateOrAssigned(AUTHENTICATED_USER, Collections.emptyList()))
            .thenReturn(taskQuery);
        when(taskQuery.taskOwner(AUTHENTICATED_USER)).thenReturn(taskQuery);
        when(taskQuery.endOr()).thenReturn(taskQuery);
        when(taskQuery.listPage(0, 50)).thenReturn(Collections.emptyList());
        when(taskQuery.count()).thenReturn(0L);
        when(taskConverter.from(Collections.emptyList())).thenReturn(Collections.emptyList());

        Pageable pageable = Pageable.of(0, 50);
        GetTasksPayload payload = TaskPayloadBuilder.tasks().build();

        //when
        taskRuntime.tasks(pageable, payload);

        //then
        verify(taskQuery, never()).orderByTaskCreateTime();
    }
}
