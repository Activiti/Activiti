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
package org.activiti.spring.boot.tasks;

import org.activiti.api.model.shared.model.VariableInstance;
import org.activiti.api.process.model.ProcessInstance;
import org.activiti.api.task.model.Task;
import org.activiti.spring.boot.process.ProcessBaseRuntime;
import org.activiti.spring.resolver.EnvironmentVariableELResolver;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource(properties = {
    "spring.activiti.env-var-el-resolver.enabled=false"
})
class EnvironmentVariableELResolverDisabledIT {

    private static final String TASK_EXPRESSION_MAPPING_ENV_VARS = "taskExpressionMappingEnvVars";
    private static final String TASK_EXPRESSION_MAPPING_ENV_VARS_PROCESS_VARS = "taskExpressionMappingEnvVarsAndProcessVar";

    @Autowired
    Environment environment;

    @Autowired
    ApplicationContext context;

    @Autowired
    private TaskBaseRuntime taskBaseRuntime;

    @Autowired
    private ProcessBaseRuntime processBaseRuntime;

    private Task checkTasks(String processInstanceId) {
        List<Task> tasks = taskBaseRuntime.getTasksByProcessInstanceId(processInstanceId);
        assertThat(tasks).isNotEmpty();
        assertThat(tasks).hasSize(1);
        return tasks.get(0);
    }

    @Test
    void should_notLoadEnvironmentVariableELResolver_when_disabled() {
        assertThat(context.getBeansOfType(EnvironmentVariableELResolver.class)).isEmpty();
    }

    @Test
    public void should_notMapTaskVariables_when_inputMappingWithExpression_envVarResolverIsDisabled() {
        ProcessInstance processInstance = processBaseRuntime.startProcessWithProcessDefinitionKey(TASK_EXPRESSION_MAPPING_ENV_VARS);

        Task task = checkTasks(processInstance.getId());

        // input mapping
        List<VariableInstance> taskVariables = taskBaseRuntime.getTasksVariablesByTaskId(task.getId());

        assertThat(taskVariables)
            .isNotNull()
            .extracting(VariableInstance::getName,
                VariableInstance::getValue)
            .containsOnly(
                tuple("inValue", "varValue"),
                tuple("envVar", null),
                tuple("inNull", null)
            );

    }

    @Test
    public void should_mapTaskVariablesToProcessVar_ifProcessVarExists_when_inputMappingWithExpression_envVarResolverIsDisabled() {
        ProcessInstance processInstance = processBaseRuntime.startProcessWithProcessDefinitionKey(TASK_EXPRESSION_MAPPING_ENV_VARS_PROCESS_VARS);

        Task task = checkTasks(processInstance.getId());

        // input mapping
        List<VariableInstance> taskVariables = taskBaseRuntime.getTasksVariablesByTaskId(task.getId());
        assertThat(taskVariables)
            .isNotNull()
            .extracting(VariableInstance::getName,
                VariableInstance::getValue)
            .containsOnly(
                tuple("inValue", "varValue"),
                tuple("envVar", "some_value"),
                tuple("inNull", null)
            );
    }
}
