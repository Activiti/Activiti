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
package org.activiti.engine.impl.cmd;

import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;
import org.activiti.engine.ActivitiTaskAlreadyClaimedException;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.history.HistoryManager;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntityManager;
import org.activiti.engine.runtime.Clock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ClaimTaskCmdTest {

    private static final String TASK_ID = "task123";
    private static final String NEW_USER_ID = "user456";
    private CommandContext commandContext;
    private TaskEntity task;
    private TaskEntityManager taskEntityManager;
    private HistoryManager historyManager;
    private Date currentTime;

    @BeforeEach
    void setUp() {
        commandContext = mock();
        task = mock();
        taskEntityManager = mock();
        ProcessEngineConfigurationImpl processEngineConfiguration = mock();
        Clock clock = mock();
        historyManager = mock();
        currentTime = new Date();

        when(task.getId()).thenReturn(TASK_ID);
        when(commandContext.getProcessEngineConfiguration())
            .thenReturn(processEngineConfiguration);
        when(processEngineConfiguration.getClock()).thenReturn(clock);
        when(clock.getCurrentTime()).thenReturn(currentTime);
        when(commandContext.getTaskEntityManager())
            .thenReturn(taskEntityManager);
        when(commandContext.getHistoryManager()).thenReturn(historyManager);
        when(task.getAssignee()).thenReturn(null);
    }

    @Test
    void should_changeAssignee_when_taskIsNotAssignedYet() {
        ClaimTaskCmd cmd = new ClaimTaskCmd(TASK_ID, NEW_USER_ID);

        cmd.execute(commandContext, task);

        verify(task).setClaimTime(currentTime);
        verify(taskEntityManager).changeTaskAssignee(task, NEW_USER_ID);
        verify(historyManager).recordTaskClaim(task);
    }

    @Test
    void should_keepAssignee_when_taskIsAlreadyAssignedToTheUser() {
        when(task.getAssignee()).thenReturn(NEW_USER_ID);
        ClaimTaskCmd cmd = new ClaimTaskCmd(TASK_ID, NEW_USER_ID);

        cmd.execute(commandContext, task);

        verify(task).setClaimTime(currentTime);
        verify(taskEntityManager, times(0)).changeTaskAssignee(task, NEW_USER_ID);
        verify(historyManager).recordTaskClaim(task);
    }

    @Test
    void should_throwException_when_taskIsAlreadyAssignedToAnotherUser() {
        String existingAssignee = "user789";
        when(task.getAssignee()).thenReturn(existingAssignee);
        ClaimTaskCmd cmd = new ClaimTaskCmd(TASK_ID, NEW_USER_ID);

        assertThrows(
            ActivitiTaskAlreadyClaimedException.class,
            () -> {
                cmd.execute(commandContext, task);
            }
        );
    }

    @Test
    void should_removeAssignee_when_taskIsAlreadyAssignedToAUser() {
        String existingAssignee = "user789";
        when(task.getAssignee()).thenReturn(existingAssignee);
        ClaimTaskCmd cmd = new ClaimTaskCmd(TASK_ID, null);

        cmd.execute(commandContext, task);

        verify(task).setClaimTime(null);
        verify(taskEntityManager).changeTaskAssignee(task, null);
        verify(historyManager).recordTaskClaim(task);
    }

    @Test
    void should_removeAssignee_when_taskIsNotAssignedYet() {
        ClaimTaskCmd cmd = new ClaimTaskCmd(TASK_ID, null);

        cmd.execute(commandContext, task);

        verify(task).setClaimTime(null);
        verify(taskEntityManager).changeTaskAssignee(task, null);
        verify(historyManager).recordTaskClaim(task);
    }
}
