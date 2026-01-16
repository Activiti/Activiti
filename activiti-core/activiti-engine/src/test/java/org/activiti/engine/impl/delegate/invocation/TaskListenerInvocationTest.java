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
package org.activiti.engine.impl.delegate.invocation;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.history.HistoryLevel;
import org.activiti.engine.impl.history.HistoryManager;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.TaskEntityImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@RunWith(MockitoJUnitRunner.class)
public class TaskListenerInvocationTest {

    @Mock
    private CommandContext commandContext;
    @Mock
    private ProcessEngineConfigurationImpl processEngineConfiguration;
    @Mock
    private HistoryManager historyManager;


    @Test
    public void testInvokeToPassCodeCoverageTest() {
        // mock environment setup
        Context.setCommandContext(commandContext);
        given(commandContext.getProcessEngineConfiguration()).willReturn(processEngineConfiguration);
        given(commandContext.getHistoryManager()).willReturn(historyManager);
        given(processEngineConfiguration.getHistoryLevel()).willReturn(HistoryLevel.ACTIVITY);

        // build TaskListenerInvocation
        DelegateTask task = new TaskEntityImpl();
        TaskListener executionListener = new TaskListener() {
            @Override
            public void notify(DelegateTask delegateTask) {
                delegateTask.setAssignee("some user");
            }
        };
        TaskListenerInvocation taskListenerInvocation = new TaskListenerInvocation(executionListener, task);

        // call method
        taskListenerInvocation.invoke();

        assertThat(task.getAssignee()).isEqualTo("some user");
    }
}
