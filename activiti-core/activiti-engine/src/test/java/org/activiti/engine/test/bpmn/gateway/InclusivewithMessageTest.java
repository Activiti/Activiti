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


package org.activiti.engine.test.bpmn.gateway;

import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;

import java.util.List;

import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;

public class InclusivewithMessageTest extends PluggableActivitiTestCase {
    private static final String PROCESS_DEFINITION_KEY = "InclusiveTest";

    private String deploymentId;

    protected void setUp() throws Exception {
        super.setUp();
        deploymentId = repositoryService.createDeployment()
            .addClasspathResource("org/activiti/engine/test/bpmn/gateway/InclusiveTest.bpmn20.xml")
            .deploy().getId();
    }

    protected void tearDown() throws Exception {
        repositoryService.deleteDeployment(deploymentId, true);
        super.tearDown();
    }

    public void testDefaultFlowOnly() {
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(PROCESS_DEFINITION_KEY);

        Execution execution = runtimeService.createExecutionQuery().messageEventSubscriptionName("message2").singleResult();
        assertThat(execution).isNotNull();

        runtimeService.messageEventReceived("message2", execution.getId());

        Execution execution1 = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).activityId("userTask").singleResult();
        assertThat(execution1).isNotNull();
        assertThat(execution1.getActivityId()).isEqualTo("userTask");

        Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getName()).isEqualTo("User Task");
        taskService.complete(task.getId(), singletonMap("form51outcome", "A"));

        List<Task> tasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).list();
        assertEquals("Task A",tasks.get(0).getName());
    }
}
