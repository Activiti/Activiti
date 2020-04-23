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

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.api.model.shared.model.VariableInstance;
import org.activiti.api.process.model.ProcessInstance;
import org.activiti.api.task.model.Task;
import org.activiti.common.util.DateFormatterProvider;
import org.activiti.spring.boot.process.ProcessBaseRuntime;
import org.activiti.spring.boot.test.util.ProcessCleanUpUtil;
import org.activiti.spring.boot.test.util.TaskCleanUpUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ContextConfiguration
public class TaskRuntimeVariableMappingIT {

    private static final String TASK_VAR_MAPPING = "taskVarMapping";

    private static final String TASK_VAR_NO_MAPPING = "taskVariableNoMapping";

    private static final String TASK_EMPTY_VAR_MAPPING = "taskVariableEmptyMapping";

    @Autowired
    private TaskBaseRuntime taskBaseRuntime;

    @Autowired
    private ProcessBaseRuntime processBaseRuntime;

    @Autowired
    private TaskCleanUpUtil taskCleanUpUtil;

    @Autowired
    private ProcessCleanUpUtil processCleanUpUtil;

    @Autowired
    private DateFormatterProvider dateFormatterProvider;

    @AfterEach
    public void cleanUp() {
        processCleanUpUtil.cleanUpWithAdmin();
        taskCleanUpUtil.cleanUpWithAdmin();
    }

    @Test
    public void processTaskVarMapping() {
        ProcessInstance processInstance = processBaseRuntime.startProcessWithProcessDefinitionKey(TASK_VAR_MAPPING);

        Date date = dateFormatterProvider.parse("2019-09-01");
        Date datetime = dateFormatterProvider.parse("2019-09-01T10:20:30.000Z");

        Task task = checkTasks(processInstance.getId());

        assertThat(task.getName()).isEqualTo("testSimpleTask");

        List<VariableInstance> procVariables = processBaseRuntime.getProcessVariablesByProcessId(processInstance.getId());
        assertThat(procVariables)
                .isNotNull()
                .extracting(VariableInstance::getName,
                            VariableInstance::getType,
                            VariableInstance::getValue)
                .containsOnly(tuple("process_variable_unmapped_1",
                                    "string",
                                    "unmapped1Value"),
                              tuple("process_variable_inputmap_1",
                                    "string",
                                    "inputmap1Value"),
                              tuple("process_variable_outputmap_1",
                                    "string",
                                    "outputmap1Value"),
                              tuple("process-variable-date",
                                    "date",
                                    date),
                              tuple("process-variable-datetime",
                                    "date",
                                    datetime)
                              );

        List<VariableInstance> taskVariables = taskBaseRuntime.getTasksVariablesByTaskId(task.getId());

        assertThat(taskVariables)
                .isNotNull()
                .extracting(VariableInstance::getName,
                            VariableInstance::getType,
                            VariableInstance::getValue)
                .containsOnly(tuple("task_input_variable_name_1",
                                    "string",
                                    "inputmap1Value"),
                              tuple("task-variable-date",
                                    "date",
                                    date),
                              tuple("task-variable-datetime",
                                    "date",
                                    datetime)
                              );

        Map<String, Object> variables = new HashMap<>();
        variables.put("task_input_variable_name_1",
                      "outputValue"); //This should not be set to 'process_variable_inputmap_1'
        variables.put("task_output_variable_name_1",
                      "outputTaskValue"); //This should be set to 'process_variable_outputmap_1'

        taskBaseRuntime.completeTask(task.getId(), variables);

        procVariables = processBaseRuntime.getProcessVariablesByProcessId(processInstance.getId());
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
                              "outputTaskValue"),
                        tuple("process-variable-date",
                              date),
                        tuple("process-variable-datetime",
                              datetime)

                );
        processBaseRuntime.delete(processInstance.getId());
    }

    @Test
    public void allVariablesShouldBePassedWhenThereIsNoMapping() {
        ProcessInstance processInstance = processBaseRuntime.startProcessWithProcessDefinitionKey(TASK_VAR_NO_MAPPING);

        Task task = checkTasks(processInstance.getId());

        assertThat(task.getName()).isEqualTo("testSimpleTask");

        List<VariableInstance> procVariables = processBaseRuntime.getProcessVariablesByProcessId(processInstance.getId());

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

        List<VariableInstance> taskVariables = taskBaseRuntime.getTasksVariablesByTaskId(task.getId());

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

        taskBaseRuntime.completeTask(task.getId(), variables);

        procVariables = processBaseRuntime.getProcessVariablesByProcessId(processInstance.getId());
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
        processBaseRuntime.delete(processInstance.getId());
    }

    private Task checkTasks(String processInstanceId) {
        List<Task> tasks = taskBaseRuntime.getTasksByProcessInstanceId(processInstanceId);
        assertThat(tasks).isNotEmpty();
        assertThat(tasks).hasSize(1);
        return tasks.get(0);
    }

    @Test
    public void taskShouldHaveNoVariablesWhenMappingIsEmpty() {
        ProcessInstance processInstance = processBaseRuntime.startProcessWithProcessDefinitionKey(TASK_EMPTY_VAR_MAPPING);

        List<Task> tasks = taskBaseRuntime.getTasksByProcessInstanceId(processInstance.getId());
        assertThat(tasks).isNotEmpty();
        assertThat(tasks).hasSize(1);

        Task task = tasks.get(0);
        assertThat(task.getName()).isEqualTo("testSimpleTask");

        List<VariableInstance> procVariables = processBaseRuntime.getProcessVariablesByProcessId(processInstance.getId());

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

        List<VariableInstance> taskVariables = taskBaseRuntime.getTasksVariablesByTaskId(task.getId());

        assertThat(taskVariables)
                .isEmpty();

        Map<String, Object> variables = new HashMap<>();
        variables.put("task_input_variable_name_1",
                      "outputValue");
        variables.put("task_output_variable_name_1",
                      "outputTaskValue");

        taskBaseRuntime.completeTask(task.getId(), variables);

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

        processBaseRuntime.delete(processInstance.getId());
    }
}
