package org.activiti.spring.boot.tasks;

import org.activiti.api.runtime.shared.query.Page;
import org.activiti.api.runtime.shared.query.Pageable;
import org.activiti.api.task.model.Task;
import org.activiti.api.task.model.builders.TaskPayloadBuilder;
import org.activiti.api.task.runtime.TaskRuntime;
import org.activiti.spring.boot.security.util.SecurityUtil;
import org.activiti.spring.boot.test.util.TaskCleanUpUtil;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class TaskRuntimeAppVersionTest {

    @Autowired
    private TaskRuntime taskRuntime;

    @Autowired
    private SecurityUtil securityUtil;

    @Autowired
    private TaskCleanUpUtil taskCleanUpUtil;

    @After
    public void taskCleanUp() {
        taskCleanUpUtil.cleanUpWithAdmin();
    }

    @Test
    public void should_standaloneTaskAlwaysHaveAppVersion() {
        securityUtil.logInAs("user");

        taskRuntime.create(TaskPayloadBuilder.create()
                                   .withName("new task")
                                   .build());

        Page<Task> tasks = taskRuntime.tasks(Pageable.of(0, 50));

        assertThat(tasks.getContent()).hasSize(1);

        Task result = tasks.getContent().get(0);

        assertThat(result.getName()).isEqualTo("new task");
        assertThat(result.getAppVersion()).isEqualTo("1");
    }
}
