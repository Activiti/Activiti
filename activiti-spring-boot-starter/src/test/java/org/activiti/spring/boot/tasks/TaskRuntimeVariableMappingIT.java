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

package org.activiti.spring.boot.tasks;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.api.model.shared.model.VariableInstance;
import org.activiti.api.process.model.ProcessInstance;
import org.activiti.api.process.model.builders.ProcessPayloadBuilder;
import org.activiti.api.process.runtime.ProcessRuntime;
import org.activiti.api.runtime.shared.query.Page;
import org.activiti.api.runtime.shared.query.Pageable;
import org.activiti.api.task.model.Task;
import org.activiti.api.task.model.Task.TaskStatus;
import org.activiti.api.task.model.builders.GetTasksPayloadBuilder;
import org.activiti.api.task.model.builders.TaskPayloadBuilder;
import org.activiti.api.task.model.payloads.GetTasksPayload;
import org.activiti.api.task.runtime.TaskRuntime;
import org.activiti.spring.boot.security.util.SecurityUtil;
import org.activiti.spring.boot.test.util.ProcessCleanUpUtil;
import org.activiti.spring.boot.test.util.TaskCleanUpUtil;
import org.junit.After;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ContextConfiguration
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TaskRuntimeVariableMappingIT {

    private static final String TASK_VAR_MAPPING = "taskVarMapping";
    private static final String TASK_VAR_NO_MAPPING = "taskVariableNoMapping";
    private static final String TASK_EMPTY_VAR_MAPPING = "taskVariableEmptyMapping";

    @Autowired
    private TaskRuntime taskRuntime;
    @Autowired
    private ProcessRuntime processRuntime;
    @Autowired
    private SecurityUtil securityUtil;
    @Autowired
    private TaskCleanUpUtil taskCleanUpUtil;

    @Autowired
    private ProcessCleanUpUtil processCleanUpUtil;

    @After
    public void cleanUp() {
        processCleanUpUtil.cleanUpWithAdmin();
        taskCleanUpUtil.cleanUpWithAdmin();
    }

    @Test
    public void processTaskVarMapping() {
        securityUtil.logInAs("garth");
        ProcessInstance process = processRuntime.start(ProcessPayloadBuilder.start()
                                                               .withProcessDefinitionKey(TASK_VAR_MAPPING)
                                                               .build());

        Task task = checkTasks(process.getId());

        assertThat(task.getName()).isEqualTo("testSimpleTask");

        List<VariableInstance> procVariables = processRuntime.variables(ProcessPayloadBuilder
                                                                                .variables()
                                                                                .withProcessInstanceId(process.getId())
                                                                                .build());
        assertThat(procVariables)
                .isNotNull()
                .extracting(VariableInstance::getName,
                            VariableInstance::getValue)
                .containsOnly(tuple("process_variable_unmapped_1",
                                    "unmapped1Value"),
                              tuple("process_variable_inputmap_1",
                                    "inputmap1Value"),
                              tuple("process_variable_outputmap_1",
                                    "outputmap1Value"));

        List<VariableInstance> taskVariables = taskRuntime.variables(TaskPayloadBuilder
                                                                             .variables()
                                                                             .withTaskId(task.getId())
                                                                             .build());
        assertThat(taskVariables)
                .isNotNull()
                .extracting(VariableInstance::getName,
                            VariableInstance::getValue)
                .containsOnly(tuple("task_input_variable_name_1",
                                    "inputmap1Value"));

        Map<String, Object> variables = new HashMap<>();
        variables.put("task_input_variable_name_1",
                      "outputValue"); //This should not be set to 'process_variable_inputmap_1'
        variables.put("task_output_variable_name_1",
                      "outputTaskValue"); //This should be set to 'process_variable_outputmap_1'

        completeTask(task.getId(),
                     variables);

        procVariables = processRuntime.variables(ProcessPayloadBuilder
                                                         .variables()
                                                         .withProcessInstanceId(process.getId())
                                                         .build());
        assertThat(procVariables)
                .isNotNull()
                .extracting(VariableInstance::getName,
                            VariableInstance::getValue)
                .containsOnly(
                        tuple("process_variable_unmapped_1",
                              "unmapped1Value"),
                        tuple("process_variable_inputmap_1",
                              "inputmap1Value"),
                        tuple("process_variable_outputmap_1",
                              "outputTaskValue")

                );

        processRuntime.delete(ProcessPayloadBuilder.delete().withProcessInstance(process).build());
    }

    private void completeTask(String taskId,
                              Map<String, Object> variables) {

        Task completeTask = taskRuntime.complete(TaskPayloadBuilder
                                                         .complete()
                                                         .withTaskId(taskId)
                                                         .withVariables(variables)
                                                         .build());
        assertThat(completeTask).isNotNull();
        assertThat(completeTask.getStatus()).isEqualTo(TaskStatus.COMPLETED);
    }

    @Test
    public void allVariablesShouldBePassedWhenThereIsNoMapping() {

        securityUtil.logInAs("garth");
        ProcessInstance process = processRuntime.start(ProcessPayloadBuilder.start()
                                                               .withProcessDefinitionKey(TASK_VAR_NO_MAPPING)
                                                               .build());

        Task task = checkTasks(process.getId());

        assertThat(task.getName()).isEqualTo("testSimpleTask");

        List<VariableInstance> procVariables = processRuntime.variables(ProcessPayloadBuilder
                                                                                .variables()
                                                                                .withProcessInstanceId(process.getId())
                                                                                .build());

        assertThat(procVariables)
                .isNotNull()
                .extracting(VariableInstance::getName,
                            VariableInstance::getValue)
                .containsOnly(tuple("process_variable_unmapped_1",
                                    "unmapped1Value"),
                              tuple("process_variable_inputmap_1",
                                    "inputmap1Value"),
                              tuple("process_variable_outputmap_1",
                                    "outputmap1Value"));

        List<VariableInstance> taskVariables = taskRuntime.variables(TaskPayloadBuilder
                                                                             .variables()
                                                                             .withTaskId(task.getId())
                                                                             .build());

        assertThat(taskVariables)
                .isNotNull()
                .extracting(VariableInstance::getName,
                            VariableInstance::getValue)
                .containsOnly(tuple("process_variable_unmapped_1",
                                    "unmapped1Value"),
                              tuple("process_variable_inputmap_1",
                                    "inputmap1Value"),
                              tuple("process_variable_outputmap_1",
                                    "outputmap1Value"));

        Map<String, Object> variables = new HashMap<>();
        variables.put("task_input_variable_name_1",
                      "outputValue");
        variables.put("task_output_variable_name_1",
                      "outputTaskValue");

        completeTask(task.getId(),
                     variables);

        procVariables = processRuntime.variables(ProcessPayloadBuilder
                                                         .variables()
                                                         .withProcessInstanceId(process.getId())
                                                         .build());
        assertThat(procVariables)
                .isNotNull()
                .extracting(VariableInstance::getName,
                            VariableInstance::getValue)
                .containsExactlyInAnyOrder(
                        tuple("process_variable_unmapped_1",
                              "unmapped1Value"),
                        tuple("process_variable_inputmap_1",
                              "inputmap1Value"),
                        tuple("process_variable_outputmap_1",
                              "outputmap1Value"),
                        tuple("task_input_variable_name_1",
                              "outputValue"),
                        tuple("task_output_variable_name_1",
                              "outputTaskValue")
                        //since there is no mapping for outputs either, all the variables are passed
                );

        processRuntime.delete(ProcessPayloadBuilder.delete().withProcessInstance(process).build());
    }

    private Task checkTasks(String processInstanceId) {
        GetTasksPayload getTasksPayload = new GetTasksPayloadBuilder().withProcessInstanceId(processInstanceId).build();
        Page<Task> tasks = taskRuntime.tasks(Pageable.of(0,
                                                         50),
                                             getTasksPayload);

        assertThat(tasks.getContent()).hasSize(1);
        return tasks.getContent().get(0);
    }

    @Test
    public void taskShouldHaveNoVariablesWhenMappingIsEmpty() {

        securityUtil.logInAs("garth");
        ProcessInstance process = processRuntime.start(ProcessPayloadBuilder.start()
                                                               .withProcessDefinitionKey(TASK_EMPTY_VAR_MAPPING)
                                                               .build());

        Task task = checkTasks(process.getId());

        assertThat(task.getName()).isEqualTo("testSimpleTask");

        List<VariableInstance> procVariables = processRuntime.variables(ProcessPayloadBuilder
                                                                                .variables()
                                                                                .withProcessInstanceId(process.getId())
                                                                                .build());

        assertThat(procVariables)
                .isNotNull()
                .extracting(VariableInstance::getName,
                            VariableInstance::getValue)
                .containsOnly(tuple("process_variable_unmapped_1",
                                    "unmapped1Value"),
                              tuple("process_variable_inputmap_1",
                                    "inputmap1Value"),
                              tuple("process_variable_outputmap_1",
                                    "outputmap1Value"));

        List<VariableInstance> taskVariables = taskRuntime.variables(TaskPayloadBuilder
                                                                             .variables()
                                                                             .withTaskId(task.getId())
                                                                             .build());

        assertThat(taskVariables)
                .isEmpty();

        Map<String, Object> variables = new HashMap<>();
        variables.put("task_input_variable_name_1",
                      "outputValue");
        variables.put("task_output_variable_name_1",
                      "outputTaskValue");

        completeTask(task.getId(),
                     variables);

        assertThat(procVariables)
                .isNotNull()
                .extracting(VariableInstance::getName,
                            VariableInstance::getValue)
                .containsOnly(tuple("process_variable_unmapped_1",
                                    "unmapped1Value"),
                              tuple("process_variable_inputmap_1",
                                    "inputmap1Value"),
                              tuple("process_variable_outputmap_1",
                                    "outputmap1Value"));

        processRuntime.delete(ProcessPayloadBuilder.delete().withProcessInstance(process).build());
    }
}
