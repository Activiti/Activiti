package org.activiti.runtime.api.model.results;

import org.activiti.runtime.api.Payload;
import org.activiti.runtime.api.Result;
import org.activiti.runtime.api.model.Task;

public class TaskResult extends Result<Task> {

    public TaskResult() {
    }

    public TaskResult(Payload payload,
                      Task entity) {
        super(payload,
              entity);
    }
}
