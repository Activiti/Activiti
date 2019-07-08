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

    @Test
    public void basicCallActivityMappingTest() {
        securityUtil.logInAs("salaboy");
        // After the process has started, the subProcess task should be active
        ProcessInstance processInstance = processRuntime.start(
                ProcessPayloadBuilder
                        .start()
                        .withProcessDefinitionKey(PARENT_PROCESS_CALL_ACTIVITY)
                        .build());
        assertThat(processInstance).isNotNull();
        //verify the existence of the sub process itself
        List<ProcessInstance> subProcessInstanceList = processRuntime.processInstances(
                Pageable.of(0, 50),
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

        //verify the existence of the task in the sub process
        List<Task> taskList = taskRuntime.tasks(
                Pageable.of(0, 50),
                TaskPayloadBuilder
                        .tasks()
                        .withProcessInstanceId(subProcessInstance.getId())
                        .build())
                .getContent();

        assertThat(taskList).isNotEmpty();
        Task task = taskList.get(0);

        assertThat(task.getAssignee()).isNull();
        assertThat(task.getStatus()).isEqualTo(Task.TaskStatus.CREATED);

        Task claimedTask = taskRuntime.claim(TaskPayloadBuilder.claim().withTaskId(task.getId()).build());

        printVariables(processInstance, "ParentProcess");


//        assertThat(subProcVariables).extracting(VariableInstance::getName,
//                VariableInstance::getValue)
//                .containsOnly(
//                        tuple("input-variable-name-1",
//                                "inName"),
//                        tuple("input-variable-name-2",
//                                20),
//                        tuple("input-variable-name-3",
//                                5),
//                        tuple("input-static-value",
//                                "a static value")
//                );


        assertThat("my-task-call-activity").isEqualTo(task.getName());

        //set subprocess variables
        //out_var_name_1="fromSubprocessName"
        //out_var_name_2=39

        //finish subprocess

        Map<String, Object> variablesForTask = new HashMap<>();
        variablesForTask.put("input-variable-name-1", "fromSubprocessName");
        variablesForTask.put("input-variable-name-2", 39);
        variablesForTask.put("task-height", 999);

        printVariables(subProcessInstance, "Subprocess");

        //parent process
        List<VariableInstance> parentVariables = processRuntime.variables(ProcessPayloadBuilder
                .variables()
                .withProcessInstanceId(processInstance.getId())
                .build());

//            assertThat(subProcVariables).extracting(VariableInstance::getName,
//                    VariableInstance::getValue)
//                    .containsOnly(
//                            tuple("out-var-name-1",
//                                    "fromSubprocessName"),
//                            tuple("out-var-name-2",
//                                    39)
//                    );
        completeTask(task.getId(), variablesForTask);
        printVariables(processInstance, "ParentProcessAgain");

    }
    // have to pass all variables
    @Test
    public void basicCallActivityEmptyMappingNoTaskTest() {
        securityUtil.logInAs("salaboy");
        // After the process has started, the subProcess task should be active
        ProcessInstance processInstance = processRuntime.start(
                ProcessPayloadBuilder
                        .start()
                        .withProcessDefinitionKey(PARENT_PROCESS_CALL_ACTIVITY_EMPTY_MAPPING_NO_TASK)
                        .build());
        assertThat(processInstance).isNotNull();
        //verify the existence of the sub process itself
        List<ProcessInstance> subProcessInstanceList = processRuntime.processInstances(
                Pageable.of(0, 50),
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

        //verify the existence of the task in the sub process
        List<Task> taskList = taskRuntime.tasks(
                Pageable.of(0, 50),
                TaskPayloadBuilder
                        .tasks()
                        .withProcessInstanceId(subProcessInstance.getId())
                        .build())
                .getContent();

        assertThat(taskList).isNotEmpty();
        Task task = taskList.get(0);

        assertThat(task.getAssignee()).isNull();
        assertThat(task.getStatus()).isEqualTo(Task.TaskStatus.CREATED);

        Task claimedTask = taskRuntime.claim(TaskPayloadBuilder.claim().withTaskId(task.getId()).build());

        printVariables(processInstance, "ParentProcess");


//        assertThat(subProcVariables).extracting(VariableInstance::getName,
//                VariableInstance::getValue)
//                .containsOnly(
//                        tuple("input-variable-name-1",
//                                "inName"),
//                        tuple("input-variable-name-2",
//                                20),
//                        tuple("input-variable-name-3",
//                                5),
//                        tuple("input-static-value",
//                                "a static value")
//                );


        assertThat("my-task-call-activity").isEqualTo(task.getName());

        //set subprocess variables
        //out_var_name_1="fromSubprocessName"
        //out_var_name_2=39

        //finish subprocess

        Map<String, Object> variablesForTask = new HashMap<>();
        variablesForTask.put("input-variable-name-1", "fromSubprocessName");
        variablesForTask.put("input-variable-name-2", 39);
        variablesForTask.put("task-height", 999);




        printVariables(subProcessInstance, "Subprocess");

        //parent process
        List<VariableInstance> parentVariables = processRuntime.variables(ProcessPayloadBuilder
                .variables()
                .withProcessInstanceId(processInstance.getId())
                .build());

//            assertThat(subProcVariables).extracting(VariableInstance::getName,
//                    VariableInstance::getValue)
//                    .containsOnly(
//                            tuple("out-var-name-1",
//                                    "fromSubprocessName"),
//                            tuple("out-var-name-2",
//                                    39)
//                    );
        completeTask(task.getId(), variablesForTask);
        printVariables(processInstance, "ParentProcessAgain");

    }

    @Test
    //no variables must be passed
    public void basicCallActivityEmptyMappingWithTaskTest() {
        securityUtil.logInAs("salaboy");
        // After the process has started, the subProcess task should be active
        ProcessInstance processInstance = processRuntime.start(
                ProcessPayloadBuilder
                        .start()
                        .withProcessDefinitionKey(PARENT_PROCESS_CALL_ACTIVITY_EMPTY_MAPPING_WITH_TASK)
                        .build());
        assertThat(processInstance).isNotNull();
        //verify the existence of the sub process itself
        List<ProcessInstance> subProcessInstanceList = processRuntime.processInstances(
                Pageable.of(0, 50),
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

        //verify the existence of the task in the sub process
        List<Task> taskList = taskRuntime.tasks(
                Pageable.of(0, 50),
                TaskPayloadBuilder
                        .tasks()
                        .withProcessInstanceId(subProcessInstance.getId())
                        .build())
                .getContent();

        assertThat(taskList).isNotEmpty();
        Task task = taskList.get(0);

        assertThat(task.getAssignee()).isNull();
        assertThat(task.getStatus()).isEqualTo(Task.TaskStatus.CREATED);

        Task claimedTask = taskRuntime.claim(TaskPayloadBuilder.claim().withTaskId(task.getId()).build());

        printVariables(processInstance, "ParentProcess");


//        assertThat(subProcVariables).extracting(VariableInstance::getName,
//                VariableInstance::getValue)
//                .containsOnly(
//                        tuple("input-variable-name-1",
//                                "inName"),
//                        tuple("input-variable-name-2",
//                                20),
//                        tuple("input-variable-name-3",
//                                5),
//                        tuple("input-static-value",
//                                "a static value")
//                );


        assertThat("my-task-call-activity").isEqualTo(task.getName());

        //set subprocess variables
        //out_var_name_1="fromSubprocessName"
        //out_var_name_2=39

        //finish subprocess

        Map<String, Object> variablesForTask = new HashMap<>();
        variablesForTask.put("input-variable-name-1", "fromSubprocessName");
        variablesForTask.put("input-variable-name-2", 39);
        variablesForTask.put("task-height", 999);




        printVariables(subProcessInstance, "Subprocess");

        //parent process
        List<VariableInstance> parentVariables = processRuntime.variables(ProcessPayloadBuilder
                .variables()
                .withProcessInstanceId(processInstance.getId())
                .build());

//            assertThat(subProcVariables).extracting(VariableInstance::getName,
//                    VariableInstance::getValue)
//                    .containsOnly(
//                            tuple("out-var-name-1",
//                                    "fromSubprocessName"),
//                            tuple("out-var-name-2",
//                                    39)
//                    );
        completeTask(task.getId(), variablesForTask);
        printVariables(processInstance, "ParentProcessAgain");

    }



    @Test
    public void testCheckSubProcesVariablesCalledManually() {

        securityUtil.logInAs("salaboy");

        // After the process has started, the subProcess task should be active
        ProcessInstance subProcessInstance = processRuntime.start(
                ProcessPayloadBuilder
                        .start()
                        .withProcessDefinitionKey(SUB_PROCESS_CALL_ACTIVITY)
                        .build());

        assertThat(subProcessInstance).isNotNull();

        //Check default process Variables
        List<VariableInstance> procVariables = processRuntime.variables(ProcessPayloadBuilder
                .variables()
                .withProcessInstanceId(subProcessInstance.getId())
                .build());
        assertThat(procVariables)
                .isNotNull()
                .extracting(VariableInstance::getName,
                        VariableInstance::getValue)
                .containsOnly(tuple("input-variable-name-1",
                        "value1"),
                        tuple("input-variable-name-2",
                                0),
                        tuple("input-variable-name-3",
                                0),
                        tuple("out-variable-name-1",
                                "outValue"),
                        tuple("out-variable-name-2",
                                0),
                        tuple("input-static-value",       //Most probably we do not need this
                                "some value")
                );

        //verify task
        List<Task> taskList = taskRuntime.tasks(
                Pageable.of(0, 50),
                TaskPayloadBuilder
                        .tasks()
                        .withProcessInstanceId(subProcessInstance.getId())
                        .build())
                .getContent();

        assertThat(taskList).isNotEmpty();
        Task task = taskList.get(0);

        assertThat(task.getAssignee()).isNull();
        assertThat("my-task-call-activity").isEqualTo(task.getName());

        //Check Task Variables: only variables defined in 'mappings' should be present
        List<VariableInstance> taskVariables = taskRuntime.variables(TaskPayloadBuilder
                .variables()
                .withTaskId(task.getId())
                .build());
        assertThat(taskVariables)
                .isNotNull()
                .extracting(VariableInstance::getName,
                        VariableInstance::getValue)
                .containsOnly(tuple("task-variable-name-1",
                        "value1"),
                        tuple("task-variable-name-2",
                                0),
                        tuple("task-variable-name-3",
                                5)
                );


        //Claim task
        Task claimedTask = taskRuntime.claim(TaskPayloadBuilder.claim().withTaskId(task.getId()).build());
        assertThat(claimedTask.getStatus()).isEqualTo(Task.TaskStatus.ASSIGNED);

        //Complete task with variables
        //This will complete also our process
        Map<String, Object> variablesForTask = new HashMap<>();
        variablesForTask.put("task-variable-name-1", "value from subprocess");
        variablesForTask.put("task-variable-name-2", 39);

        completeTask(task.getId(), variablesForTask);

    }


    @Test
    public void testCheckSubProcessTaskWhenCallActivity() {

        securityUtil.logInAs("salaboy");

        // After the process has started, the subProcess task should be active
        ProcessInstance processInstance = processRuntime.start(
                ProcessPayloadBuilder
                        .start()
                        .withProcessDefinitionKey(PARENT_PROCESS_CALL_ACTIVITY)
                        .build());

        assertThat(processInstance).isNotNull();

        printVariables(processInstance, "PARENT_PROCESS_CALL_ACTIVITY");
        //Check Process Variables
        List<VariableInstance> procVariables = processRuntime.variables(ProcessPayloadBuilder
                .variables()
                .withProcessInstanceId(processInstance.getId())
                .build());
        assertThat(procVariables)
                .isNotNull()
                .extracting(VariableInstance::getName,
                        VariableInstance::getValue)
                .containsOnly(tuple("output-unmapped-variable-with-non-matching-connector-output-name",
                        "default"),
                        tuple("input-unmapped-variable-with-non-matching-connector-input-name",
                                "inTest"),
                        tuple("nickName",
                                "testName"),
                        tuple("name",
                                "inName"),
                        tuple("out-unmapped-variable-matching-name",
                                "default"),
                        tuple("input-unmapped-variable-with-matching-name",
                                "inTest"),
                        tuple("age",
                                20)
                );

        //verify the existence of the sub process itself
        List<ProcessInstance> subProcessInstanceList = processRuntime.processInstances(
                Pageable.of(0, 50),
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
        printVariables(subProcessInstance, "PARENT_PROCESS_CALL_ACTIVITY");

        //Check SubProcess Variables
        procVariables = processRuntime.variables(ProcessPayloadBuilder
                .variables()
                .withProcessInstanceId(subProcessInstance.getId())
                .build());

        //This we expect to have after implementation of mapping
        //Without implementation we have already problems:
        //1. Default process variables are not created, like in the same process started manually

//        assertThat(procVariables)
//        .isNotNull()
//        .extracting(VariableInstance::getName,
//                    VariableInstance::getValue)
//        .containsOnly(tuple("input-variable-name-1",
//                            "inName"), 
//                      tuple("input-variable-name-2",
//                            20),        
//                      tuple("input-variable-name-3",
//                            5),
//                      tuple("out-variable-name-1",
//                            "outValue"),
//                      tuple("out-variable-name-2",
//                            0),
//                      tuple("input-static-value",       //Most probably we do not need this
//                            "a static value")
//                      );

        printVariables(subProcessInstance, "SUBPROCESS_PROCESS_CALL_ACTIVITY");
        //verify the existence of the task in the sub process
        List<Task> taskList = taskRuntime.tasks(
                Pageable.of(0, 50),
                TaskPayloadBuilder
                        .tasks()
                        .withProcessInstanceId(subProcessInstance.getId())
                        .build())
                .getContent();

        assertThat(taskList).isNotEmpty();
        Task task = taskList.get(0);

        assertThat(task.getAssignee()).isNull();
        assertThat("my-task-call-activity").isEqualTo(task.getName());


        //Check Task Variables: only variables defined in 'mappings' should be present
        List<VariableInstance> taskVariables = taskRuntime.variables(TaskPayloadBuilder
                .variables()
                .withTaskId(task.getId())
                .build());

        //This should work after implementation

//        assertThat(taskVariables)
//                .isNotNull()
//                .extracting(VariableInstance::getName,
//                            VariableInstance::getValue)
//                .containsOnly(tuple("task-variable-name-1",
//                                    "inName"),
//                              tuple("task-variable-name-2",
//                                    20),
//                              tuple("task-variable-name-3",
//                                    5)
//                              );


        //Claim task
        Task claimedTask = taskRuntime.claim(TaskPayloadBuilder.claim().withTaskId(task.getId()).build());
        assertThat(claimedTask.getStatus()).isEqualTo(Task.TaskStatus.ASSIGNED);

        //Complete task with variables
        //This will complete also our subprocess
        Map<String, Object> variablesForTask = new HashMap<>();
        variablesForTask.put("out-variable-name-1", "value from subprocess");
        variablesForTask.put("out-variable-name-2", 39);
        variablesForTask.put("task-height", 555);

        //SubProcess should be finished after task complete
        completeTask(task.getId(), variablesForTask);
        printVariables(processInstance, "PARENT_PROCESS_AFTER_SUBPROCESS_FINISHED");


        //check process variables
        procVariables = processRuntime.variables(ProcessPayloadBuilder
                .variables()
                .withProcessInstanceId(processInstance.getId())
                .build());
        assertThat(procVariables)
                .isNotNull()
                .extracting(VariableInstance::getName,
                        VariableInstance::getValue)
                .containsOnly(tuple("output-unmapped-variable-with-non-matching-connector-output-name",
                        "default"),
                        tuple("input-unmapped-variable-with-non-matching-connector-input-name",
                                "inTest"),
                        tuple("nickName",
                                "testName"),
                        tuple("name",
                                "inName"),
                        tuple("out-unmapped-variable-matching-name",
                                "default"),
                        tuple("input-unmapped-variable-with-matching-name",
                                "inTest"),
                        tuple("age",
                                20)
                );

        //Check userTask is available
//        taskList = taskRuntime.tasks(
//                                     Pageable.of(0, 50),
//                                     TaskPayloadBuilder
//                                             .tasks()
//                                             .withProcessInstanceId(processInstance.getId())
//                                             .build())
//                                     .getContent();
//
//         assertThat(taskList).isNotEmpty();
//         task=taskList.get(0);
//
//         assertThat("my-task").isEqualTo(task.getName());
//  
//        //this will complete parent process
//        completeTask(task.getId(), null);

    }

    public void completeTask(String taskId, Map<String, Object> variables) {

        Task completeTask = taskRuntime.complete(TaskPayloadBuilder
                .complete()
                .withTaskId(taskId)
                .withVariables(variables)
                .build());
        assertThat(completeTask).isNotNull();
        assertThat(completeTask.getStatus()).isEqualTo(Task.TaskStatus.COMPLETED);
    }


    public List<VariableInstance> printVariables(ProcessInstance subProcessInstance, String debugString) {
        //subprocess
        List<VariableInstance> subProcVariables = processRuntime.variables(ProcessPayloadBuilder
                .variables()
                .withProcessInstanceId(subProcessInstance.getId())
                .build());

        for (VariableInstance sprocVariable : subProcVariables) {
            System.out.println(debugString + ":::::" + sprocVariable.getName() + ":" + sprocVariable.getValue());
        }
        System.out.println("_______________________________");
        return subProcVariables;
    }
}
