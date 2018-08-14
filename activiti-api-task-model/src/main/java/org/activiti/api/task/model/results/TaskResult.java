package org.activiti.api.task.model.results;

import org.activiti.api.model.common.Payload;
import org.activiti.api.model.common.Result;
import org.activiti.api.task.model.Task;


public class TaskResult extends Result<Task> {

    public TaskResult() {
    }

    public TaskResult(Payload payload,
                      Task entity) {
        super(payload,
              entity);
    }
}
