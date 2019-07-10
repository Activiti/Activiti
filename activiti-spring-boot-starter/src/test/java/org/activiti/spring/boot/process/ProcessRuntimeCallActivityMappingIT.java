package org.activiti.spring.boot.process;

import org.activiti.api.model.shared.model.VariableInstance;
import org.activiti.api.process.model.ProcessInstance;
import org.activiti.api.process.model.builders.ProcessPayloadBuilder;
import org.activiti.api.process.runtime.ProcessRuntime;
import org.activiti.api.runtime.shared.query.Pageable;
import org.activiti.api.task.model.Task;
import org.activiti.api.task.model.builders.TaskPayloadBuilder;
import org.activiti.api.task.runtime.TaskRuntime;
import org.activiti.spring.boot.security.util.SecurityUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@RunWith(SpringRunner.class)
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
    boolean printDebug = true;

    @Test
    public void basicCallActivityMappingTest() {
        securityUtil.logInAs("salaboy");

        ProcessInstance processInstance = processRuntime.start(
                ProcessPayloadBuilder
                        .start()
                        .withProcessDefinitionKey(PARENT_PROCESS_CALL_ACTIVITY)
                        .build());
        assertThat(processInstance).isNotNull();

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
        assertThat(subProcessInstance.getProcessDefinitionKey()).isEqualTo(SUB_PROCESS_CALL_ACTIVITY);

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

        taskRuntime.claim(TaskPayloadBuilder.claim().withTaskId(task.getId()).build());

        printVariables(processInstance,
                       "ParentProcess");

        List<VariableInstance> subProcVariables = processRuntime.variables(ProcessPayloadBuilder
                                                                                   .variables()
                                                                                   .withProcessInstanceId(subProcessInstance.getId())
                                                                                   .build());

        assertThat(subProcVariables).extracting(VariableInstance::getName,
                                                VariableInstance::getValue)
                .containsOnly(
                        tuple("input-variable-name-1",
                              "inName"),
                        tuple("input-variable-name-2",
                              20),
                        tuple("input-variable-name-3",
                              5),
                        tuple("input-static-value",
                              "a static value")
                );

        assertThat("my-task-call-activity").isEqualTo(task.getName());

        Map<String, Object> variablesForTask = new HashMap<>();
        variablesForTask.put("input-variable-name-1",
                             "fromSubprocessName");
        variablesForTask.put("input-variable-name-2",
                             39);
        variablesForTask.put("out-variable-name-1",
                             176);

        printVariables(subProcessInstance,
                       "Subprocess");

        completeTask(task.getId(),
                     variablesForTask);

        List<VariableInstance> parentVariables = processRuntime.variables(ProcessPayloadBuilder
                                                                                  .variables()
                                                                                  .withProcessInstanceId(processInstance.getId())
                                                                                  .build());

        assertThat(parentVariables).extracting(VariableInstance::getName,
                                               VariableInstance::getValue)
                .containsOnly(
                        tuple("name",
                              "fromSubprocessName"),
                        tuple("age",
                              39),
                        tuple("output-unmapped-variable-with-non-matching-connector-output-name",
                              "default"),
                        tuple("input-unmapped-variable-with-non-matching-connector-input-name",
                              "inTest")

                );

        printVariables(processInstance,
                       "ParentProcessAgain");
    }

    @Test
    public void haveToPassAllVariablesCallActivityEmptyMappingNoTaskTest() {
        securityUtil.logInAs("salaboy");
        // After the process has started, the subProcess task should be active
        ProcessInstance processInstance = processRuntime.start(
                ProcessPayloadBuilder
                        .start()
                        .withProcessDefinitionKey(PARENT_PROCESS_CALL_ACTIVITY_EMPTY_MAPPING_NO_TASK)
                        .build());
        assertThat(processInstance).isNotNull();

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
        assertThat(subProcessInstance.getProcessDefinitionKey()).isEqualTo(SUB_PROCESS_CALL_ACTIVITY);

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

        taskRuntime.claim(TaskPayloadBuilder.claim().withTaskId(task.getId()).build());

        printVariables(processInstance,
                       "ParentProcess");

        List<VariableInstance> subProcVariables = processRuntime.variables(ProcessPayloadBuilder
                                                                                   .variables()
                                                                                   .withProcessInstanceId(subProcessInstance.getId())
                                                                                   .build());

        assertThat(subProcVariables).extracting(VariableInstance::getName,
                                                VariableInstance::getValue)
                .containsOnly(
                        tuple("output-unmapped-variable-with-non-matching-connector-output-name",
                              "default"),
                        tuple("input-unmapped-variable-with-non-matching-connector-input-name",
                              "inTest"),
                        tuple("name",
                              "inName"),
                        tuple("age",
                              20)
                );

        assertThat("my-task-call-activity").isEqualTo(task.getName());

        Map<String, Object> variablesForTask = new HashMap<>();
        variablesForTask.put("input-variable-name-1",
                             "fromSubprocessName");

        printVariables(subProcessInstance,
                       "Subprocess");

        completeTask(task.getId(),
                     variablesForTask);
        List<VariableInstance> parentVariablesAfterComnplete = processRuntime.variables(ProcessPayloadBuilder
                                                                                                .variables()
                                                                                                .withProcessInstanceId(processInstance.getId())
                                                                                                .build());

        assertThat(parentVariablesAfterComnplete).extracting(VariableInstance::getName,
                                                             VariableInstance::getValue)
                .containsOnly(
                        tuple("output-unmapped-variable-with-non-matching-connector-output-name",
                              "default"),
                        tuple("input-unmapped-variable-with-non-matching-connector-input-name",
                              "inTest"),
                        tuple("name",
                              "inName"),
                        tuple("age",
                              20),
                        tuple("input-variable-name-1",
                              "fromSubprocessName")
                );

        printVariables(processInstance,
                       "ParentProcessAgain");
    }

    @Test
    public void haveToPassNoVariablesCallActivityEmptyMappingWithTaskTest() {
        securityUtil.logInAs("salaboy");

        ProcessInstance processInstance = processRuntime.start(
                ProcessPayloadBuilder
                        .start()
                        .withProcessDefinitionKey(PARENT_PROCESS_CALL_ACTIVITY_EMPTY_MAPPING_WITH_TASK)
                        .build());
        assertThat(processInstance).isNotNull();

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
        assertThat(subProcessInstance.getProcessDefinitionKey()).isEqualTo(SUB_PROCESS_CALL_ACTIVITY);

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

        taskRuntime.claim(TaskPayloadBuilder.claim().withTaskId(task.getId()).build());

        printVariables(processInstance,
                       "ParentProcess");

        List<VariableInstance> subProcVariables = processRuntime.variables(ProcessPayloadBuilder
                                                                                   .variables()
                                                                                   .withProcessInstanceId(subProcessInstance.getId())
                                                                                   .build());

        assertThat(subProcVariables).extracting(VariableInstance::getName,
                                                VariableInstance::getValue).size().isEqualTo(0);

        assertThat("my-task-call-activity").isEqualTo(task.getName());

        Map<String, Object> variablesForTask = new HashMap<>();
        variablesForTask.put("input-variable-name-1",
                             "fromSubprocessName");

        printVariables(subProcessInstance,
                       "Subprocess");

        completeTask(task.getId(),
                     variablesForTask);
        List<VariableInstance> parentVariablesAfterComnplete = processRuntime.variables(ProcessPayloadBuilder
                                                                                                .variables()
                                                                                                .withProcessInstanceId(processInstance.getId())
                                                                                                .build());
        assertThat(parentVariablesAfterComnplete).extracting(VariableInstance::getName,
                                                             VariableInstance::getValue)
                .containsOnly(
                        tuple("output-unmapped-variable-with-non-matching-connector-output-name",
                              "default"),
                        tuple("input-unmapped-variable-with-non-matching-connector-input-name",
                              "inTest"),
                        tuple("name",
                              "inName"),
                        tuple("age",
                              20)
                );

        printVariables(processInstance,
                       "ParentProcessAgain");
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

    public List<VariableInstance> printVariables(ProcessInstance subProcessInstance,
                                                 String debugString) {
        List<VariableInstance> subProcVariables = processRuntime.variables(ProcessPayloadBuilder
                                                                                   .variables()
                                                                                   .withProcessInstanceId(subProcessInstance.getId())
                                                                                   .build());
        if (printDebug) {
            System.out.println("_______________________________");

            for (VariableInstance sprocVariable : subProcVariables) {
                System.out.println(debugString + ":::::" + sprocVariable.getName() + ":" + sprocVariable.getValue());
            }
        }
        return subProcVariables;
    }
}
