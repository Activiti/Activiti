/*
 * Copyright 2018 Alfresco, Inc. and/or its affiliates.
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

import java.util.Date;

import org.activiti.api.runtime.shared.identity.UserGroupManager;
import org.activiti.api.runtime.shared.security.SecurityManager;
import org.activiti.api.task.model.builders.TaskPayloadBuilder;
import org.activiti.api.task.model.impl.TaskImpl;
import org.activiti.api.task.model.payloads.UpdateTaskPayload;
import org.activiti.engine.TaskService;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.activiti.runtime.api.model.impl.APITaskConverter;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class TaskRuntimeHelperTest {

    private TaskRuntimeHelper taskRuntimeHelper;

    @Mock
    private SecurityManager securityManager;
    
    @Mock
    private UserGroupManager userGroupManager;

    @Mock
    private TaskService taskService;
    
    @Mock 
    private APITaskConverter taskConverter;
    
    @Before
    public void setUp() {
        initMocks(this);
        taskRuntimeHelper = spy(new TaskRuntimeHelper(taskService,
                            taskConverter,
                            securityManager,
                            userGroupManager));
        when(securityManager.getAuthenticatedUserId()).thenReturn("user");
    }

    @Test
    public void updateShouldSetAllFieldsAndSaveChangesWhenAssignee() {
        //given
        Date now = new Date();
        UpdateTaskPayload updateTaskPayload = TaskPayloadBuilder
                .update()
                .withTaskId("taskId")
                .withDescription("new description")
                .withName("New name")
                .withPriority(42)
                .withDueDate(now)
                .withFormKey("new form key")
                .build();
        String assignee = "user";
        Task internalTask = buildInternalTask(assignee);
        doReturn(internalTask).when(taskRuntimeHelper).getInternalTaskWithChecks("taskId");
        doReturn(internalTask).when(taskRuntimeHelper).getInternalTask("taskId");

        //when
        taskRuntimeHelper.applyUpdateTaskPayload(false, updateTaskPayload);

        //then
        verify(internalTask).setDescription("new description");
        verify(internalTask).setName("New name");
        verify(internalTask).setPriority(42);
        verify(internalTask).setDueDate(now);
        verify(internalTask).setFormKey("new form key");
        verify(taskService).saveTask(internalTask);
    }

    private Task buildInternalTask(String assignee) {
        Task internalTask = mock(Task.class);
        given(internalTask.getAssignee()).willReturn(assignee);
        return internalTask;
    }

    @Test
    public void applyUpdateTaskPayloadShouldThrowExceptionWhenAssigneeIsNotSetAndIsNotAdmin() {
        //given
        TaskQuery taskQuery = mock(TaskQuery.class);
        given(taskService.createTaskQuery()).willReturn(taskQuery);
        given(taskQuery.taskCandidateOrAssigned(any(), any())).willReturn(taskQuery);
        given(taskQuery.taskId("taskId")).willReturn(taskQuery);

        Task internalTask = mock(Task.class);
        doReturn(internalTask).when(taskRuntimeHelper).getInternalTaskWithChecks("taskId");

        UpdateTaskPayload updateTaskPayload = TaskPayloadBuilder
                .update()
                .withTaskId("taskId")
                .withDescription("new description")
                .build();

        //when
        Throwable throwable = catchThrowable(() -> taskRuntimeHelper.applyUpdateTaskPayload(false, updateTaskPayload));

        //then
        assertThat(throwable)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("You cannot update a task where you are not the assignee");
    }

    @Test
    public void updateShouldBeAbleToUpdateDescriptionOnly() {
        //given
        UpdateTaskPayload updateTaskPayload = TaskPayloadBuilder
                .update()
                .withTaskId("taskId")
                .withDescription("new description")
                .build();
        TaskImpl task = new TaskImpl();
        String assignee = "user";
        task.setAssignee(assignee);
        Task internalTask = buildInternalTask(assignee);

        doReturn(internalTask).when(taskRuntimeHelper).getInternalTaskWithChecks("taskId");
        doReturn(internalTask).when(taskRuntimeHelper).getInternalTask("taskId");

        TaskQuery taskQuery = mock(TaskQuery.class);
        given(taskQuery.taskId("taskId")).willReturn(taskQuery);
        given(taskService.createTaskQuery()).willReturn(taskQuery);

        TaskRuntimeHelper taskUpdater=mock(TaskRuntimeHelper.class);

        given(taskQuery.singleResult()).willReturn(internalTask);
        when(taskUpdater.getInternalTaskWithChecks(Mockito.any())).thenReturn(internalTask);
        
        
        Mockito.when(taskConverter.from(Mockito.any(Task.class))).thenReturn(task);
          
        //when
        taskRuntimeHelper.applyUpdateTaskPayload(false, updateTaskPayload);

        //then
        verify(internalTask).getDescription();
        verify(internalTask).setDescription("new description");

        verify(taskService).saveTask(internalTask);
    }
}