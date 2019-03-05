package org.activiti.spring.boot.process;

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

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class ProcessRuntimeCallActivityIT {

    private static final String PARENT_PROCESS_CALL_ACTIVITY = "parentproc-843144bc-3797-40db-8edc-d23190b118e3";
    private static final String SUB_PROCESS_CALL_ACTIVITY = "subprocess-fb5f2386-709a-4947-9aa0-bbf31497384f";
    
    @Autowired
    private ProcessRuntime processRuntime;

    @Autowired
    private TaskRuntime taskRuntime;

    @Autowired
    private SecurityUtil securityUtil;

    @Test
    public void testCheckSubProcessTaskWhenCallActivity (){

        securityUtil.logInAs("salaboy");

        // After the process has started, the subProcess task should be active
        ProcessInstance processInstance = processRuntime.start(
                ProcessPayloadBuilder
                        .start()
                        .withProcessDefinitionKey(PARENT_PROCESS_CALL_ACTIVITY)
                        .build());

        assertThat(processInstance).isNotNull();

        //verify the existence of the sub process itself
        ProcessInstance subProcessInstance = processRuntime.processInstances(
                Pageable.of(0, 50),
                ProcessPayloadBuilder
                        .processInstances()
                        .withProcessDefinitionKey(SUB_PROCESS_CALL_ACTIVITY)
                        .build())
                .getContent()
                .get(0);

        assertThat(subProcessInstance).isNotNull();
        assertThat(subProcessInstance.getParentId()).isEqualTo(processInstance.getId());
        assertThat(subProcessInstance.getProcessDefinitionKey()).isEqualTo(SUB_PROCESS_CALL_ACTIVITY);

        Task task = taskRuntime.tasks(
                Pageable.of(0, 50),
                TaskPayloadBuilder
                        .tasks()
                        .withProcessInstanceId(subProcessInstance.getId())
                        .build())
                .getContent()
                .get(0);

        assertThat(task).isNotNull();
        assertThat("my-task-call-activity").isEqualTo(task.getName());

    }

}
