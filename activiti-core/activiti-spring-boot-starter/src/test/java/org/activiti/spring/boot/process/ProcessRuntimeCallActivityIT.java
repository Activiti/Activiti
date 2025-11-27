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
package org.activiti.spring.boot.process;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.activiti.api.process.model.ProcessInstance;
import org.activiti.api.process.model.builders.ProcessPayloadBuilder;
import org.activiti.api.process.runtime.ProcessRuntime;
import org.activiti.api.runtime.shared.query.Pageable;
import org.activiti.api.task.model.Task;
import org.activiti.api.task.model.builders.TaskPayloadBuilder;
import org.activiti.api.task.runtime.TaskRuntime;
import org.activiti.spring.boot.security.util.SecurityUtil;
import org.activiti.spring.boot.test.util.ProcessCleanUpUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class ProcessRuntimeCallActivityIT {

    private static final String PARENT_PROCESS_CALL_ACTIVITY = "parentproc-843144bc-3797-40db-8edc-d23190b118e3";
    private static final String SUB_PROCESS_CALL_ACTIVITY = "subprocess-fb5f2386-709a-4947-9aa0-bbf31497384f";
    private static final String MAIN_PROCESS_CALL_ACTIVITY = "mainProcess-843144bc-3797-40db-8edc-d23190b1183h";

    @Autowired
    private ProcessRuntime processRuntime;

    @Autowired
    private TaskRuntime taskRuntime;

    @Autowired
    private SecurityUtil securityUtil;

    @Autowired
    private ProcessCleanUpUtil processCleanUpUtil;

    @AfterEach
    public void tearDown() {
        processCleanUpUtil.cleanUpWithAdmin();
    }

    @Test
    public void testCheckSubProcessTaskWhenCallActivity() {
        securityUtil.logInAs("user");

        ProcessInstance processInstance = startProcess(PARENT_PROCESS_CALL_ACTIVITY);

        ProcessInstance subProcessInstance = getFirstChildProcessInstance(processInstance);

        assertThat(subProcessInstance).isNotNull();
        assertThat(subProcessInstance.getParentId()).isEqualTo(processInstance.getId());
        assertThat(subProcessInstance.getProcessDefinitionKey()).isEqualTo(SUB_PROCESS_CALL_ACTIVITY);

        List<Task> taskList = taskRuntime
            .tasks(
                Pageable.of(0, 50),
                TaskPayloadBuilder.tasks().withProcessInstanceId(subProcessInstance.getId()).build()
            )
            .getContent();

        assertThat(taskList).isNotEmpty();

        Task task = taskList.get(0);

        assertThat(task).isNotNull();
        assertThat("my-task-call-activity").isEqualTo(task.getName());
    }

    @Test
    public void testCheckRootProcessInstanceIdWhenCallActivity() {
        securityUtil.logInAs("user");

        ProcessInstance rootProcessInstance = startProcess(MAIN_PROCESS_CALL_ACTIVITY);

        assertThat(rootProcessInstance).isNotNull();

        ProcessInstance parentProcessInstance = getFirstChildProcessInstance(rootProcessInstance);

        assertThat(parentProcessInstance).isNotNull();
        assertThat(parentProcessInstance.getParentId()).isEqualTo(rootProcessInstance.getId());
        assertThat(parentProcessInstance.getProcessDefinitionKey()).isEqualTo(PARENT_PROCESS_CALL_ACTIVITY);
        assertThat(parentProcessInstance.getRootProcessInstanceId()).isEqualTo(rootProcessInstance.getId());

        ProcessInstance subProcessInstance = getFirstChildProcessInstance(parentProcessInstance);

        assertThat(subProcessInstance.getProcessDefinitionKey()).isEqualTo(SUB_PROCESS_CALL_ACTIVITY);
        assertThat(subProcessInstance.getRootProcessInstanceId()).isEqualTo(rootProcessInstance.getId());
    }

    private ProcessInstance getFirstChildProcessInstance(ProcessInstance rootProcessInstance) {
        List<ProcessInstance> parentProcessInstanceList = getChildProcessInstances(rootProcessInstance);

        assertThat(parentProcessInstanceList).isNotEmpty();

        ProcessInstance parentProcessInstance = parentProcessInstanceList.get(0);

        return parentProcessInstance;
    }

    private ProcessInstance startProcess(String processDefinitionKey) {
        return processRuntime.start(
            ProcessPayloadBuilder.start().withProcessDefinitionKey(processDefinitionKey).build()
        );
    }

    private List<ProcessInstance> getChildProcessInstances(ProcessInstance processInstance) {
        return processRuntime
            .processInstances(
                Pageable.of(0, 50),
                ProcessPayloadBuilder.processInstances().withParentProcessInstanceId(processInstance.getId()).build()
            )
            .getContent();
    }
}
