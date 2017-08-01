package org.activiti.services.core.model.commands.results;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class StartProcessInstanceResults implements CommandResults {

    private String id;
    private String commandId;
    private String processInstanceId;


    public StartProcessInstanceResults() {
        this.id = UUID.randomUUID().toString();
    }

    @JsonCreator
    public StartProcessInstanceResults(@JsonProperty("id") String id,
                                       @JsonProperty("commandId") String commandId,
                                       @JsonProperty("processInstanceId") String processInstanceId) {
        this.id = id;
        this.commandId = commandId;
        this.processInstanceId = processInstanceId;
    }

    public StartProcessInstanceResults(String commandId,
                                       String processInstanceId) {
        this();
        this.commandId = commandId;
        this.processInstanceId = processInstanceId;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getCommandId() {
        return commandId;
    }

    public String getProcessInstanceId() {
        return processInstanceId;
    }
}
