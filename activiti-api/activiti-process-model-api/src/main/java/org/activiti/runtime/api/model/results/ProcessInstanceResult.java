package org.activiti.runtime.api.model.results;

import org.activiti.runtime.api.Payload;
import org.activiti.runtime.api.Result;
import org.activiti.runtime.api.model.ProcessInstance;

public class ProcessInstanceResult extends Result<ProcessInstance> {

    public ProcessInstanceResult() {
    }

    public ProcessInstanceResult(Payload payload,
                                 ProcessInstance entity) {
        super(payload,
              entity);
    }
}
