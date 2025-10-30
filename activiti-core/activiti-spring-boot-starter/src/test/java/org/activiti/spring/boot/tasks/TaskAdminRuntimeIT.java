/*
 * Copyright 2010-2025 Hyland Software, Inc. and its affiliates.
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
package org.activiti.spring.boot.tasks;

import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.activiti.api.process.model.ProcessInstance;
import org.activiti.api.process.model.builders.ProcessPayloadBuilder;
import org.activiti.api.process.model.builders.StartProcessPayloadBuilder;
import org.activiti.api.process.runtime.ProcessAdminRuntime;
import org.activiti.api.runtime.shared.query.Pageable;
import org.activiti.api.task.model.Task;
import org.activiti.api.task.model.payloads.AssignTaskPayload;
import org.activiti.api.task.model.payloads.CompleteTaskPayload;
import org.activiti.api.task.runtime.TaskAdminRuntime;
import org.activiti.engine.delegate.event.ActivitiEventDispatcher;
import org.activiti.engine.impl.TaskServiceImpl;
import org.activiti.engine.impl.cfg.CommandExecutorImpl;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.history.HistoryManager;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandInterceptor;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntityImpl;
import org.activiti.engine.impl.persistence.entity.TaskEntityManager;
import org.activiti.engine.impl.persistence.entity.TaskEntityManagerImpl;
import org.activiti.runtime.api.impl.TaskAdminRuntimeImpl;
import org.activiti.spring.boot.security.util.SecurityUtil;
import org.activiti.spring.boot.test.util.ProcessCleanUpUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class TaskAdminRuntimeIT {
    @Nested
    class AutowiredITs {
        private static final String ADMIN = "admin";

        @Autowired
        private ProcessCleanUpUtil processCleanUpUtil;

        @Autowired
        private ProcessAdminRuntime processAdminRuntime;

        @Autowired
        private TaskAdminRuntime taskAdminRuntime;

        @Autowired
        private SecurityUtil securityUtil;

        @BeforeEach
        void setUp() {
            securityUtil.logInAs(ADMIN);
        }

        @AfterEach
        void taskCleanUp() {
            processCleanUpUtil.cleanUpWithAdmin();
        }

        @Test
        void should_returnLastCreatedTaskByProcessInstanceIdAndTaskDefinitionKey() {
            ProcessInstance processInstance = processAdminRuntime.start(
                ProcessPayloadBuilder
                    .start()
                    .withProcessDefinitionKey("Process_at2zjUes")
                    .build()
            );
            Task task = taskAdminRuntime
                .tasks(Pageable.of(0, 1))
                .getContent()
                .getFirst();

            Task retrievedTask = taskAdminRuntime.lastCreatedTaskByProcessInstanceIdAndTaskDefinitionKey(
                processInstance.getId(),
                task.getTaskDefinitionKey()
            );
            assertThat(retrievedTask).isEqualTo(task);
        }

        @Test
        void should_returnLastCreatedTaskByProcessInstanceIdAndTaskDefinitionKey_whenTaskIsInALoop() {
            String taskDefinitionKey = "Task_125yjke";
            final ProcessInstance processInstance = processAdminRuntime.start(
                new StartProcessPayloadBuilder()
                    .withProcessDefinitionKey("Process_N4qkN051N")
                    .build()
            );
            Task task1 = taskAdminRuntime.lastCreatedTaskByProcessInstanceIdAndTaskDefinitionKey(
                processInstance.getId(),
                taskDefinitionKey
            );

            //complete task and provide a value that causes a loop back
            taskAdminRuntime.complete(
                new CompleteTaskPayload(
                    task1.getId(),
                    singletonMap("formInput", "provided-it1")
                )
            );

            Task task2 = taskAdminRuntime.lastCreatedTaskByProcessInstanceIdAndTaskDefinitionKey(
                processInstance.getId(),
                taskDefinitionKey
            );
            assertThat(task2)
                .satisfies(t -> {
                    assertThat(t.getId()).isNotEqualTo(task1.getId());
                    assertThat(t.getProcessInstanceId())
                        .isEqualTo(processInstance.getId());
                    assertThat(t.getTaskDefinitionKey())
                        .isEqualTo(taskDefinitionKey);
                });
        }
    }

    @Test
    void assign_should_sendExactlyOneEvent_when_wasAlreadyAssignedBefore() {
        String taskId = "test-task-id";
        String assignee = "test-assignee";

        ActivitiEventDispatcher eventDispatcher = mock();
        CommandContext commandContext = mock();
        CommandInterceptor interceptor = mock();
        ProcessEngineConfigurationImpl processEngineConfiguration = mock();
        TaskEntity task = new TaskEntityImpl();
        task.setSuspensionState(1);
        task.setAssignee("old-assignee");
        HistoryManager historyManager = mock();

        AssignTaskPayload payload = new AssignTaskPayload(taskId, assignee);
        TaskServiceImpl taskService = new TaskServiceImpl(
            processEngineConfiguration
        );
        TaskEntityManager taskEntityManager = new TaskEntityManagerImpl(
            processEngineConfiguration,
            mock()
        );

        when(eventDispatcher.isEnabled()).thenReturn(true);
        when(processEngineConfiguration.getEventDispatcher())
            .thenReturn(eventDispatcher);
        when(processEngineConfiguration.getListenerNotificationHelper())
            .thenReturn(mock());
        when(processEngineConfiguration.getClock()).thenReturn(mock());
        when(commandContext.getTaskEntityManager())
            .thenReturn(taskEntityManager);
        when(commandContext.getHistoryManager()).thenReturn(historyManager);
        when(commandContext.getProcessEngineConfiguration()).thenReturn(processEngineConfiguration);
        when(processEngineConfiguration.getHistoryManager())
            .thenReturn(historyManager);
        when(taskEntityManager.findById(any())).thenReturn(task);
        when(interceptor.execute(any(), any()))
            .thenAnswer(
                (Answer<Void>) invocation -> {
                    Command<Void> command = invocation.getArgument(1);
                    command.execute(commandContext);
                    return null;
                }
            );

        taskService.setCommandExecutor(
            new CommandExecutorImpl(mock(), interceptor)
        );
        TaskAdminRuntime taskAdminRuntime = new TaskAdminRuntimeImpl(
            taskService,
            mock(),
            mock(),
            mock(),
            mock()
        );

        taskAdminRuntime.assign(payload);

        verify(eventDispatcher, times(1)).dispatchEvent(any());
        verify(eventDispatcher)
            .dispatchEvent(
                argThat(event -> event.getType().name().equals("TASK_ASSIGNED"))
            );
    }
}
