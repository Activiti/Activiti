package org.activiti.spring.boot.test.util;

import org.activiti.api.runtime.shared.query.Page;
import org.activiti.api.runtime.shared.query.Pageable;
import org.activiti.api.task.model.Task;
import org.activiti.api.task.model.builders.TaskPayloadBuilder;
import org.activiti.api.task.runtime.TaskAdminRuntime;
import org.activiti.spring.boot.security.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestComponent;

@TestComponent
public class TaskCleanUpUtil {

    @Autowired
    private TaskAdminRuntime taskAdminRuntime;

    @Autowired
    private SecurityUtil securityUtil;

    public void cleanUpWithAdmin() {
        securityUtil.logInAs("admin");
        Page<Task> tasks = taskAdminRuntime.tasks(Pageable.of(0,
                50));
        for (Task task : tasks.getContent()) {
            if (task.getProcessInstanceId() == null) {
                taskAdminRuntime.delete(TaskPayloadBuilder
                                                .delete()
                                                .withTaskId(task.getId())
                                                .withReason("test clean up")
                                                .build());
            }
        }
    }
}
