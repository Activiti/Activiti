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
import java.util.stream.Stream;
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
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
    void should_returnResultOfHelper_when_updateTask() {
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
    void assign_should_returnIllegalStateException_when_assigneeIsNotACandidateUser() {
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
    void assign_should_updateTaskAssignee_whenAssigneeIsACandidateUser() {
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

    @ParameterizedTest(name = "sorting by createdDate {0}")
    @MethodSource("provideOrderDirections")
    void tasks_should_invokeOrderByTaskCreateTime_when_sortingByCreatedDate(
        Order.Direction direction,
        java.util.function.Function<TaskQuery, TaskQuery> directionMethod,
        java.util.function.Consumer<TaskQuery> verifyDirection
    ) {
        //given
        when(securityManager.getAuthenticatedUserId()).thenReturn(AUTHENTICATED_USER);
        when(securityManager.getAuthenticatedUserGroups()).thenReturn(Collections.emptyList());

        TaskQuery taskQuery = mock();
        TaskQuery sortedQuery = mock();
        TaskQuery directedQuery = mock();

        when(taskService.createTaskQuery()).thenReturn(taskQuery);
        when(taskQuery.or()).thenReturn(taskQuery);
        when(taskQuery.taskCandidateOrAssigned(AUTHENTICATED_USER, Collections.emptyList()))
            .thenReturn(taskQuery);
        when(taskQuery.taskOwner(AUTHENTICATED_USER)).thenReturn(taskQuery);
        when(taskQuery.endOr()).thenReturn(taskQuery);
        when(taskQuery.orderByTaskCreateTime()).thenReturn(sortedQuery);
        when(directionMethod.apply(sortedQuery)).thenReturn(directedQuery);
        when(directedQuery.listPage(0, 50)).thenReturn(Collections.emptyList());
        when(directedQuery.count()).thenReturn(0L);
        when(taskConverter.from(Collections.emptyList())).thenReturn(Collections.emptyList());

        Order order = Order.by("createdDate", direction);
        Pageable pageable = Pageable.of(0, 50, order);
        GetTasksPayload payload = TaskPayloadBuilder.tasks().build();

        //when
        taskRuntime.tasks(pageable, payload);

        //then
        verify(taskQuery).orderByTaskCreateTime();
        verifyDirection.accept(sortedQuery);
    }

    private static Stream<Arguments> provideOrderDirections() {
        return Stream.of(
            Arguments.of(
                Order.Direction.ASC,
                (java.util.function.Function<TaskQuery, TaskQuery>) TaskQuery::asc,
                (java.util.function.Consumer<TaskQuery>) (query) -> verify(query).asc()
            ),
            Arguments.of(
                Order.Direction.DESC,
                (java.util.function.Function<TaskQuery, TaskQuery>) TaskQuery::desc,
                (java.util.function.Consumer<TaskQuery>) (query) -> verify(query).desc()
            )
        );
    }

    @Test
    void tasks_should_ignoreUnsupportedField_gracefully() {
        //given
        when(securityManager.getAuthenticatedUserId()).thenReturn(AUTHENTICATED_USER);
        when(securityManager.getAuthenticatedUserGroups()).thenReturn(Collections.emptyList());

        TaskQuery taskQuery = mock();

        when(taskService.createTaskQuery()).thenReturn(taskQuery);
        when(taskQuery.or()).thenReturn(taskQuery);
        when(taskQuery.taskCandidateOrAssigned(AUTHENTICATED_USER, Collections.emptyList()))
            .thenReturn(taskQuery);
        when(taskQuery.taskOwner(AUTHENTICATED_USER)).thenReturn(taskQuery);
        when(taskQuery.endOr()).thenReturn(taskQuery);
        when(taskQuery.listPage(0, 50)).thenReturn(Collections.emptyList());
        when(taskQuery.count()).thenReturn(0L);
        when(taskConverter.from(Collections.emptyList())).thenReturn(Collections.emptyList());

        Order order = Order.by("unsupportedField", Order.Direction.ASC);
        Pageable pageable = Pageable.of(0, 50, order);
        GetTasksPayload payload = TaskPayloadBuilder.tasks().build();

        //when
        taskRuntime.tasks(pageable, payload);

        //then
        verify(taskQuery, never()).orderByTaskCreateTime();
    }

    @ParameterizedTest(name = "with {0}")
    @MethodSource("provideNullOrderScenarios")
    void tasks_should_handleNullOrder_gracefully(String scenario, Pageable pageable) {
        //given
        when(securityManager.getAuthenticatedUserId()).thenReturn(AUTHENTICATED_USER);
        when(securityManager.getAuthenticatedUserGroups()).thenReturn(Collections.emptyList());

        TaskQuery taskQuery = mock();

        when(taskService.createTaskQuery()).thenReturn(taskQuery);
        when(taskQuery.or()).thenReturn(taskQuery);
        when(taskQuery.taskCandidateOrAssigned(AUTHENTICATED_USER, Collections.emptyList()))
            .thenReturn(taskQuery);
        when(taskQuery.taskOwner(AUTHENTICATED_USER)).thenReturn(taskQuery);
        when(taskQuery.endOr()).thenReturn(taskQuery);
        when(taskQuery.listPage(0, 50)).thenReturn(Collections.emptyList());
        when(taskQuery.count()).thenReturn(0L);
        when(taskConverter.from(Collections.emptyList())).thenReturn(Collections.emptyList());

        GetTasksPayload payload = TaskPayloadBuilder.tasks().build();

        //when
        taskRuntime.tasks(pageable, payload);

        //then
        verify(taskQuery, never()).orderByTaskCreateTime();
    }

    private static Stream<Arguments> provideNullOrderScenarios() {
        return Stream.of(
            Arguments.of("null order", Pageable.of(0, 50)),
            Arguments.of("null property", Pageable.of(0, 50, Order.by(null, Order.Direction.ASC)))
        );
    }

    @Test
    void tasks_should_handleNullDirection_gracefully() {
        //given
        when(securityManager.getAuthenticatedUserId()).thenReturn(AUTHENTICATED_USER);
        when(securityManager.getAuthenticatedUserGroups()).thenReturn(Collections.emptyList());

        TaskQuery taskQuery = mock();
        Order mockOrderNullDirection = mock(Order.class);
        when(mockOrderNullDirection.getProperty()).thenReturn("createdDate");
        when(mockOrderNullDirection.getDirection()).thenReturn(null);

        when(taskService.createTaskQuery()).thenReturn(taskQuery);
        when(taskQuery.or()).thenReturn(taskQuery);
        when(taskQuery.taskCandidateOrAssigned(AUTHENTICATED_USER, Collections.emptyList()))
            .thenReturn(taskQuery);
        when(taskQuery.taskOwner(AUTHENTICATED_USER)).thenReturn(taskQuery);
        when(taskQuery.endOr()).thenReturn(taskQuery);
        when(taskQuery.listPage(0, 50)).thenReturn(Collections.emptyList());
        when(taskQuery.count()).thenReturn(0L);
        when(taskConverter.from(Collections.emptyList())).thenReturn(Collections.emptyList());

        Pageable pageable = Pageable.of(0, 50, mockOrderNullDirection);
        GetTasksPayload payload = TaskPayloadBuilder.tasks().build();

        //when
        taskRuntime.tasks(pageable, payload);

        //then
        verify(taskQuery, never()).orderByTaskCreateTime();
    }
}
