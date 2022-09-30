/*
 * Copyright 2010-2020 Alfresco Software, Ltd.
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

import org.activiti.api.runtime.shared.query.Page;
import org.activiti.api.runtime.shared.security.SecurityManager;
import org.activiti.api.task.model.Task;
import org.activiti.api.task.model.payloads.AssignTasksPayload;
import org.activiti.engine.TaskService;
import org.activiti.engine.impl.persistence.entity.TaskEntityImpl;
import org.activiti.runtime.api.model.impl.APITaskConverter;
import org.activiti.runtime.api.model.impl.APIVariableInstanceConverter;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskAdminRuntimeImplTest {

    @Mock
    private TaskService taskService;
    @Mock
    private APIVariableInstanceConverter variableInstanceConverter;
    @Mock
    private TaskRuntimeHelper taskRuntimeHelper;
    @Mock
    private SecurityManager securityManager;
    @Spy
    private APITaskConverter taskConverter = new APITaskConverter(taskService);

    @InjectMocks
    private TaskAdminRuntimeImpl taskAdminRuntime;

    @Test
    void should_assignOneTask() {
        final String taskId = "taskId";
        final String assignee = "hruser";
        AssignTasksPayload assignTasksPayload = new AssignTasksPayload(List.of(taskId), assignee);

        TaskEntityImpl task = new TaskEntityImpl();
        task.setId(taskId);
        task.setAssignee(assignee);
        when(taskRuntimeHelper.getInternalTask(taskId)).thenReturn(task);

        Page<Task> tasks = taskAdminRuntime.assignMultiple(assignTasksPayload);

        verify(taskService).unclaim(eq(taskId));
        verify(taskService).claim(eq(taskId), eq(assignee));
        Assertions.assertThat(tasks.getContent()).hasSize(1);
        Assertions.assertThat(tasks.getContent()).extracting(Task::getAssignee).containsExactly(assignee);
    }

    @Test
    void should_assignMultipleTasks() {
        final String taskId1 = "taskId1";
        final String taskId2 = "taskId2";
        final String assignee = "hruser";
        AssignTasksPayload assignTasksPayload = new AssignTasksPayload(List.of(taskId1, taskId2), assignee);

        TaskEntityImpl task1 = new TaskEntityImpl();
        task1.setId(taskId1);
        task1.setAssignee(assignee);
        when(taskRuntimeHelper.getInternalTask(taskId1)).thenReturn(task1);

        TaskEntityImpl task2 = new TaskEntityImpl();
        task2.setId(taskId2);
        task2.setAssignee(assignee);
        when(taskRuntimeHelper.getInternalTask(taskId2)).thenReturn(task2);

        Page<Task> tasks = taskAdminRuntime.assignMultiple(assignTasksPayload);

        verify(taskService).unclaim(eq(taskId1));
        verify(taskService).claim(eq(taskId1), eq(assignee));
        verify(taskService).unclaim(eq(taskId2));
        verify(taskService).claim(eq(taskId2), eq(assignee));
        Assertions.assertThat(tasks.getContent()).hasSize(2);
        Assertions.assertThat(tasks.getContent()).extracting(Task::getAssignee).containsOnly(assignee);
    }

    @Test
    void should_notAssign_when_listIsEmpty() {
        final String assignee = "hruser";
        AssignTasksPayload assignTasksPayload = new AssignTasksPayload(List.of(), assignee);

        Page<Task> tasks = taskAdminRuntime.assignMultiple(assignTasksPayload);

        verify(taskService, never()).unclaim(any());
        verify(taskService, never()).claim(any(), any());
        Assertions.assertThat(tasks.getContent()).hasSize(0);
    }

}
