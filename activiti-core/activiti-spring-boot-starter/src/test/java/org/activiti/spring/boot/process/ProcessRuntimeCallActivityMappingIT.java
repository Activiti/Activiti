package org.activiti.spring.boot.process;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.api.model.shared.model.VariableInstance;
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
public class ProcessRuntimeCallActivityMappingIT {

    private static final String PARENT_PROCESS_CALL_ACTIVITY = "parentproc-843144bc-3797-40db-8edc-d23190b118e5";
    private static final String SUB_PROCESS_CALL_ACTIVITY = "subprocess-fb5f2386-709a-4947-9aa0-bbf31497384g";

    private static final String PARENT_PROCESS_CALL_ACTIVITY_EMPTY_MAPPING_NO_TASK = "parentproc-843144bc-3797-40db-8edc-d23190b118e6";
    private static final String PARENT_PROCESS_CALL_ACTIVITY_EMPTY_MAPPING_WITH_TASK = "parentproc-843144bc-3797-40db-8edc-d23190b118e7";

    @Autowired
    private ProcessRuntime processRuntime;

    @Autowired
    private TaskRuntime taskRuntime;

    @Autowired
    private SecurityUtil securityUtil;

    @Autowired
    private ProcessCleanUpUtil processCleanUpUtil;

    @AfterEach
    public void cleanUp() {
        processCleanUpUtil.cleanUpWithAdmin();
    }

    @Test
    public void basicCallActivityMappingTest() {
        securityUtil.logInAs("user");

        ProcessInstance processInstance = processRuntime.start(
                ProcessPayloadBuilder
                        .start()
                        .withProcessDefinitionKey(PARENT_PROCESS_CALL_ACTIVITY)
                        .build());
        assertThat(processInstance).isNotNull();

        ProcessInstance subProcessInstance = getSubProcess(processInstance);
        assertThat(subProcessInstance.getProcessDefinitionKey()).isEqualTo(SUB_PROCESS_CALL_ACTIVITY);

        Task task = getTask(subProcessInstance);

        taskRuntime.claim(TaskPayloadBuilder.claim().withTaskId(task.getId()).build());

        List<VariableInstance> subProcVariables = processRuntime.variables(ProcessPayloadBuilder
                .variables()
                .withProcessInstanceId(subProcessInstance.getId())
                .build());

        assertThat(subProcVariables).extracting(VariableInstance::getName,
                VariableInstance::getValue)
                .containsOnly(
                        tuple("subprocess_input_var1", "inName"),
                        tuple("subprocess_input_var2", 20),
                        tuple("subprocess_input_var3", 5),
                        tuple("input_static_value", "a static value"),
                        tuple("subprocess_out_var1", "outValue"),
                        tuple("subprocess_out_var2", 222),
                        tuple("subprocess_static_value", "static some value"),
                        tuple("var_not_exist_in_subprocess_extension_file", 20)

                );

        assertThat("my-task-call-activity").isEqualTo(task.getName());

        Map<String, Object> variablesForTask = new HashMap<>();
        variablesForTask.put("input_variable_name_1", "fromSubprocessName");
        variablesForTask.put("input_variable_name_2", 39);
        variablesForTask.put("out_variable_name_1", 176);

        completeTask(task.getId(), variablesForTask);

        List<VariableInstance> parentVariables = processRuntime.variables(ProcessPayloadBuilder
                .variables()
                .withProcessInstanceId(processInstance.getId())
                .build());

        assertThat(parentVariables).extracting(VariableInstance::getName,
                VariableInstance::getValue)
                .containsOnly(
                        tuple("name", "outValue"),
                        tuple("age", 222),
                        tuple("output_unmapped_variable_with_non_matching_connector_output_name", "default"),
                        tuple("input_unmapped_variable_with_non_matching_connector_input_name", "inTest")
                );

        cleanUp();

    }

    @Test
    public void haveToPassAllVariablesCallActivityEmptyMappingNoTaskTest() {
        securityUtil.logInAs("user");
        // After the process has started, the subProcess task should be active
        ProcessInstance processInstance = processRuntime.start(
                ProcessPayloadBuilder
                        .start()
                        .withProcessDefinitionKey(PARENT_PROCESS_CALL_ACTIVITY_EMPTY_MAPPING_NO_TASK)
                        .build());
        assertThat(processInstance).isNotNull();

        ProcessInstance subProcessInstance = getSubProcess(processInstance);
        assertThat(subProcessInstance.getProcessDefinitionKey()).isEqualTo(SUB_PROCESS_CALL_ACTIVITY);

        Task task = getTask(subProcessInstance);

        taskRuntime.claim(TaskPayloadBuilder.claim().withTaskId(task.getId()).build());

        List<VariableInstance> subProcVariables = processRuntime.variables(ProcessPayloadBuilder
                .variables()
                .withProcessInstanceId(subProcessInstance.getId())
                .build());

        assertThat(subProcVariables).extracting(VariableInstance::getName,
                VariableInstance::getValue)
                .containsOnly(
                        tuple("output_unmapped_variable_with_non_matching_connector_output_name",
                                "default"),
                        tuple("input_unmapped_variable_with_non_matching_connector_input_name",
                                "inTest"),
                        tuple("name",
                                "inName"),
                        tuple("age",
                                20),
                        tuple("subprocess_input_var2",
                                2),
                        tuple("subprocess_input_var3",
                                3),
                        tuple("subprocess_out_var2",
                                222),
                        tuple("subprocess_out_var1",
                                "outValue"),

                        tuple("subprocess_input_var1",
                                "value1"),
                        tuple("subprocess_static_value",
                                "static some value")
                );

        assertThat("my-task-call-activity").isEqualTo(task.getName());

        Map<String, Object> variablesForTask = new HashMap<>();
        variablesForTask.put("input_variable_name_1",
                "fromSubprocessName");

        completeTask(task.getId(),
                variablesForTask);
        List<VariableInstance> parentVariablesAfterComnplete = processRuntime.variables(ProcessPayloadBuilder
                .variables()
                .withProcessInstanceId(processInstance.getId())
                .build());

        assertThat(parentVariablesAfterComnplete).extracting(VariableInstance::getName,
                VariableInstance::getValue)
                .containsOnly(
                        tuple("output_unmapped_variable_with_non_matching_connector_output_name",
                                "default"),
                        tuple("input_unmapped_variable_with_non_matching_connector_input_name",
                                "inTest"),
                        tuple("name",
                                "inName"),
                        tuple("age",
                                20),
                        tuple("subprocess_input_var2",
                                2),
                        tuple("subprocess_input_var3",
                                3),
                        tuple("subprocess_out_var2",
                                222),
                        tuple("subprocess_out_var1",
                                "outValue"),

                        tuple("subprocess_input_var1",
                                "value1"),
                        tuple("subprocess_static_value",
                                "static some value"),
                        tuple("input_variable_name_1",
                                "fromSubprocessName")
                );
    }

