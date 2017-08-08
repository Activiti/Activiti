package org.activiti.services.core.model.commands.results;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.activiti.services.core.model.ProcessInstance;

public class StartProcessInstanceResults extends AbstractCommandResults {

    private ProcessInstance processInstance;

    public StartProcessInstanceResults(String commandId,
                                       ProcessInstance processInstance) {
        super(commandId);
        this.processInstance = processInstance;
    }

    @JsonCreator
    public StartProcessInstanceResults(@JsonProperty("id") String id,
                                       @JsonProperty("commandId") String commandId,
                                       @JsonProperty("processInstance") ProcessInstance processInstance) {
        super(id,
              commandId);
        this.processInstance = processInstance;
    }

    public ProcessInstance getProcessInstance() {
        return processInstance;
    }
}
