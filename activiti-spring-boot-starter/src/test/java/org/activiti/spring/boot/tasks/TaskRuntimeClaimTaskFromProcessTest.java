package org.activiti.spring.boot.tasks;

import static org.assertj.core.api.Assertions.assertThat;

import org.activiti.api.process.model.ProcessInstance;
import org.activiti.api.process.model.builders.ProcessPayloadBuilder;
import org.activiti.api.process.runtime.ProcessRuntime;
import org.activiti.api.runtime.shared.query.Pageable;
import org.activiti.api.task.model.Task;
import org.activiti.api.task.model.builders.TaskPayloadBuilder;
import org.activiti.api.task.runtime.TaskRuntime;
import org.activiti.spring.boot.security.util.SecurityUtil;
import org.activiti.spring.boot.test.util.ProcessCleanUpUtil;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class TaskRuntimeClaimTaskFromProcessTest {

    @Autowired
    private TaskRuntime taskRuntime;

    @Autowired
    private ProcessRuntime processRuntime;

    @Autowired
    private SecurityUtil securityUtil;

    private static final String TWOTASK_PROCESS = "twoTaskProcess";
    private static final String ONETASK_PROCESS = "SingleTaskProcess";
    

    @Autowired
    private ProcessCleanUpUtil processCleanUpUtil;

    @After
    public void cleanUp(){
        processCleanUpUtil.cleanUpWithAdmin();
    }

    @Test
    public void claimTaskWithoutGroup() {

        securityUtil.logInAs("salaboy");

        //when
        ProcessInstance twoTaskInstance = processRuntime.start(ProcessPayloadBuilder.start()
                .withProcessDefinitionKey(TWOTASK_PROCESS)
                .build());

        securityUtil.logInAs("dean");

        Task task = taskRuntime.tasks(Pageable.of(0, 10),TaskPayloadBuilder.tasks().build()).getContent().get(0);

        taskRuntime.claim(TaskPayloadBuilder.claim().withTaskId(task.getId()).build());

        //should still be in dean's list after claiming
        task = taskRuntime.tasks(Pageable.of(0, 10),TaskPayloadBuilder.tasks().build()).getContent().get(0);

        assertThat(task).isNotNull();

        taskRuntime.complete(TaskPayloadBuilder.complete().withTaskId(task.getId()).build());

    }

    @Test
    public void claimTaskWithoutCandidatesAfterTaskRelease() {

        securityUtil.logInAs("salaboy");

        //when
        ProcessInstance processInstance = processRuntime.start(ProcessPayloadBuilder.start()
                .withProcessDefinitionKey(ONETASK_PROCESS)
                .build());

        securityUtil.logInAs("garth");

        Task task = taskRuntime.tasks(Pageable.of(0, 10),TaskPayloadBuilder.tasks().build()).getContent().get(0);
        String taskId = task.getId();
        
        assertThat(task.getAssignee()).isEqualTo("garth");
        assertThat(task.getStatus()).isEqualTo(Task.TaskStatus.ASSIGNED);
        
        Task releasedTask = taskRuntime.release(TaskPayloadBuilder.release().withTaskId(taskId).build());
        
        assertThat(releasedTask.getAssignee()).isNull();
        assertThat(releasedTask.getStatus()).isEqualTo(Task.TaskStatus.CREATED);
        
        //This should not happen
        taskRuntime.claim(TaskPayloadBuilder.claim().withTaskId(taskId).build());
        
        task = taskRuntime.task(taskId);
        assertThat(task).isNotNull();
        assertThat(task.getAssignee()).isEqualTo("garth");
        assertThat(task.getStatus()).isEqualTo(Task.TaskStatus.ASSIGNED);

        taskRuntime.complete(TaskPayloadBuilder.complete().withTaskId(task.getId()).build());

    }
    
    



}
