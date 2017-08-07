package org.activiti.services.core.model.commands.results;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.activiti.services.core.model.ProcessInstance;

public class StartProcessInstanceResults implements CommandResults {

    private String id;
    private String commandId;
    private ProcessInstance processInstance;


    public StartProcessInstanceResults() {
        this.id = UUID.randomUUID().toString();
    }

    @JsonCreator
    public StartProcessInstanceResults(@JsonProperty("id") String id,
                                       @JsonProperty("commandId") String commandId,
                                       @JsonProperty("processInstance") ProcessInstance processInstance) {
        this.id = id;
        this.commandId = commandId;
        this.processInstance = processInstance;
    }

    public StartProcessInstanceResults(String commandId,
                                       ProcessInstance processInstance) {
        this();
        this.commandId = commandId;
        this.processInstance = processInstance;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getCommandId() {
        return commandId;
    }

    public ProcessInstance getProcessInstance() {
        return processInstance;
    }
}
