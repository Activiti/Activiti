package org.activiti.api.process.model.results;


import org.activiti.api.process.model.ProcessInstance;
import org.activiti.api.model.common.Payload;
import org.activiti.api.model.common.Result;

public class ProcessInstanceResult extends Result<ProcessInstance> {

    public ProcessInstanceResult() {
    }

    public ProcessInstanceResult(Payload payload,
                                 ProcessInstance entity) {
        super(payload,
              entity);
    }
}
