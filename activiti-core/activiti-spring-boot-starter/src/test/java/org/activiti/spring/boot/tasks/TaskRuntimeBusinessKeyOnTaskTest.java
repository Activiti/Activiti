package org.activiti.spring.boot.tasks;

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

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class TaskRuntimeBusinessKeyOnTaskTest {

    @Autowired
    private TaskRuntime taskRuntime;

    @Autowired
    private ProcessRuntime processRuntime;

    @Autowired
    private SecurityUtil securityUtil;

    private static final String TWOTASK_PROCESS = "twoTaskProcess";

    @Autowired
    private ProcessCleanUpUtil processCleanUpUtil;

    @After
    public void cleanUp() {
        processCleanUpUtil.cleanUpWithAdmin();
    }

    @Test
    public void should_returnBusinessKeyInTasks_when_StartNewProcessWithBusinessKey() {

        securityUtil.logInAs("user");

        //when
        String businesskey = "businesskey";
        processRuntime.start(ProcessPayloadBuilder.start()
                                     .withProcessDefinitionKey(TWOTASK_PROCESS)
                                     .withBusinessKey(businesskey)
                                     .build());

        securityUtil.logInAs("dean");

        Task task = taskRuntime.tasks(Pageable.of(0, 10), TaskPayloadBuilder.tasks().build()).getContent().get(0);

        assertThat(task).isNotNull();
        assertThat(task.getBusinessKey()).isNotBlank();
        assertThat(task.getBusinessKey()).isEqualTo(businesskey);
    }

}
