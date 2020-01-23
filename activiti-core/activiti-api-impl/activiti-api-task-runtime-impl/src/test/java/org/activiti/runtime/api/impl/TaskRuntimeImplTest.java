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

package org.activiti.runtime.api.impl;

import org.activiti.api.runtime.shared.security.SecurityManager;
import org.activiti.api.task.model.Task;
import org.activiti.api.task.model.builders.TaskPayloadBuilder;
import org.activiti.api.task.model.impl.TaskImpl;
import org.activiti.api.task.model.payloads.CreateTaskPayload;
import org.activiti.api.task.model.payloads.UpdateTaskPayload;
import org.activiti.engine.TaskService;
import org.activiti.engine.impl.TaskQueryImpl;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.task.IdentityLinkType;
import org.activiti.runtime.api.model.impl.APITaskConverter;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class TaskRuntimeImplTest {

    @InjectMocks
    private TaskRuntimeImpl taskRuntime;

    @Mock
    private TaskRuntimeHelper taskRuntimeHelper;

    @Mock
    private APITaskConverter taskConverter;

    @Mock
    private org.activiti.engine.task.Task engineTaskMock;

    @Mock
    private SecurityManager securityManager;

    @Mock
    private TaskService taskService;

    @Mock
    private IdentityLink identityLink;

    @Before
    public void setUp() {
        initMocks(this);
    }

    @Test
    public void should_thrownIllegalStateException_when_assigneeOrCandidatesAreNotSpecified() {
        CreateTaskPayload createTaskPayload = TaskPayloadBuilder
                                                      .create()
                                                      .withDescription("new description")
                                                      .build();


        assertThatThrownBy(() -> taskRuntime.create(createTaskPayload))
                .isInstanceOf(IllegalStateException.class)
                .withFailMessage("You cannot create a task without an assignee or candidate users or groups");
    }

    @Test
    public void should_createTask_when_assigneeIsSpecified() {

        given(taskService.newTask())
                .willReturn(engineTaskMock);

        given(taskConverter.from(any(org.activiti.engine.task.Task.class)))
                .willReturn(new TaskImpl());

        Task taskWithAssignee = taskRuntime.create(TaskPayloadBuilder
                                               .create()
                                               .withAssignee("test")
                                               .withName("name")
                                               .build());

        assertThat(taskWithAssignee).isNotNull();

        verify(taskService).saveTask(eq(engineTaskMock));
        verify(securityManager).getAuthenticatedUserId();
        verify(taskService, never()).addCandidateUser(any(), any());
        verify(taskService, never()).addCandidateGroup(any(), any());
    }

    @Test
    public void should_createTask_when_CandidateGroupIsSpecified() {

        given(taskService.newTask())
                .willReturn(engineTaskMock);

        given(taskConverter.from(any(org.activiti.engine.task.Task.class)))
                .willReturn(new TaskImpl());

        Task taskWithCandidateGroup = taskRuntime.create(TaskPayloadBuilder
                                                                 .create()
                                                                 .withCandidateGroup("testGroup")
                                                                 .withName("name")
                                                                 .build());

        assertThat(taskWithCandidateGroup).isNotNull();

        verify(taskService, times(1)).saveTask(eq(engineTaskMock));
        verify(securityManager, times(1)).getAuthenticatedUserId();
        verify(taskService, never()).addCandidateUser(any(), any());
        verify(taskService, times(1)).addCandidateGroup(any(), eq("testGroup"));
    }

    @Test
    public void should_createTask_when_CandidateUserIsSpecified() {

        given(taskService.newTask())
                .willReturn(engineTaskMock);

        given(taskConverter.from(any(org.activiti.engine.task.Task.class)))
                .willReturn(new TaskImpl());

        Task taskWithCandidateUser = taskRuntime.create(TaskPayloadBuilder
                                                                .create()
                                                                .withCandidateUsers("testUser")
                                                                .withName("name")
                                                                .build());

        assertThat(taskWithCandidateUser).isNotNull();

        verify(taskService, times(1)).saveTask(eq(engineTaskMock));
        verify(securityManager, times(1)).getAuthenticatedUserId();
        verify(taskService, times(1)).addCandidateUser(any(), eq("testUser"));
        verify(taskService, never()).addCandidateGroup(any(), any());
    }

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
    public void should_doNotReturnCandidates_when_taskInstanceIsntTaskImpl(){
        String taskId = "taskId";
        Task taskMock = mock(Task.class);
        given(taskConverter.from(any(org.activiti.engine.task.Task.class)))
                .willReturn(taskMock);
        given(taskRuntimeHelper.getInternalTaskWithChecks(any()))
                .willReturn(engineTaskMock);

        Task task = taskRuntime.task(taskId);

        assertThat(task).isNotInstanceOf(TaskImpl.class);
        assertThat(task).isEqualTo(taskMock);
    }

    @Test
    public void should_returnCandidateUsersAndGroups_when_getTaskById(){
        String taskId = "taskId";
        given(taskRuntimeHelper.getInternalTaskWithChecks(any()))
                .willReturn(engineTaskMock);

        given(taskConverter.from(any(org.activiti.engine.task.Task.class)))
                .willReturn(new TaskImpl(taskId, "task name", Task.TaskStatus.CREATED));

        given(securityManager.getAuthenticatedUserId())
                .willReturn("userId");

        TaskQueryImpl taskQuery = spy(new TaskQueryImpl());

        given(taskService.createTaskQuery())
                .willReturn(taskQuery);

        doReturn(engineTaskMock)
                .when(taskQuery)
                .singleResult();

        given(taskService.getIdentityLinksForTask(any()))
                .willReturn(asList(identityLink));

        given(identityLink.getType())
                .willReturn(IdentityLinkType.CANDIDATE);

        given(identityLink.getUserId())
                .willReturn("user");

        given(identityLink.getGroupId())
                .willReturn("group");

        TaskImpl task = (TaskImpl) taskRuntime.task(taskId);
        assertThat(task.getCandidateUsers()).isNotNull();
        assertThat(task.getCandidateUsers()).containsExactly("user");
        assertThat(task.getCandidateGroups()).isNotNull();
        assertThat(task.getCandidateGroups()).containsExactly("group");


        verify(taskRuntimeHelper).getInternalTaskWithChecks(eq(taskId));
        verify(taskService, times(2)).getIdentityLinksForTask(eq(taskId));
    }
}