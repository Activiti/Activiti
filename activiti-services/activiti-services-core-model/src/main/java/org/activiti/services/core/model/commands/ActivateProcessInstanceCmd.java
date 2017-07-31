package org.activiti.services.core.model.commands;

import java.util.UUID;

public class ActivateProcessInstanceCmd implements Command {

    private final String id;
    private String processInstanceId;

    public ActivateProcessInstanceCmd() {
        this.id = UUID.randomUUID().toString();
    }

    public ActivateProcessInstanceCmd(String processInstanceId) {
        this();
        this.processInstanceId = processInstanceId;
    }

    @Override
    public String getId() {
        return id;
    }

    public String getProcessInstanceId() {
        return processInstanceId;
    }
}