    private Task getTask(ProcessInstance subProcessInstance) {
        List<Task> taskList = taskRuntime.tasks(
                Pageable.of(0,
                        50),
                TaskPayloadBuilder
                        .tasks()
                        .withProcessInstanceId(subProcessInstance.getId())
                        .build())
                .getContent();

        assertThat(taskList).isNotEmpty();
        Task task = taskList.get(0);

        assertThat(task.getAssignee()).isNull();
        assertThat(task.getStatus()).isEqualTo(Task.TaskStatus.CREATED);
        return task;
    }

    private ProcessInstance getSubProcess(ProcessInstance processInstance) {
        List<ProcessInstance> subProcessInstanceList = processRuntime.processInstances(
                Pageable.of(0,
                        50),
                ProcessPayloadBuilder
                        .processInstances()
                        .withParentProcessInstanceId(processInstance.getId())
                        .build())
                .getContent();

        assertThat(subProcessInstanceList).isNotEmpty();

        ProcessInstance subProcessInstance = subProcessInstanceList.get(0);

        assertThat(subProcessInstance).isNotNull();
        assertThat(subProcessInstance.getParentId()).isEqualTo(processInstance.getId());
        return subProcessInstance;
    }

    @Test
    public void haveToPassNoVariablesCallActivityEmptyMappingWithTaskTest() {
        securityUtil.logInAs("user");

        ProcessInstance processInstance = processRuntime.start(
                ProcessPayloadBuilder
                        .start()
                        .withProcessDefinitionKey(PARENT_PROCESS_CALL_ACTIVITY_EMPTY_MAPPING_WITH_TASK)
                        .build());
        assertThat(processInstance).isNotNull();

        ProcessInstance subProcessInstance = getSubProcess(processInstance);
        assertThat(subProcessInstance.getProcessDefinitionKey()).isEqualTo(SUB_PROCESS_CALL_ACTIVITY);

        Task task = getTask(subProcessInstance);

        taskRuntime.claim(TaskPayloadBuilder.claim().withTaskId(task.getId()).build());

        List<VariableInstance> subProcVariables = processRuntime.variables(ProcessPayloadBuilder
                .variables()
                .withProcessInstanceId(subProcessInstance.getId())
                .build());

        assertThat(subProcVariables).extracting(VariableInstance::getName,
                VariableInstance::getValue).size().isEqualTo(6);
        assertThat(subProcVariables).extracting(VariableInstance::getName,
                VariableInstance::getValue)
                .containsOnly(
                        tuple("subprocess_input_var2",
                                2),
                        tuple("subprocess_input_var3",
                                3),
                        tuple("subprocess_out_var2",
                                222),
                        tuple("subprocess_out_var1",
                                "outValue"),

                        tuple("subprocess_input_var1",
                                "value1"),
                        tuple("subprocess_static_value",
                                "static some value")
                );

        assertThat("my-task-call-activity").isEqualTo(task.getName());

        Map<String, Object> variablesForTask = new HashMap<>();
        variablesForTask.put("input_variable_name_1",
                "fromSubprocessName");

        completeTask(task.getId(),
                variablesForTask);
        List<VariableInstance> parentVariablesAfterComnplete = processRuntime.variables(ProcessPayloadBuilder
                .variables()
                .withProcessInstanceId(processInstance.getId())
                .build());
        assertThat(parentVariablesAfterComnplete).extracting(VariableInstance::getName,
                VariableInstance::getValue)
                .containsOnly(
                        tuple("output_unmapped_variable_with_non_matching_connector_output_name",
                                "default"),
                        tuple("input_unmapped_variable_with_non_matching_connector_input_name",
                                "inTest"),
                        tuple("name",
                                "inName"),
                        tuple("age",
                                20)
                );
    }

    public void completeTask(String taskId,
                             Map<String, Object> variables) {

        Task completeTask = taskRuntime.complete(TaskPayloadBuilder
                .complete()
                .withTaskId(taskId)
                .withVariables(variables)
                .build());
        assertThat(completeTask).isNotNull();
        assertThat(completeTask.getStatus()).isEqualTo(Task.TaskStatus.COMPLETED);
    }
}
