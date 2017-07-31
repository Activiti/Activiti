package org.activiti.services.core.model.commands.results;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;

public class StartProcessInstanceResults implements CommandResults {

    private String id;
    private String commandId;
    private String processInstanceId;

    public StartProcessInstanceResults() {
        this.id = UUID.randomUUID().toString();
    }

    @JsonCreator
    public StartProcessInstanceResults(String id,
                                       String commandId,
                                       String processInstanceId) {
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
