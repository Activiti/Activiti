package org.activiti.spring.boot.process;

import org.activiti.api.process.model.ProcessInstance;
import org.activiti.api.process.model.builders.ProcessPayloadBuilder;
import org.activiti.api.process.runtime.ProcessRuntime;
import org.activiti.api.runtime.shared.query.Page;
import org.activiti.api.runtime.shared.query.Pageable;
import org.activiti.api.task.model.Task;
import org.activiti.api.task.model.builders.TaskPayloadBuilder;
import org.activiti.api.task.runtime.TaskRuntime;
import org.activiti.spring.boot.security.util.SecurityUtil;
import org.activiti.spring.boot.test.util.ProcessCleanUpUtil;
import org.activiti.spring.boot.test.util.TaskCleanUpUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class ProcessRuntimeTasksIT {

    private static final String SINGLE_TASK_PROCESS = "SingleTaskProcess";

    @Autowired
    private ProcessRuntime processRuntime;

    @Autowired
    private TaskRuntime taskRuntime;

    @Autowired
    private SecurityUtil securityUtil;

    @Autowired
    private ProcessCleanUpUtil processCleanUpUtil;

    @Autowired
    private TaskCleanUpUtil taskCleanUpUtil;

    @AfterEach
    public void cleanUp() {
        processCleanUpUtil.cleanUpWithAdmin();
        taskCleanUpUtil.cleanUpWithAdmin();
    }

    @Test
    public void should_taskAlwaysHaveAppVersion() {
        securityUtil.logInAs("garth");

        ProcessInstance processInstance = processRuntime.start(ProcessPayloadBuilder.start()
                                                                       .withProcessDefinitionKey(SINGLE_TASK_PROCESS)
                                                                       .build());

        Page<Task> tasks = taskRuntime.tasks(Pageable.of(0, 50),
                                             TaskPayloadBuilder
                                                     .tasks()
                                                     .withProcessInstanceId(processInstance.getId())
                                                     .build());

        assertThat(tasks.getContent()).hasSize(1);

        Task result = tasks.getContent().get(0);

        assertThat(result.getName()).isEqualTo("my-task");
        assertThat(result.getAppVersion()).isEqualTo("1");
    }
}
